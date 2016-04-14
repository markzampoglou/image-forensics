/*
 * @(#)APP1Data.java
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
 *
 Modified BSD License

Copyright (c) 2015, Jeremy Wood.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* The name of the contributors may not be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package gr.iti.mklab.reveal.forensics.util.ThumbnailExtractor.image.jpeg;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import gr.iti.mklab.reveal.forensics.util.ThumbnailExtractor.io.GuardedInputStream;

class APP1Data {
	
	public static final int TYPE_EXIF = 0;
	public static final int TYPE_XMP = 1;
	public static final int TYPE_UNKNOWN = -1;
	
	BufferedImage thumbnail;
	Hashtable<String, Object> properties = new Hashtable<String, Object>();
	final int type;
	
	static final Comparator<ImageFileDirectory.DirectoryEntry> directoryEntryComparator = new Comparator<ImageFileDirectory.DirectoryEntry>() {
		public int compare(ImageFileDirectory.DirectoryEntry d1, ImageFileDirectory.DirectoryEntry d2) {
			return d1.readLong(d1.fieldValue, 0)-d2.readLong(d2.fieldValue, 0);
		}
	};
	
	APP1Data(JPEGMarkerInputStream in,boolean storeThumbnail) throws IOException {
		byte[] array = new byte[4];
		
		if(in.readFully(array, 4)!=4) {
			throw new IOException();
		}
		
		
		if(array[0]==88 && array[1]==77 && array[2]==80 && array[3]==0) {
			parseXMP(in, storeThumbnail);
			type = TYPE_XMP;
		} else if(array[0]==69 && array[1]==120 && array[2]==105 && array[3]==102) {
			if(in.readFully(array, 2)!=2)
				throw new IOException();
			
			if(array[0]==0 && array[1]==0) {
				parseExif(in, storeThumbnail);
			} else {
				throw new IOException("APP1 expected to begin with \"Exif__\". (Exif"+JPEGMarkerInputStream.toString(array,2)+")");
			}
			type = TYPE_EXIF;
		} else {
			type = TYPE_UNKNOWN;
		}
	}
	
	private void parseXMP(JPEGMarkerInputStream in, boolean storeThumbnail) throws IOException {
		//TODO: after somehow filtering this content we can parse the XML for properties
		//
		//DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		//DocumentBuilder builder = dbfac.newDocumentBuilder();
		//Document dom = builder.parse(mergedIn);
		//Element root = dom.getDocumentElement();
	}
	
	private void parseExif(JPEGMarkerInputStream in, boolean storeThumbnail) throws IOException {
		/** Originally I tried parsing this data in a single pass, but
		 * my testing showed some JPEGs are structured:
		 * 1.  TIFF header
		 * 2.  Specific IFD entry values
		 * 3.  IFD definition referred to in #2.
		 * 
		 * So unfortunately it seems like the safe thing to do now
		 * is to buffer data.
		 */
		BufferedInputStream bufferedIn = new BufferedInputStream(in);
		bufferedIn.mark(in.remainingMarkerLength+1);
		
		TIFFHeader tiffHeader = new TIFFHeader(bufferedIn);
		
		boolean reverse = !tiffHeader.bigEndian;

		ImageFileDirectory ifd = new ImageFileDirectory(bufferedIn, tiffHeader.ifdOffset, reverse);
		ImageFileDirectory ifd0 = ifd;
		ImageFileDirectory ifd1 = null;
		while(ifd!=null) {
			Arrays.sort(ifd.entries, directoryEntryComparator);
			for(int a = 0; a<ifd.entries.length; a++) {
				ifd.entries[a].resolveValue(bufferedIn);
			}
			if(ifd.nextIFDPosition==0) {
				if(ifd!=ifd0)
					ifd1 = ifd;
				ifd = null;
			} else {
				ifd = new ImageFileDirectory(bufferedIn, ifd.nextIFDPosition, reverse);
			}
		}
		
		if(ifd0!=null) {
			for(int a = 0; a<ifd0.entries.length; a++) {
				String name = ifd0.entries[a].getPropertyName();
				if( name==null ) {
					properties.put( Integer.toString(ifd0.entries[a].tagNumber), ifd0.entries[a].value);
				} else {
					properties.put(name, ifd0.entries[a].value);
				}
			}
		}
		
		if(ifd1!=null && storeThumbnail) {
			Number jpegPosition = (Number)ifd1.getProperty(513);
			Number jpegLength = (Number)ifd1.getProperty(514);
			
			/** 2 of my 11,000 JPG's had an IFD for a thumbnail but were
			 * missing tags 513 and 514.  There is a separate unit test
			 * to see if we're missing embedded JPGs, for now let's just
			 * move on if we have null references without a thumbnail.
			 */
			if(jpegPosition!=null && jpegLength!=null) {
				bufferedIn.reset();
				JPEGMarkerInputStream.skipFully(bufferedIn, jpegPosition.longValue());
				bufferedIn.mark(-1);
				
				GuardedInputStream guardedIn = new GuardedInputStream(bufferedIn, jpegLength.longValue(),false);
				thumbnail = ImageIO.read(guardedIn);
			}
		}
	}
	
	/** Returns the properties found in this APP1 block, if any.
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	/** This returns the thumbnail found in this APP1 block if it exists.
	 * 
	 */
	public BufferedImage getThumbnail() {
		return thumbnail;
	}
}
