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

/**
 * 代表了IoSession或IoSession懒惰型。有三种类型的闲置：
 * <ul>
 * <li>{@link #READER_IDLE} - 没有数据是来自远程对等。</li>
 * <li>{@link #WRITER_IDLE} - 会话没有写入任何数据。</li>
 * <li>{@link #BOTH_IDLE} - 读端和写端都空闲.</li>
 * </ul>
 * <p>
 * 为了节约会话资源，可以让用户设置当空闲超过一定时间后关闭此会话，因为此会话可能在某一端出问题了，从而导致另一端空闲超过太长时间。
 * 这可以通过使用IoSessionConfig.setIdleTime(IdleStatus,int)设置，空闲时间阀值在会话配（IoSessionConfig）中设置。
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class IdleStatus {
	/**
	 * 读端空闲： 表示会话状态，没有数据是来自远程对等。
	 */
	public static final IdleStatus READER_IDLE = new IdleStatus("reader idle");

	/**
	 * 写端没有在写数据：表示会话状态，会话时不写任何数据。
	 */
	public static final IdleStatus WRITER_IDLE = new IdleStatus("writer idle");

	/**
	 * 读端和写端都空闲
	 */
	public static final IdleStatus BOTH_IDLE = new IdleStatus("both idle");

	private final String strValue;

	/**
	 * Creates a new instance.
	 */
	private IdleStatus(String strValue) {
		this.strValue = strValue;
	}

	/**
	 * Returns the string representation of this status.
	 * <ul>
	 * <li>{@link #READER_IDLE} - <tt>"reader idle"</tt></li>
	 * <li>{@link #WRITER_IDLE} - <tt>"writer idle"</tt></li>
	 * <li>{@link #BOTH_IDLE} - <tt>"both idle"</tt></li>
	 * </ul>
	 */
	@Override
	public String toString() {
		return strValue;
	}
}