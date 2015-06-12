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
import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
* This object represents a file inside a Cabinet archive.
* @author David Himelright <a href="mailto:david@litfit.org">david@litfit.org</a>
*/
public
class CabEntry
implements CabConstants {
	
	private String		mName;
	private int			mInflatedSize, //inflated_size,
						mInflatedOffset, //inflated_offset,
						mTimestamp;
	private short		mFolderIndex,				// file control id (CAB_FILE_*)
						mAttributes;			// file attributes (CAB_ATTRIB_*)
	private CabFolder	mCabFolder;
	
	
	public CabEntry(String inName, int inSize, int inOffset,
					int inTimestamp, short inFolderIndex, short inAttributes,
					CabFolder inCabFolder) {
			
		mName = inName;
		mInflatedSize = inSize;
		mInflatedOffset = inOffset;
		mTimestamp = inTimestamp;
		mFolderIndex = inFolderIndex;	//		iFolder
		mAttributes = inAttributes;
		mCabFolder = inCabFolder;
		
		mCabFolder.addEntry(this);
	}
	
	/**
	* @return The name of the file this entry refers to, a DOS path.
	*/
	public String getName() {
		return mName;
	}
	
	/**
	* @return Size of this entry when inflated
	*/
	public int getInflatedSize() {
		return mInflatedSize;
	}
	
	/**
	* @return The compression method for this entry's folder as defined in CabConstants.
	*/
	public short getMethod() {
		return mCabFolder.getMethod();
	}
	
	
	public Date getTimestamp() {
		return new Date(dosToJavaTime((long)mTimestamp));
	}
	
	/**
	* @return The contents of the attributes field as a short.
	*/
	public short getAttributes() {
		return mAttributes;
	}
	
	/**
	* @return The CabFolder which contains this entry.
	*/
	public CabFolder getCabFolder() {
		return mCabFolder;
	}
	
	public short getFolderIndex() {
		return mFolderIndex;
	}
	
	/**
	* @return The offset of this entry's file in this entry's folder when inflated.
	*/
	public int getOffset() {
		return mInflatedOffset;
	}
	
	/**
	* @return True if compresson scheme is supported by this library.
	
	public boolean canExtract() {
		return folder.canExtract();
	}*/
	
	/**
	* @return The file this entry is stored in.
	*/
	public File getArchiveFile() {
		return mCabFolder.getArchiveFile();
	}
	
	/**
	 * Converts DOS time to Java time (number of milliseconds since epoch)
	 * @param dosTime
	 * @return
	 */
	private static final long dosToJavaTime(long dosTime) {
		Calendar cal = Calendar.getInstance();
	 	cal.set(Calendar.YEAR, (int) ((dosTime >> 25) & 0x7f) + 1980);
	 	cal.set(Calendar.MONTH, (int) ((dosTime >> 21) & 0x0f) - 1);
	 	cal.set(Calendar.DATE, (int) (dosTime >> 16) & 0x1f);
	 	cal.set(Calendar.HOUR_OF_DAY, (int) (dosTime >> 11) & 0x1f);
	 	cal.set(Calendar.MINUTE, (int) (dosTime >> 5) & 0x3f);
	 	cal.set(Calendar.SECOND, (int) (dosTime << 1) & 0x3e);
	 	cal.set(Calendar.MILLISECOND, 0);
	 	
	 	return cal.getTimeInMillis(); 
	}
	
	
	/**
	 * Converts Java time to DOS time
	 * @param time
	 * @return
	 */
    @SuppressWarnings("unused")
	private static final long javaToDosTime(long time) {
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(time);
    	
    	int year = cal.get(Calendar.YEAR); 
    	if (year < 1980) {
    	    return (1 << 21) | (1 << 16);
    	}
    	return (year - 1980) << 25 
    		| (cal.get(Calendar.MONTH) + 1) << 21 
    		| cal.get(Calendar.DAY_OF_MONTH) << 16 
    		| cal.get(Calendar.HOUR_OF_DAY) << 11 
    		| cal.get(Calendar.MINUTE) << 5 
    		| cal.get(Calendar.SECOND) >> 1;
    }

}