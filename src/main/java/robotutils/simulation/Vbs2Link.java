/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package robotutils.simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.EventListener;
import java.util.EventObject;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 * A simple communications class that talks to VBS2 over a socket using the
 * TcpBridge plugin.
 * @author pkv
 */
public class Vbs2Link {
    private static final Logger logger = Logger.getLogger(Vbs2Link.class.getName());
    private static final Charset DEFAULT_CHARSET = Charset.forName("US-ASCII");
    private static final byte[] DEFAULT_LINE_ENDING = "\r\n".getBytes(DEFAULT_CHARSET);
    private static final String EVENT_REGEX = "^#.*";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final int DEFAULT_PORT = 6223;
    public static final int DEFAULT_TIMEOUT = 2000;

    private final LinkedBlockingQueue<String> responses = new LinkedBlockingQueue<String>();
    private final EventListenerList listenerList = new EventListenerList();
    private MessageEvent msgEvent = null;

    private Socket sock;
    private BufferedReader in;
    private OutputStream out;
    private final Object sendLock = new Object();

    public void connect() {
        connect(DEFAULT_HOSTNAME, DEFAULT_PORT);
    }

    public void connect(String hostname) {
        connect(hostname, DEFAULT_PORT);
    }

    public void connect(int port) {
        connect(DEFAULT_HOSTNAME, port);
    }

    public void connect(String hostname, int port) {
        if (isConnected()) return;

        try {
            sock = new Socket();
            sock.connect(new InetSocketAddress(hostname, port), DEFAULT_TIMEOUT);
            out = sock.getOutputStream();
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            new Thread(new Listener()).start();
        } catch (SocketTimeoutException e) {
            logger.info("Failed to connect to VBS2.");
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

    public boolean isConnected() {
        return (sock != null)
                && (sock.isConnected())
                && (!sock.isClosed());
    }

    protected void send(String cmd) {
        if (!isConnected()) return;

        synchronized(sendLock) {
            byte[] b = cmd.getBytes(DEFAULT_CHARSET);

            try {
                out.write(b);
                out.write(DEFAULT_LINE_ENDING);
                out.flush();
            } catch (IOException e){
                logger.severe("Failed to transmit string '" + cmd + "': " + e);
            }
        }
    }

    public String evaluate(String cmd) {
        if (!isConnected()) return new String();

        try {
            send(cmd);
            return responses.take();
        } catch (InterruptedException e) {
            logger.warning("No response to: " + cmd);
            return new String();
        }
    }
    
    public void addMessageListener(MessageListener l) {
        listenerList.add(MessageListener.class, l);
    }

    public void removeMessageListener(MessageListener l) {
        listenerList.remove(MessageListener.class, l);
    }

    protected void fireMessage(String msg) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MessageListener.class) {
                // Lazily create the event
                if (msgEvent == null) msgEvent = new MessageEvent(this, msg);
                ((MessageListener)listeners[i+1]).receivedMessage(msgEvent);
            }
        }

        // Release reference to object
        msgEvent = null;
    }

    public interface MessageListener extends EventListener {
        public void receivedMessage(MessageEvent evt);
    }

    public class MessageEvent extends EventObject {
        private final String message;

        public MessageEvent(Vbs2Link src, String msg) {
            super(src);
            message = msg;
        }

        public String getMessage() { return message; }

        @Override
        public String toString() { 
            return super.toString() + "[message=" + message + "]";
        }
    }

    private class Listener implements Runnable {
        public void run() {
            try {
                while (isConnected()) {
                    String line = in.readLine();
                    if (line.matches(EVENT_REGEX)) {
                        fireMessage(line);
                    } else {
                        try {
                            responses.put(line);
                        } catch (InterruptedException e) {
                            logger.warning("Failed to queue response: " + line);
                        }
                    }
                }
            } catch (SocketException e) {
            } catch (IOException e) {
                logger.warning("Failed to read from socket: " + e);
            }
        }
    }
}
