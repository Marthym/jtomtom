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
import java.util.*;
import java.io.File;
/**
* This class represents the folder structure found in cabinets which 
* is a grouping of files compressed as one contiguous block of data.
* @author David Himelright <a href="mailto:david@litfit.org">david@litfit.org</a>
*/
public class CabFolder implements CabConstants {
	private short			mMethod,		//compression method
							mData;
	private int				mOffset,		//offset
							mInflatedSize,
							mFolderNum;
	private List<CabEntry>	mEntries;
	private File			mFile;
	
	public CabFolder(File inFile, int inOffset, short inData, short inMethod, int inFolderNum) {
		mFile = inFile;
		mOffset = inOffset;
		mData = inData;
		mMethod = inMethod;
		mFolderNum = inFolderNum;
		mInflatedSize = 0;
		mEntries = new ArrayList<CabEntry>();
	}
	
	public void addEntry(CabEntry inEntry) {
		mEntries.add(inEntry);
		mInflatedSize += inEntry.getInflatedSize();
	}
	
	
	/**
	* @return Iterator of the CabEntries in this CabFolder.
	*/
	public final Iterator<CabEntry> iterator() {
		return mEntries.iterator();
	}
	
	public CabEntry[] getEntries() {
		CabEntry[] result = new CabEntry[mEntries.size()];
		mEntries.toArray(result);
		return result;
	}
	
	/**
	* @return The compression method for this folder as defined in CabConstants.
	*/
	public final short getMethod() {
		return (short)(mMethod & 0x000F);
	}
	
	/**
	* @return The offset of this CabFolder in the File it is stored in.
	*/
	public final int getOffset() {
		return mOffset;
	}

	public final int getData() {
		return mData;
	}

	/**
	* @return Total size of all entries in this folder when inflated.
	*/
	public final int getInflatedSize() {
		return mInflatedSize;
	}
	
	/**
	* @return The File this CabFolder is stored in.
	*/
	public final File getArchiveFile() {
		return mFile;
	}
	
	public final String getName() {
		return "Folder #" + mFolderNum;
	}
	
	public final short getDate() {
		return mData;
	}
}