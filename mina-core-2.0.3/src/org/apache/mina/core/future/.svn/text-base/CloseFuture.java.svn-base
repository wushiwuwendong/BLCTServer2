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
package org.apache.mina.core.future;

/**
 * 为{@link IoFuture}异步关闭请求。
 * 
 * <h3>Example</h3>
 * 
 * <pre>
 * IoSession session = ...;
 * CloseFuture future = session.close(true);
 * // Wait until the connection is closed
 * future.awaitUninterruptibly();
 * // Now connection should be closed.
 * assert future.isClosed();
 * </pre>
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface CloseFuture extends IoFuture {
	/**
	 * 如果关闭请求完成后，会话被关闭，则返回<tt>true</tt>。
	 */
	boolean isClosed();

	/**
	 * Marks this future as closed and notifies all threads waiting for this
	 * future. This method is invoked by MINA internally. Please do not call
	 * this method directly.
	 */
	void setClosed();

	CloseFuture await() throws InterruptedException;

	CloseFuture awaitUninterruptibly();

	CloseFuture addListener(IoFutureListener<?> listener);

	CloseFuture removeListener(IoFutureListener<?> listener);
}
