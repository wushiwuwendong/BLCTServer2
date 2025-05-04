/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.mina.filter.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

/**
 * 
 * 这个解码器用于将任何一个解码器包装为一 个线程安全的解码器，用于解决上面说的每 次执行 decode()方法时可能线程不是上一次
 * 的线程的问题，但这样会在高并发时，大大 降低系统的性能。
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class SynchronizedProtocolDecoder implements ProtocolDecoder {
	private final ProtocolDecoder decoder;

	/**
	 * Creates a new instance which decorates the specified <tt>decoder</tt>.
	 */
	public SynchronizedProtocolDecoder(ProtocolDecoder decoder) {
		if (decoder == null) {
			throw new IllegalArgumentException("decoder");
		}
		this.decoder = decoder;
	}

	/**
	 * Returns the decoder this decoder is decorating.
	 */
	public ProtocolDecoder getDecoder() {
		return decoder;
	}

	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		synchronized (decoder) {
			decoder.decode(session, in, out);
		}
	}

	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
		synchronized (decoder) {
			decoder.finishDecode(session, out);
		}
	}

	public void dispose(IoSession session) throws Exception {
		synchronized (decoder) {
			decoder.dispose(session);
		}
	}
}
