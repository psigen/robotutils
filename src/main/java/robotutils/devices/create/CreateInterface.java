/*
 * Copyright (c) 2008, Prasanna Velagapudi <pkv@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE PROJECT AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package robotutils.devices.create;

import gnu.io.*;
import java.io.*;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * This class implements the interface to the iRobot Create hardware.
 * 
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class CreateInterface {
    private static Logger logger = Logger.getLogger(CreateInterface.class.getName());
    
    protected SerialPort serialPort;
    protected InputStream in;
    protected OutputStream out;
    
    protected Thread packetParserThread;
    protected PacketParser packetParser;
    protected EventListenerList listenerList = new EventListenerList();
    
    protected Object updateLock = new Object();
    protected CreateStatus currentStatus;
    
    protected boolean advanceLED;
    protected boolean playLED;
    protected int powerLEDColor;
    protected int powerLEDIntensity;
    
    protected static final int SENSOR_RATE = 50;
    protected static final int BAUD_RATE = 57600;
    protected static final int PORT_TIMEOUT = 2000;
    
    /**
     * Attempts to open a connection to a robot on the default gumstix port.
     * @return true if a successful connection was made, false otherwise.
     */
    public synchronized boolean connect() {
        return connect("/dev/ttyS2");
    }
    
    /**
     * Attempts to open a connection to a robot on the specified hardware port.
     * @param portName the name of the serial port the robot is on.
     * @return true if a successful connection was made, false otherwise.
     */
    public synchronized boolean connect(String portName) {
        try {
            CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(portName);
            if (cpi.getPortType() != CommPortIdentifier.PORT_SERIAL)
                throw new NoSuchPortException();
            serialPort = (SerialPort)cpi.open("CreateInterface", PORT_TIMEOUT);
        } catch (NoSuchPortException e) {
            logger.warning(portName + " not found.");
            return false;
        } catch (PortInUseException e) {
            logger.warning(portName + " is in use.");
            return false;
        }
        
        try {
            serialPort.setSerialPortParams(
                    BAUD_RATE, 
                    SerialPort.DATABITS_8, 
                    SerialPort.STOPBITS_1, 
                    SerialPort.PARITY_NONE
                
                );
            serialPort.enableReceiveTimeout(1000);
            serialPort.enableReceiveThreshold(0);
        } catch (UnsupportedCommOperationException e) {
            logger.warning(portName + " does not support Create serial settings.");
            serialPort.close();
            serialPort = null;
            return false;
        }
        
        try {
            in = serialPort.getInputStream();
            out = serialPort.getOutputStream();
        } catch (IOException e) {
            logger.warning("Failed to get IO streams for " + portName + ": " + e);
            serialPort.close();
            serialPort = null;
            return false;
        }
        
        advanceLED = false;
        playLED = false;
        powerLEDColor = 0;
        powerLEDIntensity = 0;
        
        packetParser = new PacketParser();
        packetParserThread = new Thread(packetParser);
        packetParserThread.start();
        return true;
    }
    
    /**
     * Disconnects any existing connection to a robot.
     */
    public synchronized void disconnect() {
        // Only disconnect if a connection is open
        if (!isConnected()) return;
        
        packetParser.stop();
        
        try {
            out.close();
        } catch (IOException e) {
            logger.warning("Failed to close output stream: " + e);
        }
        
        try {
            in.close();
        } catch (IOException e) {
            logger.warning("Failed to close input stream: " + e);
        }
        
        serialPort.close();
        serialPort = null;
        
        advanceLED = false;
        playLED = false;
        powerLEDColor = 0;
        powerLEDIntensity = 0;
        
        try { packetParserThread.join(2000); } catch (InterruptedException e) {
            logger.warning("Uncaught listener thread still running.");
        }
    }
    
    /**
     * Returns the current connection status of the interface.
     * @return true if connected to a robot, false otherwise.
     */
    public synchronized boolean isConnected() {
        return (serialPort != null);
    }
    
    /**
     * Writes the necessary initialization string over serial to the robot.
     */
    public void initialize() {
        // Only send if a connection is open
        if (!isConnected()) return;
        
        try {
            synchronized(out) {
                out.write(128); //Enter SAFE mode, enable interface
                out.flush();

                out.write(132); //Enter FULL mode, enable motors
                out.flush();
            }
        } catch (IOException e) {
            logger.warning("Failed to write initialization: " + e);
            disconnect();
        }
    }
    
    /**
     * Writes the necessary initialization string over serial to the robot.
     */
    public void deinitialize() {
        // Only send if a connection is open
        if (!isConnected()) return;
        
        try {
            synchronized(out) {
                out.write(128);  //Enter SAFE mode, enable interface
                out.flush();
            }
        } catch (IOException e) {
            logger.warning("Failed to write deinitialization: " + e);
            disconnect();
        }
    }
    
    /**
     * Adds a new listener that receives events when the robot status changes.
     * @param listener the listener to add.
     */
    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    /**
     * Removes an existing listener that receives events when robot status 
     * changes.
     * @param listener the listener to remove.
     */
    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }
    
    /**
     * Sends an event when the robot status changes in some way.
     * @param e the event representing the status change.
     */
    protected void fireChangeEvent(ChangeEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }
    
    /**
     * Queries the current robot status.
     * @return the current robot status.
     */
    public CreateStatus getStatus() { 
        CreateStatus st;
        synchronized(updateLock) {
            st = currentStatus;
        }
        return st;
    }
    
    /**
     * Sets the internal field storing the current robot status.
     * @param status the new robot status to be stored.
     */
    protected void setStatus(CreateStatus status) {
        synchronized(updateLock) {
            currentStatus = status;
        }
        
        fireChangeEvent(new ChangeEvent(this));
    }
    
    /**
     * This thread reads incoming sensor packets from the robot and parses them.
     */
    protected class PacketParser implements Runnable {
        private CreateParser parser = new CreateParser(in, out);
        private volatile boolean stop = false;
        
        public void stop() {
            this.stop = true;
        }
        
        @Override
        public void run() {
            while(!stop) {
                try {
                    requestStatus();
                    readStatus();
                    Thread.sleep(SENSOR_RATE);
                } catch (InterruptedException e) {
                } catch (EOFException e) {
                } catch (IOException e) {
                    logger.warning("Failed to read from stream: " + e);
                }
            }
        }
        
        private final void readStatus() throws IOException {
            CreateStatus status = new CreateStatus();
            parser.getGroup6(status);
            if (status != null) {
                setStatus(status);
            }
        }
        
        private final void requestStatus() throws IOException {
            synchronized(out) {
                out.write(142); //Stream command (start streaming data)
                out.write(6);   //Complete sensor packet
                out.flush();
            }
        }
    }
    
    /**
     * Commands the robot to drive at a specified velocity and curvature.
     * @param velocity the desired robot velocity in mm/s (-500 to 500).
     * @param radius the desired radius of curvature in mm (-2000 to 2000).
     */
    public void drive(int velocity, int radius) {
        if (!isConnected()) return;
        
        try {
            synchronized(out) {
                out.write(137);
                out.write(((velocity >> 8) & 0xFF));
                out.write((velocity & 0xFF));
                out.write(((radius >> 8) & 0XFF) );
                out.write((radius & 0xFF));
            }
        } catch (IOException e) {
            logger.warning("Failed drive command: " + e);
            disconnect();
        }
    }
    
    /** 
     * Commands the robot to drive each wheel at a specified velocity.
     * @param left the desired left wheel speed in mm/s (-500 to 500).
     * @param right the desired right wheel speed in mm/s (-500 to 500).
     */
    public void driveDirect(int left, int right) {
        if (!isConnected()) return;
        
        try {
            synchronized(out) {
                out.write(145);
                out.write(((right >> 8) & 0xFF));
                out.write((right & 0xFF));
                out.write(((left >> 8) & 0XFF) );
                out.write((left & 0xFF));
            }
        } catch (IOException e) {
            logger.warning("Failed drive direct command: " + e);
            disconnect();
        }
    }
    
    /**
     * Turns the Advance LED on the robot on and off.
     * @param on the desired state of the LED.
     */
    public void setAdvanceLED(boolean on) {
        setLEDs(on, playLED, powerLEDColor, powerLEDIntensity);
    }
    
    /**
     * Turns the Play LED on the robot on and off.
     * @param on the desired state of the LED.
     */
    public void setPlayLED(boolean on) {
        setLEDs(advanceLED, on, powerLEDColor, powerLEDIntensity);
    }
    
    /**
     * Adjusts the bi-color Power LED state, where 0 = green, 255 = red.
     * @param color the desired color of the LED (0 - 255).
     * @param intensity the desired brightness of the LED (0 - 255).
     */
    public void setPowerLED(int color, int intensity) {
        setLEDs(advanceLED, playLED, color, intensity);
    }
    
    /**
     * Writes the command to set all the LED states at once.
     * @param advance the desired state of the Advance LED.
     * @param play the desired state of the Play LED.
     * @param powerColor the desired color of the Power LED (0 - 255).
     * @param powerIntensity the desired brightness of the Power LED (0 - 255).
     */
    protected void setLEDs(boolean advance, boolean play, int powerColor, int powerIntensity) {
        if (!isConnected()) return;
        
        // Construct a bitfield for the advance and play buttons
        int bits = 0;
        bits += (advance) ? 8 : 0;
        bits += (play) ? 2 : 0;
        
        try {
            // Send out command
            synchronized(out) {
                out.write(139);
                out.write(bits);
                out.write((powerColor & 0xFF));
                out.write((powerIntensity & 0xFF));
            }
            
            // Update internal fields
            advanceLED = advance;
            playLED = play;
            powerLEDColor = powerColor & 0xFF;
            powerLEDIntensity = powerIntensity & 0xFF;
        } catch (IOException e) {
            logger.warning("Failed LED command: " + e);
            disconnect();
        }
    }
    
    /**
     * On exit the robot is disconnected, if possible.
     */
    @Override
    protected void finalize(){
        deinitialize();
        disconnect();
    }
    
    /**
     * Opens a serial connection to a Create robot.  State information is 
     * printed out to the screen.
     * @param args Command line parameters (ignored)
     */
    public static void main (String[] args) throws IOException {
        System.out.println(System.getProperty("java.library.path"));
        
        // Set up robot for connection
        CreateInterface create = new CreateInterface();
        create.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println(((CreateInterface)e.getSource()).getStatus());
            }
        });
        
        // Make a connection to the robot
        String port = (args.length > 0) ? args[0] : "/dev/ttyS2";
        if (!create.connect(port)) {
            System.out.println("Failed to connect to robot.");
            return;
        }
        create.initialize();
        
        // Wait for user input
        System.out.println("Press Enter to end test.");
        System.in.read();
        System.out.println("Ending test.");

        // Disconnect from the robot
        create.disconnect();
    }
}