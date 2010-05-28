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
/**
* CabException is thrown by classes in the net.sf.jcablib package when format
* errors and other strangeness happen.
* @author David Himelright <a href="mailto:david@litfit.org">david@litfit.org</a>
*/
public
class CabException
extends IOException {
	private static final long serialVersionUID = 1L;
	public CabException() {
		super();
    }
    public CabException(String s) {
		super(s);
    }
}