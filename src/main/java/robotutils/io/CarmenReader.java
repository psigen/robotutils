/*
 *  The MIT License
 * 
 *  Copyright 2010 Prasanna Velagapudi <psigen@gmail.com>.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package robotutils.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import robotutils.Pose2D;

/**
 * A simple reader class for parsing CARMEN log files.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class CarmenReader {
    private static Pattern linePat = Pattern.compile(" +");

    BufferedReader log;
    
    static Map<String, Class<? extends Message>> _messageTypes = new LinkedHashMap();
    {
        _messageTypes.put("PARAM", Param.class);
        _messageTypes.put("SYNC", Sync.class);
        _messageTypes.put("ODOM", Odom.class);
        _messageTypes.put("FLASER", FLaser.class);
        _messageTypes.put("RLASER", RLaser.class);
    }

    static public abstract class Message {
        public double ipcTimestamp;
        public String ipcHostname;
        public double loggerTimestamp;

        void parse(String[] args) {
            ipcTimestamp = Double.parseDouble(args[args.length - 3]);
            ipcHostname = args[args.length - 2];
            loggerTimestamp = Double.parseDouble(args[args.length - 1]);
        }
    }

    static public class Param extends Message {
        public String name;
        public String value;

        @Override
        void parse(String[] args) {
            super.parse(args);

            name = args[1];
            value = args[2];
        }

        @Override
        public String toString() {
            return "PARAM [" + name + " = " + value + "] @ " + loggerTimestamp;
        }
    }

    static public class Sync extends Message {
        public String tagName;

        @Override
        void parse(String[] args) {
            super.parse(args);

            tagName = args[1];
        }

        @Override
        public String toString() {
            return "SYNC [" + tagName + "] @ " + loggerTimestamp;
        }
    }

    static public class Odom extends Message {
        public Pose2D pose;
        public Pose2D vel;
        public double accel;

        @Override
        void parse(String[] args) {
            super.parse(args);

            pose = new Pose2D(
                    Double.parseDouble(args[1]),
                    Double.parseDouble(args[2]),
                    Double.parseDouble(args[3])
                    );

            vel = new Pose2D(
                    0.0,
                    Double.parseDouble(args[4]),
                    Double.parseDouble(args[5])
                    );

            accel = Double.parseDouble(args[6]);
        }

        @Override
        public String toString() {
            return "ODOM [" + pose + ", " + vel + ", " + accel + "] @ " + loggerTimestamp;
        }
    }

    static public class FLaser extends Message {
        public double[] readings;
        public Pose2D pose;
        public Pose2D odom;

        @Override
        void parse(String[] args) {
            super.parse(args);

            int numReadings = Integer.parseInt(args[1]);
            readings = new double[numReadings];

            for (int i = 0; i < numReadings; ++i) {
                readings[i] = Double.parseDouble(args[2+i]);
            }

            pose = new Pose2D(
                    Double.parseDouble(args[numReadings + 2]),
                    Double.parseDouble(args[numReadings + 3]),
                    Double.parseDouble(args[numReadings + 4])
                    );

            odom = new Pose2D(
                    Double.parseDouble(args[numReadings + 5]),
                    Double.parseDouble(args[numReadings + 6]),
                    Double.parseDouble(args[numReadings + 7])
                    );
        }

        @Override
        public String toString() {
            return "FLASER [" + pose + ", " + odom + "] @ " + loggerTimestamp;
        }
    }
    
    static public class RLaser extends Message {
        public double[] readings;
        public Pose2D pose;
        public Pose2D odom;

        @Override
        void parse(String[] args) {
            super.parse(args);

            int numReadings = Integer.parseInt(args[1]);
            readings = new double[numReadings];

            for (int i = 0; i < numReadings; ++i) {
                readings[i] = Double.parseDouble(args[2+i]);
            }

            pose = new Pose2D(
                    Double.parseDouble(args[numReadings + 2]),
                    Double.parseDouble(args[numReadings + 3]),
                    Double.parseDouble(args[numReadings + 4])
                    );

            odom = new Pose2D(
                    Double.parseDouble(args[numReadings + 5]),
                    Double.parseDouble(args[numReadings + 6]),
                    Double.parseDouble(args[numReadings + 7])
                    );
        }

        @Override
        public String toString() {
            return "FLASER [" + pose + ", " + odom + "] @ " + loggerTimestamp;
        }
    }
    
    public void load(String logFile) throws FileNotFoundException {
        log = new BufferedReader(new FileReader(logFile));
    }

    public Message step() {
        String line;
        String[] s;

        try {
            // Get next log message
            do {
                line = log.readLine();
                if (line == null) return null;
                s = linePat.split(line);
            } while (s[0].startsWith("#"));

            for (Map.Entry<String, Class<? extends Message> > entry
                    : _messageTypes.entrySet()) {
                if (s[0].equalsIgnoreCase(entry.getKey())) {
                    try {
                        Message msg = entry.getValue().newInstance();
                        msg.parse(s);
                        return msg;
                    } catch (InstantiationException ex) {
                        Logger.getLogger(CarmenReader.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(CarmenReader.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(CarmenReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            System.err.println("Failed to parse line: " + line);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        CarmenReader cr = new CarmenReader();

        if (args.length > 0) {
            cr.load(args[0]);
        } else {
            cr.load("/Users/pkv/Desktop/albertb.sm.log");
        }

        Message msg;
        while ((msg = cr.step()) != null) {
            System.out.println(msg.loggerTimestamp + ": " + msg);
        }
    }
}
