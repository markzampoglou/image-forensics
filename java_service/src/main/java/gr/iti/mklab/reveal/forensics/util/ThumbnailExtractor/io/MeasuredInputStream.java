/*
 * @(#)MeasuredInputStream.java
 *
 * $Date: 2014-10-25 17:13:48 -0400 (Sat, 25 Oct 2014) $
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
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gr.iti.mklab.reveal.forensics.util.ThumbnailExtractor.io;

import java.io.IOException;
import java.io.InputStream;

/** This <code>InputStream</code> relays information from an underlying
 * <code>InputStream</code> while measuring how many bytes have been
 * read or skipped.
 * <P>Note marking is not supported in this object.
 */
public class MeasuredInputStream extends InputStream {
	InputStream in;
	long read = 0;
	boolean closeable = true;
	
	public MeasuredInputStream(InputStream i) {
		this.in = i;
	}
	
	/** Control whether calling <code>close()</code> affects
	 *  the underlying InputStream. This is useful in cases when you pass
	 *  an InputStream to a 3rd party decoder that helpfully tries to close
	 *  the stream as it wraps up, but there is still data to be read later (such
	 *  as when working with a ZipInputStream).
	 * @param b whether calling <code>close()</code> will close
	 * the underlying InputStream.
	 */
	public void setCloseable(boolean b) {
		closeable = b;
	}
	
	public boolean isCloseable() {
		return closeable;
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}
	
	/** Returns the number of bytes that have been read (or skipped).
	 * 
	 * @return the number of bytes that have been read (or skipped).
	 */
	public long getReadBytes() {
		return read;
	}

	@Override
	public void close() throws IOException {
		if(closeable)
			in.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		throw new RuntimeException();
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int read() throws IOException {
		int k = in.read();
		if(k==-1)
			return -1;
		read++;
		return k;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int returnValue = in.read(b, off, len);
		if(returnValue==-1)
			return -1;
		read += returnValue;
		
		return returnValue;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b,0,b.length);
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new RuntimeException();
	}

	@Override
	public long skip(long n) throws IOException {
		long returnValue = in.skip(n);
		if(returnValue==-1)
			return -1;
		read += returnValue;
		
		return returnValue;
	}

	/** This skips forward to the requested position.
	 * If the requested position is less than the current
	 * position, then an exception is thrown.
	 * @param pos
	 * @throws IOException
	 */
	public void seek(long pos) throws IOException {
		if(pos==read) return;
		if(pos<read) throw new IOException("Cannot seek backwards.");
		skip(pos-read);
	}
}
