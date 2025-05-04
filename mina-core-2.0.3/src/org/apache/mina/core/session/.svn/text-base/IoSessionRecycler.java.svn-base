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
package org.apache.mina.core.session;

import java.net.SocketAddress;

/**
 * 负责回收不再使用的会话的接口:
 * 为一个无连接的传输服务提供回收现有会话的服务， 连接传输可以通过指定IoSessionRecycler回收到IoService现有的会话。
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a> TODO More
 *         documentation
 */
public interface IoSessionRecycler {
	/**
	 * 一个虚拟回收，不回收任何会话。使用这种回收将尽一切会话的生命周期为每一个I / O的连接会话的所有发射活动。
	 */
	static IoSessionRecycler NOOP = new IoSessionRecycler() {
		public void put(IoSession session) {
			// Do nothing
		}

		public IoSession recycle(SocketAddress localAddress, SocketAddress remoteAddress) {
			return null;
		}

		public void remove(IoSession session) {
			// Do nothing
		}
	};

	/**
	 * 底层传输时调用创建或写入一个新的IoSession。
	 * 
	 * @param session
	 *            the new {@link IoSession}.
	 */
	void put(IoSession session);

	/**
	 * 试图检索一个要回收的IoSession。
	 * 
	 * @param localAddress
	 *            该IoSession传输要回收的本地套接字地址
	 * @param remoteAddress
	 *            该IoSession传输要回收的远程套接字地址。
	 * @return 被回收的IoSession，或NULL如果不能找到。
	 */
	IoSession recycle(SocketAddress localAddress, SocketAddress remoteAddress);

	/**
	 * 当调用一个IoSession明确关闭。
	 * 
	 * @param session
	 *            the new {@link IoSession}.
	 */
	void remove(IoSession session);
}
