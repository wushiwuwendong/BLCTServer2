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
import java.util.Set;

import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;

/**
 * IoSession接口与底层的传输层类型无关（也就是不管是TCP还是UDP）,它表示通信双端的连接。它提供用户自定义属性，
 * 可以用于在过滤器和处理器之间交换用户自定义协议相关的信息。
 * 每个会话都有一个Service为之提供服务，同时有一个Handler负责此会话的I/O事件处理。
 * 最重要的两个方法是read和write，这两个方法都是异步执行
 * ，若要真正完成必须在其返回结果上进行等待。关闭会话的方法close是异步执行的，也就是应当等待返回的CloseFuture
 * ，此外，还有另一种关闭方式closeOnFlush
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface IoSession {
	/**
	 * @return 一本次会话的唯一标识符。每个会话都有自己的ID，这是各不相同。
	 * 
	 *         TODO : 它的实施方法并不能保证合同是尊重。它使用的hashCode（）方法，该方法不保证唯一性的关键。
	 */
	long getId();

	/**
	 * @return 为本次会话提供 I/O服务的IoService对象.
	 */
	IoService getService();

	/**
	 * @return 这次会话的IoHandler对象.
	 */
	IoHandler getHandler();

	/**
	 * @return 本次会话的配置对象.
	 */
	IoSessionConfig getConfig();

	/**
	 * @return 过滤器链，只影响本次会话.
	 */
	IoFilterChain getFilterChain();

	/**
	 * TODO 添加方法的文档
	 */
	WriteRequestQueue getWriteRequestQueue();

	/**
	 * @return 本次会话的元数据的传输.
	 */
	TransportMetadata getTransportMetadata();

	/**
	 * TODO 这是错误的javadoc。返回的标签要短。
	 * 
	 * @return 这是一个新的通知时，收到消息{@link ReadFuture}， 连接关闭或异常被捕获。
	 *         此操作是特别有用当你实现一个客户端应用程序。 TODO：在这里描述如何启用此功能。 不过，请注意，此操作默认情况下禁用并抛出
	 *         {@link IllegalStateException}，因为所有接收到的事件，必须排队的地方，以支持该操作，
	 *         可能导致内存泄漏。 这意味着你必须保持调用{@link #read()}一旦你启用此操作。 
	 *         为了使这次 为true。请用真实{@link IoSessionConfig#setUseReadOperation(boolean)}
	 * 
	 * @throws IllegalStateException
	 *             if {@link IoSessionConfig#setUseReadOperation(boolean)
	 *             useReadOperation} option has not been enabled.
	 */
	ReadFuture read();

	/**
	 * 写入指定的消息异地同行。 此操作是异步的; {@link IoHandler#messageSent(IoSession, Object)}
	 * 将被调用时，消息实际发送到远程节点 。如果你想等待实际写的消息，你也可以等待返回{@link WriteFuture}。
	 */
	WriteFuture write(Object message);

	/**
	 * （可选）指定的<tt>message</tt>写入到指定的目的地。此操作是异步的;
	 * {@link IoHandler#messageSent(IoSession, Object)}将被调用时，消息实际发送到远程节点
	 * 。如果你想等待实际写的消息，你也可以等待返回{@link WriteFuture}。
	 * <p>
	 * 当你实现一个客户端，它接收来自DHCP服务器，如服务器的广播消息，客户可能需要发送广播消息的服务器发送的响应消息。
	 * 由于会议的远程地址是不是在广播的情况下服务器的地址，
	 * 应该有一种方法来指定目标时，你写的响应消息。此接口提供写（对象，SocketAddress的）方法，以便您可以指定的目的地。
	 * 
	 * @param destination
	 *            <tt>null</tt> 如果你想要的信息发送到默认的远程地址
	 * 
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported
	 */
	WriteFuture write(Object message, SocketAddress destination);

	/**
	 * 本次会话结束后立即刷新所有排队的写入请求。 此操作是异步的。如果你想为会话等待真正关闭，返回CloseFuture等待。
	 * 
	 * @param immediately
	 *            {@code true}为这次会话真正立即关闭. {@link #close()}为挂起的写请求，将简单地丢弃.
	 *            {@code false} 为本次会话结束后，所有排队的写请求被刷新 {@link #closeOnFlush()}).
	 */
	CloseFuture close(boolean immediately);

	/**
	 * Closes this session after all queued write requests are flushed. This
	 * operation is asynchronous. Wait for the returned {@link CloseFuture} if
	 * you want to wait for the session actually closed.
	 * 
	 * @deprecated use {@link IoSession#close(boolean)}
	 */
	@Deprecated
	CloseFuture close();

	/**
	 * 返回本次会议的附件。这种方法是<tt>getAttribute("")</tt>方法相同。
	 * 
	 * @deprecated Use {@link #getAttribute(Object)} instead.
	 */
	@Deprecated
	Object getAttachment();

	/**
	 * Sets an attachment of this session. This method is identical with
	 * <tt>setAttribute( "", attachment )</tt>.
	 * 
	 * @return Old attachment. <tt>null</tt> if it is new.
	 * @deprecated Use {@link #setAttribute(Object, Object)} instead.
	 */
	@Deprecated
	Object setAttachment(Object attachment);

	/**
	 * 返回这个会话的用户定义的属性值。
	 * 
	 * @param key
	 *            the key of the attribute
	 * @return <tt>null</tt> if there is no attribute with the specified key
	 */
	Object getAttribute(Object key);

	/**
	 * 返回用户定义的属性值与指定键相关联。 如果没有这样的属性， 指定的默认​​值是与指定的键 和默认的返回值。
	 * 这种方法是用下面的代码，除了原子地执行操作相同。
	 * 
	 * <pre>
	 * if (containsAttribute(key)) {
	 * 	return getAttribute(key);
	 * } else {
	 * 	setAttribute(key, defaultValue);
	 * 	return defaultValue;
	 * }
	 * </pre>
	 */
	Object getAttribute(Object key, Object defaultValue);

	/**
	 * 设置一个用户定义的属性。
	 * 
	 * @param key
	 *            the key of the attribute
	 * @param value
	 *            the value of the attribute
	 * @return The old value of the attribute. <tt>null</tt> if it is new.
	 */
	Object setAttribute(Object key, Object value);

	/**
	 * 设置一个用户定义不带值的属性。这是有用的，当你只想把一个“标记”属性。 它的值默认设置为 {@link Boolean#TRUE}.
	 * 
	 * @param key
	 *            the key of the attribute
	 * @return The old value of the attribute. <tt>null</tt> if it is new.
	 */
	Object setAttribute(Object key);

	/**
	 * 设置一个用户定义的属性，如果用指定的关键属性还没有设置。 这种方法是用下面的代码，除了原子地执行操作相同。
	 * 
	 * <pre>
	 * if (containsAttribute(key)) {
	 * 	return getAttribute(key);
	 * } else {
	 * 	return setAttribute(key, value);
	 * }
	 * </pre>
	 */
	Object setAttributeIfAbsent(Object key, Object value);

	/**
	 * 设置一个用户定义的属性，如果没有值与指定键的属性还没有设置。 这是有用的，当你只想把一个“标记”属性。 它的值设置为Boolean.TRUE。
	 * 这种方法是用下面的代码，除了原子地执行操作相同。
	 * 
	 * <pre>
	 * if (containsAttribute(key)) {
	 * 	return getAttribute(key); // might not always be Boolean.TRUE.
	 * } else {
	 * 	return setAttribute(key);
	 * }
	 * </pre>
	 */
	Object setAttributeIfAbsent(Object key);

	/**
	 * 删除具有指定键的用户定义的属性。
	 * 
	 * @return The old value of the attribute. <tt>null</tt> if not found.
	 */
	Object removeAttribute(Object key);

	/**
	 * 如果当前的属性值等于指定的值，删除用户定义的属性与指定键。 这种方法是用下面的代码，除了原子地执行操作相同。
	 * 
	 * <pre>
	 * if (containsAttribute(key) &amp;&amp; getAttribute(key).equals(value)) {
	 * 	removeAttribute(key);
	 * 	return true;
	 * } else {
	 * 	return false;
	 * }
	 * </pre>
	 */
	boolean removeAttribute(Object key, Object value);

	/**
	 * 如果属性的值是用户等于指定的旧值，替换定义与指定的键属性。 这种方法是用下面的代码，除了原子地执行操作相同。
	 * 
	 * <pre>
	 * if (containsAttribute(key) &amp;&amp; getAttribute(key).equals(oldValue)) {
	 * 	setAttribute(key, newValue);
	 * 	return true;
	 * } else {
	 * 	return false;
	 * }
	 * </pre>
	 */
	boolean replaceAttribute(Object key, Object oldValue, Object newValue);

	/**
	 * 如果这个环节包含具有指定<tt>key</tt>的属性，返回<tt>true</tt>，。
	 */
	boolean containsAttribute(Object key);

	/**
	 * 返回所有用户定义的属性键设置的集合。
	 */
	Set<Object> getAttributeKeys();

	/**
	 * 如果本次会议是与远程对等连接，返回<code>true</code>。
	 */
	boolean isConnected();

	/**
	 * 当且仅当此会话被关闭（但还没有断开）或关闭，则返回<code>true</tt>。
	 */
	boolean isClosing();

	/**
	 * 此方法返回相同的实例每当用户调用它，返回本次会话{@link CloseFuture}。
	 */
	CloseFuture getCloseFuture();

	/**
	 * 返回远程对等套接字地址。
	 */
	SocketAddress getRemoteAddress();

	/**
	 * 返回与本次会话相关联的本地地址，。
	 */
	SocketAddress getLocalAddress();

	/**
	 * 返回{@link IoService}套接字地址听来管理这个环节。 如果本次会议是由{@link IoAcceptor}管理， 它返回的是作为一个
	 * {@link IoAcceptor#bind()}的参数指定的{@link SocketAddress}。 如果本次会议是由
	 * {@link IoConnector}管理，此方法返回与{@link #getRemoteAddress()}同一地址。
	 */
	SocketAddress getServiceAddress();

	/**
	 * 
	 * TODO 设置写请求队列.
	 * 
	 * @param writeRequestQueue
	 */
	void setCurrentWriteRequest(WriteRequest currentWriteRequest);

	/**
	 * 暂停阅读本次会话的操作。
	 */
	void suspendRead();

	/**
	 * 本次会话挂起写操作。
	 */
	void suspendWrite();

	/**
	 * 继续阅读本次会话的操作。
	 */
	void resumeRead();

	/**
	 * 继续写入本次会话的操作。
	 */
	void resumeWrite();

	/**
	 * 是否读去操作的暂停状态
	 * 
	 * @return 如果是已暂停挂起的，则返回<code>true</code>
	 */
	boolean isReadSuspended();

	/**
	 * 是否写入操作的暂停状态
	 * 
	 * @return 如果是已暂停挂起的，则返回<code>true</code>
	 */
	boolean isWriteSuspended();

	/**
	 * 更新与 吞吐量相关的所有特性统计， 假设 指定的时间是当前时间。 如果他们已经在计算最后
	 * {@link IoSessionConfig#getThroughputCalculationInterval() calculation
	 * interval}，默认情况下此方法返回不更新吞吐量性能。
	 * 
	 * 
	 * @param currentTime
	 *            当前时间以毫秒为单位
	 * @param force
	 *            如果<tt>force</tt>为<tt>true</tt>，则立即更新吞吐量
	 */
	void updateThroughput(long currentTime, boolean force);

	/**
	 * 返回的是从本次会议读取的字节总数。
	 */
	long getReadBytes();

	/**
	 * 返回其中写入本次会议的总字节数。
	 */
	long getWrittenBytes();

	/**
	 * 返回从本次会议中读取和解码消息总数。
	 */
	long getReadMessages();

	/**
	 * 返回从本次会话中写入和编码消息的总数
	 */
	long getWrittenMessages();

	/**
	 * 返回每秒读取的字节数。
	 */
	double getReadBytesThroughput();

	/**
	 * 返回每秒写入的字节数。
	 */
	double getWrittenBytesThroughput();

	/**
	 * 返回每秒读消息的数量。
	 */
	double getReadMessagesThroughput();

	/**
	 * 返回每秒写消息的数量。
	 */
	double getWrittenMessagesThroughput();

	/**
	 * 返回的预定要写入本次会话的消息数。
	 */
	int getScheduledWriteMessages();

	/**
	 * 返回的预定要写入本次会话的字节数。
	 */
	long getScheduledWriteBytes();

	/**
	 * 返回目前正由{@link IoService}写入的信息.
	 * 
	 * @return <tt>null</tt> if and if only no message is being written
	 */
	Object getCurrentWriteMessage();

	/**
	 * Returns the {@link WriteRequest} which is being processed by
	 * {@link IoService}.
	 * 
	 * @return <tt>null</tt> 如果没有消息，如果仅仅是被写入
	 */
	WriteRequest getCurrentWriteRequest();

	/**
	 * @return 会话的创建时间以毫秒为单位
	 */
	long getCreationTime();

	/**
	 * 返回毫秒值，在I/O操作发生最后。
	 */
	long getLastIoTime();

	/**
	 * 返回毫秒值，读操作发生最后。
	 */
	long getLastReadTime();

	/**
	 * 返回毫秒值，写操作发生最后。
	 */
	long getLastWriteTime();

	/**
	 * 如果这次会议是为{@link IdleStatus}指定闲置，则返回<code>true</code>。
	 */
	boolean isIdle(IdleStatus status);

	/**
	 * 返回<code>true</code>如果这次会话是 {@link IdleStatus#READER_IDLE}.
	 * 
	 * @see #isIdle(IdleStatus)
	 */
	boolean isReaderIdle();

	/**
	 * 返回<code>true</code>如果这次会话是 {@link IdleStatus#WRITER_IDLE}.
	 * 
	 * @see #isIdle(IdleStatus)
	 */
	boolean isWriterIdle();

	/**
	 * 返回<code>true</code>如果这次会话是{@link IdleStatus#BOTH_IDLE} .
	 * 
	 * @see #isIdle(IdleStatus)
	 */
	boolean isBothIdle();

	/**
	 * 空闲次数：返回在连续发射<tt>sessionIdle</tt>时间指定的{@link IdleStatus}的数量.
	 * <p/>
	 * 如果<tt>sessionIdle</tt>事件是在第一次I/O被激发后的时间，<tt>idleCount</tt> 变为 <tt>1</tt>
	 * 。如果任何I/O再次发生， <tt>idleCount</tt>复位为<tt>1</tt>。 如果<tt>sessionIdle</tt>
	 * 事件没有触发任何两个以上的I/O，则 <tt>idleCount</tt>会增加至<tt>2</tt>。
	 */
	int getIdleCount(IdleStatus status);

	/**
	 * 读空闲的次数：返回发射连续的<tt>sessionIdle</tt>事件的{@link IdleStatus#READER_IDLE}。
	 * 
	 * @see #getIdleCount(IdleStatus)
	 */
	int getReaderIdleCount();

	/**
	 * 写空闲的次数：返回发射连续的<tt>sessionIdle</tt>事件的{@link IdleStatus#READER_IDLE}。
	 * 
	 * @see #getIdleCount(IdleStatus)
	 */
	int getWriterIdleCount();

	/**
	 * 获得这两个空闲计数:返回发射连续的<tt>sessionIdle</tt>事件的{@link IdleStatus#READER_IDLE}。
	 * 
	 * @see #getIdleCount(IdleStatus)
	 */
	int getBothIdleCount();

	/**
	 * 返回当最后<tt>sessionIdle</tt>事件指定{@link IdleStatus}发射的毫秒时间。
	 */
	long getLastIdleTime(IdleStatus status);

	/**
	 * 返回当最后<tt>sessionIdle</tt>事件是{@link IdleStatus#READER_IDLE}发射的毫秒时间。
	 * 
	 * @see #getLastIdleTime(IdleStatus)
	 */
	long getLastReaderIdleTime();

	/**
	 * 返回当最后<tt>sessionIdle</tt>事件是{@link IdleStatus#WRITER_IDLE}发射毫秒的时间。
	 * 
	 * @see #getLastIdleTime(IdleStatus)
	 */
	long getLastWriterIdleTime();

	/**
	 * 返回当最后<tt>sessionIdle</tt>事件是{@link IdleStatus#BOTH_IDLE}发射的毫秒时间。
	 * 
	 * @see #getLastIdleTime(IdleStatus)
	 */
	long getLastBothIdleTime();
}
