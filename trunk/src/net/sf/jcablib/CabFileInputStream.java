package net.sf.jcablib;
/*
* CabLib, a library for extracting MS cabinets
* Copyright (C) 1999, 2002, 2010  David V. Himelright, Frédéric Combes
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
import java.nio.ByteBuffer;
/**
* CabFileInputStream mimics the ZipFileInputStream interface.
* @author David Himelright <a href="mailto:david@litfit.org">david@litfit.org</a>
*/
public
class CabFileInputStream extends FilterInputStream implements CabConstants {
	
	private long 		count;	//bytes available in this stream
	private long		filesize;
	
	private long		cfdataCount;	// Taille restante avant la fin du CFData
	
	/**
	* @param ce the CabEntry to return data from
	* @exception CabException thrown if the file's compression format isn't supported
	* @exception IOException thrown by various file IO routines
	*/
	CabFileInputStream(CabEntry ce) throws IOException {
		super(new FileInputStream(ce.getArchiveFile()));
		
		count = ce.getInflatedSize();	//compressed size unavailable in Cabinet
		filesize = count;
		String error = "";
		
		switch(ce.getMethod()) {
			case kNoCompression:
				
				break;
				
			case kMszipCompression:
				error = "Unsupported compression scheme: Zip";
			case kInvalidFolder:
				error = "Invalid compression flag, unable to continue.";
			case kQuantumCompression:
				error = "Unsupported compression scheme: Quantum";
			case kLzxCompression:
				error = "Unsupported compression scheme: LZX";
			default:
				throw new CabException(error);
		}
		
		in.skip(ce.getCabFolder().getOffset());
		
		// Là on est au début du CFDATA, il faut le lire pour savoir jusqu'où on peut lire
		long cbDataRead = 0;
		while (cbDataRead <= ce.getOffset()) {
			in.skip(cfdataCount);  // On va au prochain CFDATA
			readCFData();
			cbDataRead += cfdataCount;
		}
		
		// On se place au début du fichier
		cbDataRead = ce.getOffset() - (cbDataRead - cfdataCount);
		in.skip(cbDataRead);
	}
	
	
	/**
	* Creates an InputStream for reading the contents of the specified CabFolder
	* @param cf A CabFolder to read
	* @exception CabException thrown if the file's compression format isn't supported
	* @exception IOException thrown by various file IO routines
	*/
	public CabFileInputStream(CabFolder cf) throws IOException {
		super(new FileInputStream(cf.getArchiveFile()));
		
		count = cf.getInflatedSize();	//compressed size unavailable in Cabinet
		FileInputStream fis = new FileInputStream(cf.getArchiveFile());
		String error = "";
		
		switch(cf.getMethod()) {
			case kNoCompression:
				break;
				
			case kMszipCompression:
				// Ca je suis pas sur que ça soit terrible
				in = new MSZipInputStream(fis);
				break;
				
			case kInvalidFolder:
				error = "Invalid compression flag, unable to continue.";
			case kQuantumCompression:
				error = "Unsupported compression scheme: Quantum";
			case kLzxCompression:
				error = "Unsupported compression scheme: LZX";
			default:
				throw new CabException(error);
		}
		
		in.skip(cf.getOffset());
		
		in.skip(cf.getData()*8);
	}
	
	
	/**
	 * Lit le CFDATA pour trouver la taille du prochain bloc
	 * @throws IOException
	 */
	private void readCFData() throws IOException {
		in.skip(4); 			// Ca c'est le checksum, pour l'instant on s'en fout
		byte[] buffer = new byte[2];
		in.read(buffer);
		cfdataCount = Math.abs(Short.reverseBytes(ByteBuffer.wrap(buffer).getShort()));
		in.skip(2);	// cbUncomp utilisé seulement pour les compressé
	}

	/**
	* @return Number of bytes available in this stream.
	*/
	public int available() {
		return (int)Math.min(count, Integer.MAX_VALUE);
	}


	@Override
	public int read() throws IOException {
		if (count <= 0) return -1;
		
		if (cfdataCount <= 0) {
			readCFData();
		}
		
		int ln = super.read();
		count--;
		cfdataCount--;
		return ln;
	}


	@Override
	public int read(byte[] buffer, int offset, int len) throws IOException {
		if (count <= 0) return -1;

		if (len > count) {
			len = (int)count;
		}
		
		if (len > cfdataCount) {
			len = (int)cfdataCount;
			readCFData();
		}

		int ln = super.read(buffer, offset, len);
		count -= ln;
		cfdataCount -= ln;
		
		return ln;
	}

	@Override
	public synchronized void reset() throws IOException {
		count = filesize;
		super.reset();
	}


	@Override
	public long skip(long arg0) throws IOException {
		long ln = super.skip(arg0);
		count -= ln;
		cfdataCount -= ln;
		return ln;
	}
	
}