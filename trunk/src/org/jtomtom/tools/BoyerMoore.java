package org.jtomtom.tools;

import java.io.IOException;
import java.io.InputStream;

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
