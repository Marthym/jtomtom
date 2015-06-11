package net.sf.jcablib;
/*
* CabLib, a library for extracting MS cabinets
* Copyright (C) 1999, 2002  David V. Himelright
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Library General Public
* License as published by the Free Software Foundation; either
* version 2 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Library General Public License for more details.
*
* You should have received a copy of the GNU Library General Public
* License along with this library; if not, write to the
* Free Software Foundation, Inc., 59 Temple Place - Suite 330,
* Boston, MA  02111-1307, USA.
*
* David Himelright can be reached at:
* <david@litfit.org> 
*/
import java.io.*;
import java.util.zip.*;
/**
* This class exists because of the strange requirement of MSZIP that every 32k
* chunk of compressed data be followed by an ASCII 'CK'.  
* @author David Himelright <a href="mailto:david@litfit.org">david@litfit.org</a>
*/
public
class MSZipInputStream
extends InflaterInputStream {
	
	/*
	* @param An inputstream that provides MSZIP compressed data
	* @return new MSZipInputStream
	*/
	public MSZipInputStream(InputStream in)
	throws IOException {
		super(in);
		this.inf = new Inflater(true);
		buf = new byte[512];
		in.skip(2);		//skip first 'CK'
	}
	
	/**
	* This method decompresses and returns data stored in the MSZIP format.
	* @param b the buffer into which the data is read
	* @param off the start offset of the data
	* @param len the maximum number of bytes read
	* @return the actual number of bytes read, or -1 if the end of the
	*		 compressed input is reached or a preset dictionary is needed
	* @exception CabException if a MSZIP format error has occurred
	* @exception IOException if an I/O error has occurred
	*/
	public int read(byte[] b, int off, int len) throws IOException {
		if (len <= 0) {
			return 0;
		}
		
		int n;
		
		try {
			while ((n = inf.inflate(b, off, len)) == 0) {
				if(inf.needsDictionary()) {
					return -1;
				}
				if(inf.finished()) {
					//try to continue and refill the buffer
					if(inf.getTotalOut() == 32768)
						seekCK();
					else
						return -1;
				}
				if(inf.needsInput()) {
					fill();
				}
			}
		} catch (DataFormatException e) {
			String s = e.getMessage();
			throw new CabException(s != null ? s : "Invalid ZLIB data format");
		}
		
		return n;
	}
	
	/**
	* Copy remaining bytes to buffer, copy buffer to main buffer
	* @exception IOException if an I/O error has occurred
	* @exception CabException thrown if it can't get more than 8k of input
	*/
	protected void seekCK()
	throws IOException {
		int 	offset = buf.length - inf.getRemaining();
		byte[]	temp = new byte[inf.getRemaining()];
		
		System.arraycopy(buf, offset, temp, 0, temp.length);
		System.arraycopy(temp, 0, buf, 0, temp.length);
		
		len = temp.length + in.read(buf, temp.length, offset);
		
		if(len < 8)
			throw new CabException("MSZipInputStream: Blocked or sloooooow file IO");
								
		for(int i=0; i<(buf.length-2); i++) {
			if(buf[i] == (byte)'C' &&
				buf[(i + 1)] == (byte)'K') {
				inf.reset();
				inf.setInput(buf, i+2, buf.length - (i+2));
			}
		}
	}
	
	/**
	* Refills the input buffer with data from the input file.
	* @exception IOException if an I/O error has occurred
	*/
	protected void fill() throws IOException {
		len = in.read(buf, 0, buf.length);
		if (len == -1) {
			throw new EOFException("Unexpected end of ZLIB input stream");
		}
		inf.setInput(buf, 0, len);
	}
}