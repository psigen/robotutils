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

package robotutils;

import bsh.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * A static autoconfiguration class to load and save parameters in a
 * flat file.  This functionality is achieved by evaluating the file as a
 * BeanShell script that sets public static parameters in other classes.
 * 
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class Config {
    /**
     * Static logging object for this class.
     */
    private static Logger logger = Logger.getLogger(Config.class.getName());

    /**
     * Static interpreter object for loading configurations.
     */
    private static Interpreter bshell = new Interpreter();

    /**
     * Load a set of properties from an input stream, and attempt to set the 
     * values loaded to any public static variables in the specified classes.
     * 
     * @param stream the input configuration as a BeanShell script.
     * @return the result of the script evaluation
     */
    public static Object load(InputStream stream) {
        try {
            return bshell.eval(new InputStreamReader(stream));
        } catch ( TargetError e ) {
            logger.severe(
                "The script or code called by the script threw an exception: "
                + e.getTarget() );
            return null;
        } catch ( EvalError e )    {
            logger.severe(
                "There was an error in evaluating the script:" + e );
            return null;
        }
    }
    
    public static boolean save(OutputStream stream, Class c) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    public static Object load(String filename) {
        try {
            return load(new FileInputStream(filename));
        } catch (FileNotFoundException ex) {
            logger.severe("Error while loading file: " + ex);
            return null;
        }
    }
}
