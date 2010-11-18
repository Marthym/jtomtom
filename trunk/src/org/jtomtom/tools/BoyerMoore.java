/**
 *  Copyright (C) 2010  Frédéric Combes
 *  This file is part of jTomtom.
 *
 *  jTomtom is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  jTomtom is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jTomtom.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Frédéric Combes can be reached at:
 *  <belz12@yahoo.fr> 
 */
package org.jtomtom.tools;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Frédéric Combes
 *
 * Search Algorithm from Bob Boyer & J Strother Moore
 */
public class BoyerMoore {
	public static final int BUFFER_SIZE = 32;

	public static int [] buildShiftArray(byte [] byteSequence){
	        int [] shifts = new int[byteSequence.length];
	        int [] ret;
	        int shiftCount = 0;
	        byte end = byteSequence[byteSequence.length-1];
	        int index = byteSequence.length;
	        int shift = 1;

	        while(--index >= 0){
	                if(byteSequence[index] == end){
	                        shifts[shiftCount++] = shift;
	                        shift = 1;
	                } else {
	                        shift++;
	                }
	        }
	        ret = new int[shiftCount];
	        for(int i = 0;i < shiftCount;i++){
	                ret[i] = shifts[i];
	        }
	        return ret;
	}

	public static byte [] flushBuffer(byte [] buffer, int keepSize){
	        byte [] newBuffer = new byte[buffer.length];
	        for(int i = 0;i < keepSize;i++){
	                newBuffer[i] = buffer[buffer.length - keepSize + i];
	        }
	        return newBuffer;
	}

	public static int findBytes(byte [] haystack, int haystackSize, byte [] needle, int [] shiftArray){
	        int index = needle.length;
	        int searchIndex, needleIndex, currentShiftIndex = 0;
	        boolean shiftFlag = false;

	        index = needle.length;
	        while(true){
	                needleIndex = needle.length-1;
	                while(true){
	                        if(index >= haystackSize)
	                                return -1;
	                        if(haystack[index] == needle[needleIndex])
	                                break;
	                        index++;
	                }
	                searchIndex = index;
	                needleIndex = needle.length-1;
	                while(needleIndex >= 0 && haystack[searchIndex] == needle[needleIndex]){
	                        searchIndex--;
	                        needleIndex--;
	                }
	                if(needleIndex < 0)
	                        return index-needle.length+1;
	                if(shiftFlag){
	                        shiftFlag = false;
	                        index += shiftArray[0];
	                        currentShiftIndex = 1;
	                } else if(currentShiftIndex >= shiftArray.length){
	                        shiftFlag = true;
	                        index++;
	                } else{
	                        index += shiftArray[currentShiftIndex++];
	                }                       
	        }
	}

	public static int findBytes(InputStream stream, byte [] needle){
	        byte [] buffer = new byte[BUFFER_SIZE];
	        int [] shiftArray = buildShiftArray(needle);
	        int bufferSize;
	        int offset = 0, init = needle.length;
	        int val;

	        try{
	                while(true){
	                        bufferSize = stream.read(buffer, needle.length-init, buffer.length-needle.length+init);
	                        if(bufferSize == -1)
	                                return -1;
	                        if((val = findBytes(buffer, bufferSize+needle.length-init, needle, shiftArray)) != -1)
	                                return val+offset;
	                        buffer = flushBuffer(buffer, needle.length);
	                        offset += bufferSize-init;
	                        init = 0;
	                }
	        } catch (IOException e){
	                e.printStackTrace();
	        }
	        return -1;
	}

}
