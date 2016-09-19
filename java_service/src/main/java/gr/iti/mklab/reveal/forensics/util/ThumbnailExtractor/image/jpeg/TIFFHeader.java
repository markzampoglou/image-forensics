/*
 * @(#)TIFFHeader.java
 *
 * $Date: 2014-03-13 04:15:48 -0400 (Thu, 13 Mar 2014) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 *
Modified BSD License

Copyright (c) 2015, Jeremy Wood.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* The name of the contributors may not be used to endorse or promote products derived from this software without specific prior written permission.
*
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package gr.iti.mklab.reveal.forensics.util.ThumbnailExtractor.image.jpeg;

import java.io.IOException;
import java.io.InputStream;

class TIFFHeader {
	boolean bigEndian;
	int ifdOffset;
	
	TIFFHeader(InputStream in) throws IOException {
		byte[] array = new byte[4];
		if(JPEGMarkerInputStream.readFully(in, array, 2, false)!=2) {
			throw new IOException("Incomplete TIFF Header");
		}
		
		if(array[0]==73 && array[1]==73) { //little endian
			bigEndian = false;
		} else if(array[0]==77 && array[1]==77) { //big endian
			bigEndian = true;
		} else {
			throw new IOException("Unrecognized endian encoding.");
		}
		

		if(JPEGMarkerInputStream.readFully(in, array, 2, !bigEndian)!=2) {
			throw new IOException("Incomplete TIFF Header");
		}
		if(!(array[0]==0 && array[1]==42)) { //required byte in TIFF header
			throw new IOException("Missing required identifier 0x002A.");
		}
		

		if(JPEGMarkerInputStream.readFully(in, array, 4, !bigEndian)!=4) {
			throw new IOException("Incomplete TIFF Header");
		}
		ifdOffset = ((array[0] & 0xff) << 24) + ((array[1] & 0xff) << 16) +
					((array[2] & 0xff) << 8) + ((array[3] & 0xff) << 0) ;
	}
	
	/** The length of this TIFF header. */
	int getLength() {
		return 8;
	}
}
