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

/**
 * Object storing complete state information for a Create robot.
 *
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class CreateStatus {
    
    /**
     * The state of the caster wheel drop sensor.
     * @return false = wheel raised (normal), true = wheel dropped
     */
    public boolean isWheelDropCaster() { return wheelDropCaster; }
    protected boolean wheelDropCaster;
    
    /**
     * The state of the left wheel drop sensor.
     * @return false = wheel raised (normal), true = wheel dropped
     */
    public boolean isWheelDropLeft() { return wheelDropLeft; }
    protected boolean wheelDropLeft;
    
    /**
     * The state of the caster wheel drop sensor.
     * @return false = wheel raised (normal), true = wheel dropped
     */
    public boolean isWheelDropRight() { return wheelDropRight; }
    protected boolean wheelDropRight;
    
    /**
     * The state of the left side bump sensor.
     * @return false = no collision (normal), true = left side collision
     */
    public boolean isBumpLeft() { return bumpLeft; }
    protected boolean bumpLeft;
    
    /**
     * The state of the right side bump sensor.
     * @return false = no collision (normal), true = right side collision
     */
    public boolean isBumpRight() { return bumpRight; }
    protected boolean bumpRight;
    
    /**
     * The state of the wall sensor.
     * @return false = no wall detected (normal), true = wall detected
     */
    public boolean isWall() { return wall; }
    protected boolean wall;
    
    /**
     * The state of the left cliff sensor.
     * @return false = no cliff (normal), true = cliff detected
     */
    public boolean isCliffLeft() { return cliffLeft; }
    protected boolean cliffLeft;
    
    /**
     * The state of the front left cliff sensor.
     * @return false = no cliff (normal), true = cliff detected
     */
    public boolean isCliffFrontLeft() { return cliffFrontLeft; }
    protected boolean cliffFrontLeft;
    
    /**
     * The state of the front right cliff sensor.
     * @return false = no cliff (normal), true = cliff detected
     */
    public boolean isCliffFrontRight() { return cliffFrontRight; }
    protected boolean cliffFrontRight;
    
    /**
     * The state of the right cliff sensor.
     * @return false = no cliff (normal), true = cliff detected
     */
    public boolean isCliffRight() { return cliffRight; }
    protected boolean cliffRight;
    
    /**
     * The state of the virtual wall detector.
     * Note that this also detects the force field emitted by a charging dock.
     * @return false = no virtual wall (normal), true = virtual wall detected
     */
    public boolean isVirtualWall() { return virtualWall; }
    protected boolean virtualWall;
    
    /**
     * The state of the left wheel overcurrent sensor.
     * @return false = less than 1.0A (normal), true = more than 1.0A
     */
    public boolean isLeftWheelOvercurrent() { return leftWheelOvercurrent; }
    protected boolean leftWheelOvercurrent;
    
    /**
     * The state of the right wheel overcurrent sensor.
     * @return false = less than 1.0A (normal), true = more than 1.0A
     */
    public boolean isRightWheelOvercurrent() { return rightWheelOvercurrent; }
    protected boolean rightWheelOvercurrent;
    
    /**
     * The state of the low-side driver overcurrent sensor.
     * LD-0 trips at 0.5A.
     * LD-1 trips at 0.5A.
     * LD-2 trips at 1.6A.
     * @return false = no overcurrent (normal), true = overcurrent condition
     */
    public boolean isLowSideDriver(int i) { return lowSideDriver[i]; }
    protected boolean[] lowSideDriver = new boolean[3];
    
    /**
     * The IR byte currently being received by the robot.
     * 255 indicates that no byte is being received.
     * See Open Interface specification for a full list of byte codes.
     * @return the byte currently being received.
     */
    public byte getInfrared() { return infrared; }
    protected byte infrared;
    
    /**
     * The state of the Advance button on the robot.
     * @return true if the Advance button is being pushed.
     */
    public boolean isAdvanceButton() { return advanceButton; }
    protected boolean advanceButton;
    
    /**
     * The state of the Play button on the robot.
     * @return true if the Play button is being pushed.
     */
    public boolean isPlayButton() { return playButton; }
    protected boolean playButton;
    
    /**
     * The distance that the robot has traveled in millimeters since the last
     * sensor packet.  Positive values indicate forward motion.
     * @return the distance traveled in millimeters.
     */
    public int getDistance() { return distance; }
    protected int distance;
    
    /**
     * The angle that the robot has turned in degrees since the last
     * sensor packet.  Positive values indicate counter-clockwise motion.
     * @return the angle turned in degrees.
     */
    public int getAngle() { return angle; }
    protected int angle;
    
    /**
     * A code indicating the robot's current charging state.
     * @return the charging state.
     */
    public ChargeState getChargingState() { return chargingState; }
    protected ChargeState chargingState;
    
    /**
     * The voltage of the robot's battery in millivolts.
     * @return battery voltage in millivolts.
     */
    public int getBatteryVoltage() { return batteryVoltage; }
    protected int batteryVoltage;
    
    /**
     * The current flowing into or out of the battery in milliamps.  Negative
     * values indicate that current is flowing out of the battery, as in normal
     * operation.
     * @return battery current in milliamps.
     */
    public int getBatteryCurrent() { return batteryCurrent; }
    protected int batteryCurrent;
    
    /**
     * The temperature of the battery in degrees Celsius.
     * @return battery temperature in degrees Celsius.
     */
    public int getBatteryTemperature() { return batteryTemperature; }
    protected int batteryTemperature;
    
    /**
     * The current charge of the robot's battery in milliamp-hours (mAh).
     * @return battery charge in milliamp-hours (mAh).
     */
    public int getBatteryCharge() { return batteryCharge; }
    protected int batteryCharge;
    
    /**
     * The total capacity of the robot's battery in milliamp-hours (mAh).
     * @return battery capacity in milliamp-hours (mAh).
     */
    public int getBatteryCapacity() { return batteryCapacity; }
    protected int batteryCapacity;
    
    /**
     * The strength of the wall sensor signal.
     * @return strength of the wall sensor signal.
     */
    public int getWallSignal() { return wallSignal; }
    protected int wallSignal;
    
    /**
     * The strength of the left cliff sensor signal.
     * @return strength of the left cliff sensor signal.
     */
    public int getCliffLeftSignal() { return cliffLeftSignal; }
    protected int cliffLeftSignal;
    
    /**
     * The strength of the front left cliff sensor signal.
     * @return strength of the front left cliff sensor signal.
     */
    public int getCliffFrontLeftSignal() { return cliffFrontLeftSignal; }
    protected int cliffFrontLeftSignal;
    
    /**
     * The strength of the front right cliff sensor signal.
     * @return strength of the front right cliff sensor signal.
     */
    public int getCliffFrontRightSignal() { return cliffFrontRightSignal; }
    protected int cliffFrontRightSignal;
    
    /**
     * The strength of the right cliff sensor signal.
     * @return strength of the right cliff sensor signal.
     */
    public int getCliffRightSignal() { return cliffFrontRightSignal; }
    protected int cliffRightSignal;
    
    /**
     * The state of the device detect pin of the cargo bay connector.  When 
     * this bit is low, the interface baud rate is 19200.  By default, this 
     * pin is high, and the interface baud rate is 57600.
     * @return state of the device detect pin.
     */
    public boolean isDeviceDetect() { return deviceDetect; }
    protected boolean deviceDetect;
    
    /**
     * The state of one of the digital inputs on the cargo bay connector.
     * True = the pin is high (5V)
     * False = the pin is low (0V)
     * @param i the index of the digital input (0 to 3) to query.
     * @return the state of the specified digital input.
     */
    public boolean isCargoBayDigital(int i) { return cargoBayDigital[i]; }
    protected boolean[] cargoBayDigital = new boolean[4];
    
    /**
     * The value of the analog input on the cargo bay connector.
     * 0 = 0V
     * 1023 = 5V
     * @return the value of the analog input.
     */
    public int getCargoBayAnalog() { return cargoBayAnalog; }
    protected int cargoBayAnalog;
    
    /**
     * Returns true if the robot is docked to the home base station.
     * @return the status of the connection to the home base.
     */
    public boolean isHomeBase() { return chargeHomeBase; }
    protected boolean chargeHomeBase;
    
    /**
     * Returns true if the robot is plugged into the AC wall adapter (this mode
     * of charging uses the internal charger).
     * @return the status of the connection to the internal charger.
     */
    public boolean isInternalCharger() { return chargeInternalCharger; }
    protected boolean chargeInternalCharger;
    
    /**
     * The current operating mode of the robot.
     * OFF = The robot is powered on only because it is charging.
     * PASSIVE = The robot will provide feedback but not accept controls.
     * SAFE = The robot will accept controls, but will stop moving if a cliff, 
     * wheel drop, or charge event occurs.
     * FULL = The robot will execute any command that is given to it.
     * @return the current operating mode of the robot.
     */
    public OIMode getOIMode() { return oiMode; }
    protected OIMode oiMode;
    
    public int getSongNumber() { return songNumber; }
    protected int songNumber;
    
    public boolean isSongPlaying() { return songPlaying; }
    protected boolean songPlaying;
    
    public int getNumStreamPackets() { return numStreamPackets; }
    protected int numStreamPackets;
    
    public int getReqVelocity() { return reqVelocity; }
    protected int reqVelocity;
    
    public int getReqRadius() { return reqRadius; }
    protected int reqRadius;
    
    public int getReqRightVelocity() { return reqRightVelocity; }
    protected int reqRightVelocity;
    
    public int getReqLeftVelocity() { return reqLeftVelocity; }
    protected int reqLeftVelocity;
            
    public enum ChargeState {
        NOT_CHARGING,
        RECONDITIONING_CHARGING,
        FULL_CHARGING,
        TRICKLE_CHARGING,
        WAITING,
        CHARGING_FAULT;
    }
    
    public enum OIMode {
        OFF,
        PASSIVE,
        SAFE,
        FULL;
    }
    
    @Override
    public String toString() {
        return "Create Status: \n" +
                "Wheel Drops:" +
                " L = " + wheelDropLeft + "," +
                " R = " + wheelDropRight + "," +
                " C = " + wheelDropCaster + "\n" +
                "Bumps:" + 
                " L = " + bumpLeft + "," +
                " R = " + bumpRight + "\n" +
                "Walls:" + 
                " W = " + wall + "," +
                " VW = " + virtualWall + "\n" +
                "Cliffs:" +
                " L = " + cliffLeft + "," +
                " FL = " + cliffFrontLeft + "," +
                " FR = " + cliffFrontRight + "," +
                " R = " + cliffRight + "\n" +
                "Overcurrents:" +
                " L = " + leftWheelOvercurrent + "," +
                " R = " + rightWheelOvercurrent + "," +
                " LS2 = " + lowSideDriver[2] + "," + 
                " LS1 = " + lowSideDriver[1] + "," +
                " LS0 = " + lowSideDriver[0] + "\n" +
                "IR byte: " + infrared + "\n" + 
                "Buttons:" +
                " A = " + advanceButton + "," +
                " P = " + playButton + "\n" +
                "Odometry:" + 
                " D = " + distance + "," + 
                " A = " + angle + "\n" +
                "Charging State: " + chargingState + "\n" +
                "Battery:" + 
                " V = " + batteryVoltage + "," +
                " A = " + batteryCurrent + "," +
                " T = " + batteryTemperature + "," +
                " Q = " + batteryCharge + "," + 
                " C = " + batteryCapacity + "\n" +
                "Wall Signal: " + wallSignal + "\n" +
                "Cliff Signals:" +
                " L = " + cliffLeftSignal + "," +
                " FL = " + cliffFrontLeftSignal + "," +
                " FR = " + cliffFrontRightSignal + "," +
                " R = " + cliffRightSignal + "\n" +
                "Inputs:" +
                " D = {" +
                cargoBayDigital[4] + ", " +
                cargoBayDigital[3] + ", " +
                cargoBayDigital[2] + ", " +
                cargoBayDigital[1] + ", " +
                cargoBayDigital[0] + "}" +
                " A = " + cargoBayAnalog +"\n" +
                "Charging Sources:" +
                " HB = " + chargeHomeBase + "," +
                " I = " + chargeInternalCharger + "\n" +
                "OI Mode: " + oiMode + "\n" +
                "Song:" +
                " # = " + songNumber + "," +
                " P = " + songPlaying + "\n" +
                "Number of Packets: " + numStreamPackets + "\n" +
                "Requested Commands:" +
                " V = " + reqVelocity + "," +
                " R = " + reqRadius + "," +
                " RV = " + reqRightVelocity + "," + 
                " LV = " + reqLeftVelocity + "\n";
    }
}
