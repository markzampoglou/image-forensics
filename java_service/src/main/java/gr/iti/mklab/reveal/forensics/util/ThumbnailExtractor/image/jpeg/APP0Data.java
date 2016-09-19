/*
 * @(#)APP0Data.java
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
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package gr.iti.mklab.reveal.forensics.util.ThumbnailExtractor.image.jpeg;

import java.awt.image.BufferedImage;
import java.io.IOException;

class APP0Data {
	int versionMajor;
	int versionMinor;
	int units;
	int horizontalDensity, verticalDensity;
	int thumbnailWidth, thumbnailHeight;
	BufferedImage thumbnail;
	
	APP0Data(JPEGMarkerInputStream in,boolean storeThumbnail) throws IOException {
		//TODO: also support JFXX ("JFIF Extention")
		//http://en.wikipedia.org/wiki/JPEG_File_Interchange_Format
		//the problem is: I can't find a single file that uses this.
		byte[] array = new byte[9];
		if(in.readFully(array, 5)!=5)
			throw new IOException("APP0 expected to begin with \"JFIF_\".");
		if(array[0]!=74 || array[1]!=70 || array[2]!=73 || array[3]!=70 || array[4]!=0)
			throw new IOException("APP0 expected to begin with \"JFIF_\".");
		if(in.readFully(array, 9)!=9) {
			throw new IOException("APP0 expected to at least 9 bytes of data.");
		}
		setVersionMajor( array[0] & 0xff);
		setVersionMinor( array[1] & 0xff);
		setUnits( array[2] & 0xff );
		setHorizontalDensity( ((array[3] & 0xff) << 16) + (array[4] & 0xff) );
		setVerticalDensity( ((array[5] & 0xff) << 16) + (array[6] & 0xff) );
		setThumbnailWidth( array[7] & 0xff );
		setThumbnailHeight( array[8] & 0xff );
		if( thumbnailWidth*thumbnailHeight>0 && storeThumbnail) {
			//TODO: test this.  I haven't found a single file that uses
			//an APP0 thumbnail, so this code has never been tested.
			byte[] dataByte = new byte[ thumbnailWidth*3 ];
			int[] dataInt = new int[ thumbnailWidth ];
			in.readFully(dataByte, dataByte.length);
			BufferedImage image = new BufferedImage(getThumbnailWidth(), getThumbnailHeight(), BufferedImage.TYPE_INT_RGB);
			for(int y = 0; y<thumbnailHeight; y++) {
				for(int x = 0; x<thumbnailWidth; x++) {
					int r = (dataByte[x*3] & 0xff);
					int g = (dataByte[x*3+1] & 0xff);
					int b = (dataByte[x*3+2] & 0xff);
					dataInt[x] = (r << 16) + (g << 8) + (b);
				}
				image.getRaster().setDataElements(0, y, thumbnailWidth, 1, array);
			}
			setThumbnail(image);
		}
	}
	
	public void setVersionMajor(int versionMajor) {
		this.versionMajor = versionMajor;
	}

	public void setVersionMinor(int versionMinor) {
		this.versionMinor = versionMinor;
	}

	public void setUnits(int units) {
		this.units = units;
	}

	public void setHorizontalDensity(int horizontalDensity) {
		this.horizontalDensity = horizontalDensity;
	}

	public void setVerticalDensity(int verticalDensity) {
		this.verticalDensity = verticalDensity;
	}

	public int getThumbnailWidth() {
		return thumbnailWidth;
	}

	public void setThumbnailWidth(int thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	public int getThumbnailHeight() {
		return thumbnailHeight;
	}

	public void setThumbnailHeight(int thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}

	/** This returns the thumbnail found in this APP0 block if it exists.
	 * 
	 */
	public BufferedImage getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(BufferedImage thumbnail) {
		this.thumbnail = thumbnail;
		setThumbnailWidth( thumbnail.getWidth() );
		setThumbnailHeight( thumbnail.getHeight() );
	}
}
