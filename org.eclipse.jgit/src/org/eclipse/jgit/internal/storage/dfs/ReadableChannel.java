/*
 * Copyright (C) 2011, Google Inc.
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.eclipse.jgit.internal.storage.dfs;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Readable random access byte channel from a file.
 */
public interface ReadableChannel extends ReadableByteChannel {
	/**
	 * Get the current position of the channel.
	 *
	 * @return r current offset.
	 * @throws java.io.IOException
	 *             the channel's current position cannot be obtained.
	 */
	long position() throws IOException;

	/**
	 * Seek the current position of the channel to a new offset.
	 *
	 * @param newPosition
	 *            position to move the channel to. The next read will start from
	 *            here. This should be a multiple of the {@link #blockSize()}.
	 * @throws java.io.IOException
	 *             the position cannot be updated. This may be because the
	 *             channel only supports block aligned IO and the current
	 *             position is not block aligned.
	 */
	void position(long newPosition) throws IOException;

	/**
	 * Get the total size of the channel.
	 * <p>
	 * Prior to reading from a channel the size might not yet be known.
	 * Implementors may return -1 until after the first read method call. Once a
	 * read has been completed, the underlying file size should be available.
	 *
	 * @return r total size of the channel; -1 if not yet available.
	 * @throws java.io.IOException
	 *             the size cannot be determined.
	 */
	long size() throws IOException;

	/**
	 * Get the recommended alignment for reads.
	 * <p>
	 * Starting a read at multiples of the blockSize is more efficient than
	 * starting a read at any other position. If 0 or -1 the channel does not
	 * have any specific block size recommendation.
	 * <p>
	 * Channels should not recommend large block sizes. Sizes up to 1-4 MiB may
	 * be reasonable, but sizes above that may be horribly inefficient. The
	 * {@link org.eclipse.jgit.internal.storage.dfs.DfsBlockCache} favors the
	 * alignment suggested by the channel rather than the configured size under
	 * the assumption that reads are very expensive and the channel knows what
	 * size is best to access it with.
	 *
	 * @return recommended alignment size for randomly positioned reads. Does
	 *         not need to be a power of 2.
	 */
	int blockSize();

	/**
	 * Recommend the channel maintain a read-ahead buffer.
	 * <p>
	 * A read-ahead buffer of approximately {@code bufferSize} in bytes may be
	 * allocated and used by the channel to smooth out latency for read.
	 * <p>
	 * Callers can continue to read in smaller than {@code bufferSize} chunks.
	 * With read-ahead buffering enabled read latency may fluctuate in a pattern
	 * of one slower read followed by {@code (bufferSize / readSize) - 1} fast
	 * reads satisfied by the read-ahead buffer. When summed up overall time to
	 * read the same contiguous range should be lower than if read-ahead was not
	 * enabled, as the implementation can combine reads to increase throughput.
	 * <p>
	 * To avoid unnecessary IO callers should only enable read-ahead if the
	 * majority of the channel will be accessed in order.
	 * <p>
	 * Implementations may chose to read-ahead using asynchronous APIs or
	 * background threads, or may simply aggregate reads using a buffer.
	 * <p>
	 * This read ahead stays in effect until the channel is closed or the buffer
	 * size is set to 0.
	 *
	 * @param bufferSize
	 *            requested size of the read ahead buffer, in bytes.
	 * @throws java.io.IOException
	 *             if the read ahead cannot be adjusted.
	 */
	void setReadAheadBytes(int bufferSize) throws IOException;
}
