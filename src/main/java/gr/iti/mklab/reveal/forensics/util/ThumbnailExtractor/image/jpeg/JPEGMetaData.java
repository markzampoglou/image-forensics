/*
 * @(#)JPEGMetaData.java
 *
 * $Date: 2015-03-13 19:39:03 -0400 (Fri, 13 Mar 2015) $
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

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gr.iti.mklab.reveal.forensics.util.ThumbnailExtractor.image.jpeg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/** This class parses JPEG metadata to retrieve properties or
 * thumbnails.
 * 
 * @see com.bric.image.jpeg.JPEGMetaDataDemo
 */
public class JPEGMetaData {
	
	/** Extract a thumbnail from a JPEG file, if possible.
	 * This may return null if no thumbnail can be found.
	 * @throws IOException if an IO problem occurs.
	 */
	public static BufferedImage getThumbnail(File file) throws IOException {
		JPEGMetaData data = new JPEGMetaData(file, true);
		return data.thumbnail;
	}

	Hashtable<String, Object> properties = new Hashtable<String, Object>();
	BufferedImage thumbnail;
	String[] comments = new String[0];
	
	/** Creates a JPEGMetaData object.
	 * 
	 * @param file the JPEG image.
	 * @param fetchThumbnail whether the thumbnail should be retrieved.  If
	 * this is false then <code>getThumbnail()</code> will return false,
	 * but properties will still be loaded (if possible).
	 * @throws IOException
	 */
	public JPEGMetaData(File file,boolean fetchThumbnail) throws IOException {
		InputStream in = null;
		try {
			 in = new FileInputStream(file);
			 init(in, fetchThumbnail);
		} finally {
			if(in!=null) {
				try {
					in.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/** Creates a JPEGMetaData object.
	 * 
	 * @param in the JPEG image.
	 * @param fetchThumbnail whether the thumbnail should be retrieved.  If
	 * this is false then <code>getThumbnail()</code> will return false,
	 * but properties will still be loaded (if possible).
	 * @throws IOException
	 */
	private void init(InputStream in,boolean fetchThumbnail) throws IOException {
		JPEGMarkerInputStream jpegIn = new JPEGMarkerInputStream(in);
		String marker = jpegIn.getNextMarker();
		if(!JPEGMarkerInputStream.START_OF_IMAGE_MARKER.equals(marker)) {
			//did you see "0x4748"? as in the first two letters of "GIF89a"?
			throw new IOException("error: expecting \""+
					JPEGMarkerInputStream.START_OF_IMAGE_MARKER+"\", but found \""+marker+"\"");
		}
		marker = jpegIn.getNextMarker();
		while( marker!=null ) {
			try {
				if( JPEGMarkerInputStream.APP0_MARKER.equals(marker) ) {
					APP0Data data = new APP0Data(jpegIn, fetchThumbnail);
					processAPP0(data);
				} else if( JPEGMarkerInputStream.APP1_MARKER.equals(marker) ) {
					APP1Data data = new APP1Data(jpegIn, fetchThumbnail);
					processAPP1(data);
				} else if( JPEGMarkerInputStream.APP2_MARKER.equals(marker)) {
					APP2Data data = new APP2Data(jpegIn, fetchThumbnail);
					processAPP2(data);
				} else if( JPEGMarkerInputStream.APP13_MARKER.equals(marker)) {
					APP13Data data = new APP13Data(jpegIn, fetchThumbnail);
					processAPP13(data);
				} else if( JPEGMarkerInputStream.COMMENT_MARKER.equals(marker) ) {
					byte[] b = new byte[64];
					StringBuffer buffer = new StringBuffer();
					int t = jpegIn.read(b);
					while(t>0) {
						for(int a = 0; a<t; a++) {
							char c = (char)( b[a] & 0xff);
							buffer.append(c);
						}
						t = jpegIn.read(b);
					}
					processComment( buffer.toString() );
				}
			} catch(Exception e) {
				processException(e, marker);
			}
			if(JPEGMarkerInputStream.START_OF_SCAN_MARKER.equals(marker) ) {
				return;
			}
			marker = jpegIn.getNextMarker();
		}
	}

	/** This is called when an exception occurs trying to parse
	 * an block of data.  The default implementation is simply to
	 * call <code>e.printStackTrace()</code>, but subclasses
	 * can override this as needed.
	 * @param e the exception that occurred.
	 * @param marker the type of marker we were trying to
	 * process.
	 */
	protected void processException(Exception e,String marker) {
		e.printStackTrace();
	}
	
	/** This is called when an <code>APP0Data</code> object
	 * has been parsed.
	 * <P>The default implementation is simply to retrieve
	 * the thumbnail, but subclasses can override this to
	 * do extra work.
	 * @param data the newly parsed data.
	 */
	protected void processAPP0(APP0Data data) {
		considerAddingThumbnail(data.getThumbnail());
	}

	/** This is called when an <code>APP13Data</code> object
	 * has been parsed.
	 * <P>The default implementation is simply to retrieve
	 * the thumbnail, but subclasses can override this to
	 * do extra work.
	 * @param data the newly parsed data.
	 */
	protected void processAPP13(APP13Data data) {
		considerAddingThumbnail(data.getThumbnail());
	}

	/** This is called when an <code>APP2Data</code> object
	 * has been parsed.
	 * <P>The default implementation is simply to retrieve
	 * the thumbnail, but subclasses can override this to
	 * do extra work.
	 * @param data the newly parsed data.
	 */
	protected void processAPP2(APP2Data data) {
		considerAddingThumbnail(data.getThumbnail());
	}

	/** This is called when an <code>APP1Data</code> object
	 * has been parsed.
	 * <P>The default implementation is to retrieve
	 * the thumbnail and add the properties,
	 * but subclasses can override this to
	 * do extra work.
	 * @param data the newly parsed data.
	 */
	protected void processAPP1(APP1Data data) {
		properties.putAll(data.getProperties());
		considerAddingThumbnail(data.getThumbnail());
	}

	/** This is called when a comment marker
	 * has been parsed.
	 * <P>The default implementation is to add this
	 * comment, but subclasses can override this to
	 * do extra work.
	 * @param comment the newly parsed comment.
	 */
	protected void processComment(String comment) {
		String[] newComments = new String[comments.length+1];
		System.arraycopy(comments,0,newComments,0,comments.length);
		newComments[newComments.length-1] = comment;
		comments = newComments;
	}
	
	private void considerAddingThumbnail(BufferedImage bi) {
		if(bi==null) return;
		
		if(thumbnail==null) {
			thumbnail = bi;
			return;
		}
		if(bi.getWidth()>thumbnail.getWidth() && 
				bi.getHeight()>thumbnail.getHeight()) {
			thumbnail = bi;
			return;
		}
	}
}
