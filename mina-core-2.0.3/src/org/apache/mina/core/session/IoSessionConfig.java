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

import java.util.concurrent.BlockingQueue;

/**
 * 用于表示会话的配置信息，主要包括：读缓冲区大小，会话数据吞吐量，计算吞吐量时间间隔，指定会话端的空闲时间，写请求操作超时时间。 The
 * configuration of {@link IoSession}.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface IoSessionConfig {

	/**
	 * Returns the size of the read buffer that I/O processor allocates per each
	 * read. It's unusual to adjust this property because it's often adjusted
	 * automatically by the I/O processor.
	 */
	int getReadBufferSize();

	/**
	 * Sets the size of the read buffer that I/O processor allocates per each
	 * read. It's unusual to adjust this property because it's often adjusted
	 * automatically by the I/O processor.
	 */
	void setReadBufferSize(int readBufferSize);

	/**
	 * Returns the minimum size of the read buffer that I/O processor allocates
	 * per each read. I/O processor will not decrease the read buffer size to
	 * the smaller value than this property value.
	 */
	int getMinReadBufferSize();

	/**
	 * Sets the minimum size of the read buffer that I/O processor allocates per
	 * each read. I/O processor will not decrease the read buffer size to the
	 * smaller value than this property value.
	 */
	void setMinReadBufferSize(int minReadBufferSize);

	/**
	 * Returns the maximum size of the read buffer that I/O processor allocates
	 * per each read. I/O processor will not increase the read buffer size to
	 * the greater value than this property value.
	 */
	int getMaxReadBufferSize();

	/**
	 * Sets the maximum size of the read buffer that I/O processor allocates per
	 * each read. I/O processor will not increase the read buffer size to the
	 * greater value than this property value.
	 */
	void setMaxReadBufferSize(int maxReadBufferSize);

	/**
	 * Returns the interval (seconds) between each throughput calculation. The
	 * default value is <tt>3</tt> seconds.
	 */
	int getThroughputCalculationInterval();

	/**
	 * Returns the interval (milliseconds) between each throughput calculation.
	 * The default value is <tt>3</tt> seconds.
	 */
	long getThroughputCalculationIntervalInMillis();

	/**
	 * Sets the interval (seconds) between each throughput calculation. The
	 * default value is <tt>3</tt> seconds.
	 */
	void setThroughputCalculationInterval(int throughputCalculationInterval);

	/**
	 * Returns idle time for the specified type of idleness in seconds.
	 */
	int getIdleTime(IdleStatus status);

	/**
	 * Returns idle time for the specified type of idleness in milliseconds.
	 */
	long getIdleTimeInMillis(IdleStatus status);

	/**
	 * Sets idle time for the specified type of idleness in seconds.
	 */
	void setIdleTime(IdleStatus status, int idleTime);

	/**
	 * Returns idle time for {@link IdleStatus#READER_IDLE} in seconds.
	 */
	int getReaderIdleTime();

	/**
	 * Returns idle time for {@link IdleStatus#READER_IDLE} in milliseconds.
	 */
	long getReaderIdleTimeInMillis();

	/**
	 * Sets idle time for {@link IdleStatus#READER_IDLE} in seconds.
	 */
	void setReaderIdleTime(int idleTime);

	/**
	 * Returns idle time for {@link IdleStatus#WRITER_IDLE} in seconds.
	 */
	int getWriterIdleTime();

	/**
	 * Returns idle time for {@link IdleStatus#WRITER_IDLE} in milliseconds.
	 */
	long getWriterIdleTimeInMillis();

	/**
	 * Sets idle time for {@link IdleStatus#WRITER_IDLE} in seconds.
	 */
	void setWriterIdleTime(int idleTime);

	/**
	 * Returns idle time for {@link IdleStatus#BOTH_IDLE} in seconds.
	 */
	int getBothIdleTime();

	/**
	 * Returns idle time for {@link IdleStatus#BOTH_IDLE} in milliseconds.
	 */
	long getBothIdleTimeInMillis();

	/**
	 * Sets idle time for {@link IdleStatus#WRITER_IDLE} in seconds.
	 */
	void setBothIdleTime(int idleTime);

	/**
	 * Returns write timeout in seconds.
	 */
	int getWriteTimeout();

	/**
	 * Returns write timeout in milliseconds.
	 */
	long getWriteTimeoutInMillis();

	/**
	 * Sets write timeout in seconds.
	 */
	void setWriteTimeout(int writeTimeout);

	/**
	 * Returns <tt>true</tt> if and only if {@link IoSession#read()} operation
	 * is enabled. If enabled, all received messages are stored in an internal
	 * {@link BlockingQueue} so you can read received messages in more
	 * convenient way for client applications. Enabling this option is not
	 * useful to server applications and can cause unintended memory leak, and
	 * therefore it's disabled by default.
	 */
	boolean isUseReadOperation();

	/**
	 * 设置IoSession的read方法是否启用，若启用的话，则所有接收到的消息都会存储在内部的一个阻塞队列中，好处在于可以更方便用户对信息的处理，
	 * 但对于某些应用来说并不管用，而且还会造成内存泄露，因此默认情况下这个选项是不开启的。
	 * 启用或禁用IoSession.read（）操作。如果启用，所有收到的邮件
	 * 储存在内部的BlockingQueue所以你可以阅读更方便地获得客户端应用程序的消息
	 * 。启用此选项是没有用的服务器应用程序，并可能导致意想不到的内存泄漏，因此它的默认禁用。
	 */
	void setUseReadOperation(boolean useReadOperation);

	/**
	 * Sets all configuration properties retrieved from the specified
	 * <tt>config</tt>.
	 */
	void setAll(IoSessionConfig config);
}
