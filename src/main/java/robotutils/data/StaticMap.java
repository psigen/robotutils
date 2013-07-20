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

import java.util.Arrays;

/**
 * This is a map implementation that just uses a large internal array.
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class StaticMap implements GridMap {
    byte[] _map = null;
    int[] _sizes = new int[0];
    int[] _cumSizes = new int[0];
    int _length = 0;

    public StaticMap() {    
    }

    public StaticMap(int... sizes) {
        resize(sizes);
    }

    public void resize(int... sizes) {
        _sizes = Arrays.copyOf(sizes, sizes.length);
        _cumSizes = new int[_sizes.length];

        _cumSizes[0] = 1;
        for (int i = 1; i < _sizes.length; i++) {
            _cumSizes[i] = _cumSizes[i-1] * _sizes[i-1];
        }

        _length = _cumSizes[_sizes.length - 1] * _sizes[_sizes.length - 1];
        _map = new byte[_length];
    }

    protected int index(int[] idx) {
        int linIdx = 0;
        
        for (int i = 0; i < _sizes.length; i++) {
            if (idx[i] < 0) return -1;
            if (idx[i] >= _sizes[i]) return -1;

            linIdx += _cumSizes[i]*idx[i];
        }

        return linIdx;
    }

    public byte get(int... idx) {
        int i = index(idx);
        return (i >= 0) ? _map[i] : 0;
    }

    public void set(byte val, int... idx) {
        int i = index(idx);
        if (i >= 0) _map[i] = val;
    }

    public int length() {
        return _length;
    }

    public int size(int dim) {
        return _sizes[dim];
    }

    public int[] sizes() {
        return Arrays.copyOf(_sizes, _sizes.length);
    }

    public int dims() {
        return _sizes.length;
    }

    public byte[] getData() {
        return _map;
    }
}
