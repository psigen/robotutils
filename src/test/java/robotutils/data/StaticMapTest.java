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

package robotutils.data;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class StaticMapTest {
    
    /**
     * Test of resize method, of class StaticMap.
     */
    @Test
    public void testResize() {
        System.out.println("resize");
        int[] dims = {100, 400, 200};
        StaticMap instance = new StaticMap();
        instance.resize(dims);

        assertEquals(3, instance.dims());

        assertEquals(100, instance.size(0));
        assertEquals(400, instance.size(1));
        assertEquals(200, instance.size(2));

        assertEquals(100, instance.sizes()[0]);
        assertEquals(400, instance.sizes()[1]);
        assertEquals(200, instance.sizes()[2]);

        assertEquals(3, instance._cumSizes.length);

        assertEquals(1, instance._cumSizes[0]);
        assertEquals(100, instance._cumSizes[1]);
        assertEquals(100*400, instance._cumSizes[2]);

        assertEquals(100*400*200, instance.length());
    }

    /**
     * Test of get and set methods, of class StaticMap.
     */
    @Test
    public void testGetAndSet() {
        System.out.println("get/set");
        StaticMap instance = new StaticMap(100, 200, 300);

        Random rnd = new Random();
        int[] idx = new int[3];

        for (int i = 0; i < 1000; i++) {
            byte b = (byte)rnd.nextInt();
            idx[0] = rnd.nextInt(instance.size(0));
            idx[1] = rnd.nextInt(instance.size(1));
            idx[2] = rnd.nextInt(instance.size(2));

            instance.set(b, idx);
            assertEquals(instance.get(idx), b);
        }
    }
}