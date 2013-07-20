/*
 *  Copyright (c) 2009, Prasanna Velagapudi <pkv@cs.cmu.edu>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the project nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ''AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE PROJECT AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package robotutils.simulation;

import java.io.*;
import java.net.*;
import java.util.EventListener;
import java.util.EventObject;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 * Connects to the ImageServer and parses incoming images, then sends them
 * to the appropriate listeners.
 * @author pkv
 */
public class ImageServerLink implements Runnable {
    private static final Logger logger = Logger.getLogger(ImageServerLink.class.getName());
    
    // Connection to video server
    private Socket sock;
    private BufferedWriter out;
    private DataInputStream in;
    
    // Default connection settings
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final int DEFAULT_PORT = 3000;
    public static final int DEFAULT_TIMEOUT = 2000;

    // Listener notification
    protected EventListenerList listenerList;
    protected boolean isStreaming = true;
     
    // Buffer for imaging
    private byte[] imgBuffer;
    
    /** Creates a new instance of ImageServerLink */
    public ImageServerLink() {
        sock = new Socket();
        imgBuffer = new byte[0];
        listenerList = new EventListenerList();
    }

    public void connect() {
        connect(DEFAULT_HOSTNAME, DEFAULT_PORT);
    }

    public void connect(String hostname) {
        connect(hostname, DEFAULT_PORT);
    }

    public void connect(int port) {
        connect(DEFAULT_HOSTNAME, port);
    }

    /**
     * Attempt a connection to the ImageServer, or do nothing if there is
     * already a connection open.
     */
    public void connect(String hostname, int port) {
        if (isConnected()) return;

        try {
            sock = new Socket();
            sock.connect(new InetSocketAddress(hostname, port), 2000);
            sock.setSoTimeout(500);
            out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            in = new DataInputStream(sock.getInputStream());
            new Thread(this).start();
        } catch (SocketTimeoutException e) {
            logger.info("Failed to connect to ImageServer.");
        } catch (IOException e) {
            logger.warning("Failed to open socket: " + e);
        }
    }

    public void disconnect() {
        if (!isConnected()) return;

        try { sock.close(); } catch (IOException e) {
            logger.warning("Failed to close socket: " + e);
        }

        try { out.close(); } catch (IOException e) {
            logger.warning("Failed to close output stream: " + e);
        }

        try { in.close(); } catch (IOException e) {
            logger.warning("Failed to close input stream: " + e);
        }
    }
    
    /**
     * Indicates the connection status of the link.
     * @return true if there is a connection, false if not.
     */
    public boolean isConnected() {
        return (sock != null)
                && (sock.isConnected())
                && (!sock.isClosed());
    }
    
    /** 
     * Adds a new listener that will receive images from the server.
     * @param listener the listener object that will be added.
     */
    public void addImageEventListener(ImageEventListener listener) {
        listenerList.add(ImageEventListener.class, listener);
    }
    
    /**
     * Removes an existing listener from the notification list for receiving 
     * images from the server.
     * @param listener the listener object that will be removed.
     */
    public void removeUpdateEventListener(ImageEventListener listener) {
        listenerList.remove(ImageEventListener.class, listener);
    }
    
    /**
     * Handler that dispatches images to all of the listeners.
     * @param evt the image event received from the server.
     */
    protected void fireUpdateEventMessage(ImageEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==ImageEventListener.class) {
                ((ImageEventListener)listeners[i+1]).receivedImage(evt);
            }
        }

    }

    public interface ImageEventListener extends EventListener {
        public void receivedImage(ImageEvent evt);
    }

    public class ImageEvent extends EventObject {
        public ImageEvent(ImageServerLink source) {
            super(source);
        }
    }
    
    /** 
     * Reads in a valid image and stores it.
     * @return the success of the read operation.
     * @throws java.io.IOException
     */
    private synchronized boolean receiveImage() throws IOException {
        byte type = in.readByte();
        int size = in.readInt();

        // We expect types above 1, they are JPEG compressed
        if (type <= 1) 
            return false;

        // If we get a ridiculously sized image, just drain
        if ((size > 250000)||(size <= 0)) {
            drainImage();
            logger.severe("Failure : Invalid ImgServer image size : " + size);
            return false;
        }
        
        // Reallocate buffer to match image stream
        if (imgBuffer.length != size);
            imgBuffer = new byte[size];

        // Read in the whole image
        int pos = 0;
        while (pos < size) {
            pos += in.read(imgBuffer,pos,size-pos);
            Thread.yield();
        }
        
        return true;
    }
   
    /**
     * Reads a valid image and ignores it.
     * @throws java.io.IOException
     */
    private synchronized void skipImage() throws IOException {
        byte type = in.readByte();
        int size = in.readInt();

        // We expect types above 1, they are JPEG compressed
        if (type <= 1) 
            return;

        // If we get a ridiculously sized image, just drain
        if ((size > 250000)||(size <= 0)) {
            drainImage();
            logger.severe("Failure : Invalid ImgServer image size : " + size);
            return;
        }
        
        // Skip the whole image
        int pos = 0;
        while (pos < size) {
            pos += in.skipBytes(size-pos);
            Thread.yield();
        }
    }
    
    /** 
     * Drains remaining bytes on input buffer.
     * @throws java.io.IOException
     */
    private synchronized void drainImage() throws IOException {
        // Just drain the read buffer, we don't want the data
        in.skipBytes(in.available());
    }
    
    /**
     * Request the next image from the image server.
     * @throws java.io.IOException
     */
    private synchronized void ackImage() throws IOException {
        if (out == null) return;
       
        // Send out the official 'OK'
        out.write("OK");
        out.flush();
    }
    
    /**
     * This gets a pointer to the <b>current image buffer</b>.
     * @return the current image.
     */
    public synchronized byte[] getDirectImage() {
        return imgBuffer;
    }
    
    /** 
     * This returns a copy of the current image.
     * @return a copy of the current image.
     */
    public synchronized byte[] getImage() {
        return imgBuffer.clone();
    }
    
    /** 
     * Non-blocking trigger to capture a single picture.
     */
    public void takePicture() {
        try {
            ackImage();
        } catch (IOException e) {
            logger.severe("Failed to request image from image server : " + e);
        }
    }
 
    @Override
    public void run() {
        while (sock.isConnected()) {
            try {
                // Wait for new data to come in
                while(in.available() == 0) { 
                    try {Thread.sleep(100);} 
                    catch (InterruptedException e) {} 
                }
              
                // Get the next image
                if (receiveImage()) {
                    // Alert the image listeners
                    fireUpdateEventMessage(new ImageEvent(this));
                    
                    if (isStreaming) {
                        try {Thread.sleep(100);}
                        catch (InterruptedException e) {}
                        takePicture();
                    }
                } else {
                    // Remove bits of this image, then re-request
                    drainImage();
                    takePicture();
                }
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                logger.severe("Error in image server connection : " + e);
                break;
            }
        }
    }
}

