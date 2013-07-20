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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Parsing class that handles the details of converting raw information from 
 * the Create into usable Java objects.
 * 
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class CreateParser {
    private static Logger logger = Logger.getLogger(CreateParser.class.getName());
    private final InputStream in;
    private final OutputStream out;
    
    private int checksum;
    private int readCount;
    
    public CreateParser(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }
    
    private void resetCounters() {
        checksum = 0;
        readCount = 0;
    }

    public synchronized CreateStatus readStream() throws IOException {
        // Check for correct packet header, abort if not matched
        int code = getUInt8();
        if (code != 19) {
            logger.fine("Invalid packet header: " + code);
            return null;
        }
        
        // Initialize a new status object
        CreateStatus status = new CreateStatus();
        
        // Reset checksum and read length counters
        resetCounters();
        
        // Read the next packet off the stream
        int len = getUInt8();
        while (readCount < len) {
            int type = getUInt8();
            switch (type) {
                case 1:
                    getGroup1(status);
                    break;
                case 2:
                    getGroup2(status);
                    break;
                case 3:
                    getGroup3(status);
                    break;
                case 4:
                    getGroup4(status);
                    break;
                case 5:
                    getGroup5(status);
                    break;
                default:
                    logger.fine("Unknown group/packet: " + type);
                    return null;   
            }
        }

        getInt8(); //Add checksum to counter
        if (checksum == 237) { //237 + 19 (Stream header) = 256
            return status;
        } else {
            logger.warning("Invalid packet: " + checksum);
            return null;
        }
    }
    
    public void pauseStream() throws IOException {
        out.write(150);  //Pause/Resume stream command
        out.write(0);    //Pause the stream
        out.flush();
    }
    
    public void resumeStream() throws IOException {
        out.write(150);   //Pause/Resume stream command
        out.write(1);     //Resume the stream
    }
    
    public synchronized final int getInt8() throws IOException {
        return (byte)getUInt8();
    }

    public synchronized final int getUInt8() throws IOException  {
        // Read next byte from stream
        int word = -1;
        synchronized (in) {
            word = in.read();
        }
        if (word == -1) throw new EOFException();
        
        // Update read statistics
        checksum = (checksum + word) & 0xFF;
        readCount++;
        
        return word;
    }

    public synchronized final int getInt16() throws IOException  {
        int hiByte = getInt8();
        int loByte = getUInt8();
        
        return (short)(hiByte << 8 | loByte);
    }

    public synchronized final int getUInt16() throws IOException  {
        int hiByte = getUInt8();
        int loByte = getUInt8();
        return (hiByte << 8 | loByte);
    }

    protected void getGroup1(CreateStatus status) throws IOException {
        int bumpsAndWheelDrops =        getInt8();
        status.wheelDropCaster =        ((bumpsAndWheelDrops & 0x10) != 0);
        status.wheelDropLeft =          ((bumpsAndWheelDrops & 0x08) != 0);
        status.wheelDropRight =         ((bumpsAndWheelDrops & 0x04) != 0);
        status.bumpLeft =               ((bumpsAndWheelDrops & 0x02) != 0);
        status.bumpRight =              ((bumpsAndWheelDrops & 0x01) != 0);

        status.wall =                   (getInt8() != 0);
        status.cliffLeft =              (getInt8() != 0);
        status.cliffFrontLeft =         (getInt8() != 0);
        status.cliffFrontRight =        (getInt8() != 0);
        status.cliffRight =             (getInt8() != 0);
        status.virtualWall =            (getInt8() != 0);

        int overCurrents =              getInt8();
        status.leftWheelOvercurrent =   ((overCurrents & 0x10) != 0);
        status.rightWheelOvercurrent =  ((overCurrents & 0x08) != 0);
        status.lowSideDriver[2] =       ((overCurrents & 0x04) != 0);
        status.lowSideDriver[0] =       ((overCurrents & 0x02) != 0);
        status.lowSideDriver[1] =       ((overCurrents & 0x01) != 0);

        // Read reserved bytes
        getInt16();
    }

    protected void getGroup2(CreateStatus status) throws IOException {
        status.infrared =       (byte)getUInt8();
        
        int buttons = getInt8();
        status.advanceButton =  ((buttons & 0x04) != 0);
        status.playButton =     ((buttons & 0x01) != 0);
        
        status.distance =       getInt16();
        status.angle =          getInt16();
    }

    protected void getGroup3(CreateStatus status) throws IOException {
        int state = getInt8();
        if (state == 0) {
            status.chargingState = CreateStatus.ChargeState.NOT_CHARGING;
        } else if (state == 1) {
            status.chargingState = CreateStatus.ChargeState.RECONDITIONING_CHARGING;
        } else if (state == 2) {
            status.chargingState = CreateStatus.ChargeState.FULL_CHARGING;
        } else if (state == 3) {
            status.chargingState = CreateStatus.ChargeState.TRICKLE_CHARGING;
        } else if (state == 4) {
            status.chargingState = CreateStatus.ChargeState.WAITING;
        } else if (state == 5) {
            status.chargingState = CreateStatus.ChargeState.CHARGING_FAULT;
        }

        status.batteryVoltage =     getUInt16();
        status.batteryCurrent =     getInt16();
        status.batteryTemperature = getInt8();
        status.batteryCharge =      getUInt16();
        status.batteryCapacity =    getUInt16();
    }

    protected void getGroup4(CreateStatus status) throws IOException {
        status.wallSignal =             getUInt16();
        status.cliffLeftSignal =        getUInt16();
        status.cliffFrontLeftSignal =   getUInt16();
        status.cliffFrontRightSignal =  getUInt16();
        status.cliffRightSignal =       getUInt16();

        int digitalInputs = getInt8();
        status.deviceDetect = ((digitalInputs & 0x10) != 0);
        status.cargoBayDigital[3] = ((digitalInputs & 0x08) != 0);
        status.cargoBayDigital[2] = ((digitalInputs & 0x04) != 0);
        status.cargoBayDigital[1] = ((digitalInputs & 0x02) != 0);
        status.cargoBayDigital[0] = ((digitalInputs & 0x01) != 0);

        status.cargoBayAnalog = getUInt16();

        int chargingSources = getInt8();
        status.chargeHomeBase =         ((chargingSources & 0x02) != 0);
        status.chargeInternalCharger =  ((chargingSources & 0x01) != 0);
    }

    protected void getGroup5(CreateStatus status) throws IOException {
        int oiMode = getUInt8();
        if (oiMode == 0) {
            status.oiMode = CreateStatus.OIMode.OFF;
        } else if (oiMode == 1) {
            status.oiMode = CreateStatus.OIMode.PASSIVE;
        } else if (oiMode == 2) {
            status.oiMode = CreateStatus.OIMode.SAFE;
        } else if (oiMode == 3) {
            status.oiMode = CreateStatus.OIMode.FULL;
        }

        status.songNumber =         getUInt8();
        status.songPlaying =        (getInt8() != 0);

        status.numStreamPackets =   getUInt8();

        status.reqVelocity =        getInt16();
        status.reqRadius =          getInt16();
        status.reqRightVelocity =   getInt16();
        status.reqLeftVelocity =    getInt16();
    }

    protected void getGroup6(CreateStatus status) throws IOException {
        getGroup1(status);
        getGroup2(status);
        getGroup3(status);
        getGroup4(status);
        getGroup5(status);
    }
    
}
