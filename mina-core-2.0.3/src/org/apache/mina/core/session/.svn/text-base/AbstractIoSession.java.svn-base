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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.DefaultFileRegion;
import org.apache.mina.core.file.FilenameFileRegion;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.DefaultCloseFuture;
import org.apache.mina.core.future.DefaultReadFuture;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.AbstractIoService;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteException;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteTimeoutException;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.mina.util.ExceptionMonitor;

/**
 * IoSession的一个抽象实现类
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public abstract class AbstractIoSession implements IoSession {
	/** The associated handler */
	private final IoHandler handler;

	/** The session config */
	protected IoSessionConfig config;

	/** The service which will manage this session */
	private final IoService service;

	private static final AttributeKey READY_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "readyReadFutures");

	private static final AttributeKey WAITING_READ_FUTURES_KEY = new AttributeKey(AbstractIoSession.class, "waitingReadFutures");

	private static final IoFutureListener<CloseFuture> SCHEDULED_COUNTER_RESETTER = new IoFutureListener<CloseFuture>() {
		public void operationComplete(CloseFuture future) {
			AbstractIoSession session = (AbstractIoSession) future.getSession();
			session.scheduledWriteBytes.set(0);
			session.scheduledWriteMessages.set(0);
			session.readBytesThroughput = 0;
			session.readMessagesThroughput = 0;
			session.writtenBytesThroughput = 0;
			session.writtenMessagesThroughput = 0;
		}
	};

	/**
	 * An internal write request object that triggers session close.
	 * 
	 * @see #writeRequestQueue
	 */
	private static final WriteRequest CLOSE_REQUEST = new DefaultWriteRequest(new Object());

	private final Object lock = new Object();

	/** 会话属性映射图 */
	private IoSessionAttributeMap attributes;

	/** 写请求队列 */
	private WriteRequestQueue writeRequestQueue;

	/** 当前写请求 */
	private WriteRequest currentWriteRequest;

	/** 会话建立的时间 */
	private final long creationTime;

	/** 一个ID生成器生成保证为会议唯一的ID */
	private static AtomicLong idGenerator = new AtomicLong(0);

	/** The session ID */
	private long sessionId;

	/**
	 * 一个将要关闭的连接，将被设置为'关闭'。
	 */
	private final CloseFuture closeFuture = new DefaultCloseFuture(this);

	private volatile boolean closing;

	// traffic control
	private boolean readSuspended = false;

	private boolean writeSuspended = false;

	// Status variables
	private final AtomicBoolean scheduledForFlush = new AtomicBoolean();

	private final AtomicInteger scheduledWriteBytes = new AtomicInteger();

	private final AtomicInteger scheduledWriteMessages = new AtomicInteger();

	private long readBytes;

	private long writtenBytes;

	private long readMessages;

	private long writtenMessages;

	private long lastReadTime;

	private long lastWriteTime;

	private long lastThroughputCalculationTime;

	private long lastReadBytes;

	private long lastWrittenBytes;

	private long lastReadMessages;

	private long lastWrittenMessages;

	private double readBytesThroughput;

	private double writtenBytesThroughput;

	private double readMessagesThroughput;

	private double writtenMessagesThroughput;

	private AtomicInteger idleCountForBoth = new AtomicInteger();

	private AtomicInteger idleCountForRead = new AtomicInteger();

	private AtomicInteger idleCountForWrite = new AtomicInteger();

	private long lastIdleTimeForBoth;

	private long lastIdleTimeForRead;

	private long lastIdleTimeForWrite;

	private boolean deferDecreaseReadBuffer = true;

	/**
	 * TODO Add method documentation
	 */
	protected AbstractIoSession(IoService service) {
		this.service = service;
		this.handler = service.getHandler();

		// Initialize all the Session counters to the current time
		long currentTime = System.currentTimeMillis();
		creationTime = currentTime;
		lastThroughputCalculationTime = currentTime;
		lastReadTime = currentTime;
		lastWriteTime = currentTime;
		lastIdleTimeForBoth = currentTime;
		lastIdleTimeForRead = currentTime;
		lastIdleTimeForWrite = currentTime;

		// TODO add documentation
		closeFuture.addListener(SCHEDULED_COUNTER_RESETTER);

		// Set a new ID for this session
		sessionId = idGenerator.incrementAndGet();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * We use an AtomicLong to guarantee that the session ID are unique.
	 */
	public final long getId() {
		return sessionId;
	}

	/**
	 * @return The associated IoProcessor for this session
	 */
	public abstract IoProcessor getProcessor();

	/**
	 * {@inheritDoc}
	 */
	public final boolean isConnected() {
		return !closeFuture.isClosed();
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isClosing() {
		return closing || closeFuture.isClosed();
	}

	/**
	 * {@inheritDoc}
	 */
	public final CloseFuture getCloseFuture() {
		return closeFuture;
	}

	/**
	 * Tells if the session is scheduled for flushed
	 * 
	 * @param true if the session is scheduled for flush
	 */
	public final boolean isScheduledForFlush() {
		return scheduledForFlush.get();
	}

	/**
	 * Schedule the session for flushed
	 */
	public final void scheduledForFlush() {
		scheduledForFlush.set(true);
	}

	/**
	 * Change the session's status : it's not anymore scheduled for flush
	 */
	public final void unscheduledForFlush() {
		scheduledForFlush.set(false);
	}

	/**
	 * Set the scheduledForFLush flag. As we may have concurrent access to this
	 * flag, we compare and set it in one call.
	 * 
	 * @param schedule
	 *            the new value to set if not already set.
	 * @return true if the session flag has been set, and if it wasn't set
	 *         already.
	 */
	public final boolean setScheduledForFlush(boolean schedule) {
		if (schedule) {
			// If the current tag is set to false, switch it to true,
			// otherwise, we do nothing but return false : the session
			// is already scheduled for flush
			return scheduledForFlush.compareAndSet(false, schedule);
		}

		scheduledForFlush.set(schedule);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final CloseFuture close(boolean rightNow) {
		if (rightNow) {
			return close();
		}

		return closeOnFlush();
	}

	/**
	 * 当要结束当前会话时，会发送一个一个写请求CLOSE_REQUEST。而closeFuture这个CloseFuture会在连接关闭时状态被设置为”
	 * closed”,它的监听器是SCHEDULED_COUNTER_RESETTER。
	 * close和closeOnFlush都是异步的关闭操作，区别是前者立即关闭连接
	 * ，而后者是在写请求队列中放入一个CLOSE_REQUEST，并将其即时刷新出去，若要真正等待关闭完成，需要调用方在返回的CloseFuture等待
	 * 
	 * {@inheritDoc}
	 */
	public final CloseFuture close() {
		synchronized (lock) {
			if (isClosing()) {
				return closeFuture;
			}

			closing = true;
		}

		getFilterChain().fireFilterClose();// fire出关闭事件
		return closeFuture;
	}

	private final CloseFuture closeOnFlush() {
		getWriteRequestQueue().offer(this, CLOSE_REQUEST);
		getProcessor().flush(this);
		return closeFuture;
	}

	/**
	 * {@inheritDoc}
	 */
	public IoHandler getHandler() {
		return handler;
	}

	/**
	 * {@inheritDoc}
	 */
	public IoSessionConfig getConfig() {
		return config;
	}

	/**
	 * 读数据
	 */
	public final ReadFuture read() {
		if (!getConfig().isUseReadOperation()) {// 会话配置不允许读数据(这是默认情况)
			throw new IllegalStateException("useReadOperation is not enabled.");
		}

		// 获取已经可被读数据队列
		Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
		ReadFuture future;
		synchronized (readyReadFutures) {// 锁住读数据队列
			// 取队头数据
			future = readyReadFutures.poll();
			if (future != null) {
				if (future.isClosed()) {// 关联的会话已经关闭了，让读者知道此情况
					// Let other readers get notified.
					readyReadFutures.offer(future);
				}
			} else {
				future = new DefaultReadFuture(this);
				// 将此数据插入等待被读取数据的队列，这个代码和上面的getReadyReadFutures类似，只是键值不同而已
				getWaitingReadFutures().offer(future);
			}
		}

		return future;
	}

	/**
	 * TODO Add method documentation
	 */
	public final void offerReadFuture(Object message) {
		newReadFuture().setRead(message);
	}

	/**
	 * TODO Add method documentation
	 */
	public final void offerFailedReadFuture(Throwable exception) {
		newReadFuture().setException(exception);
	}

	/**
	 * TODO Add method documentation
	 */
	public final void offerClosedReadFuture() {
		Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
		synchronized (readyReadFutures) {
			newReadFuture().setClosed();
		}
	}

	/**
	 * TODO Add method documentation
	 */
	private ReadFuture newReadFuture() {
		Queue<ReadFuture> readyReadFutures = getReadyReadFutures();
		Queue<ReadFuture> waitingReadFutures = getWaitingReadFutures();
		ReadFuture future;
		synchronized (readyReadFutures) {
			future = waitingReadFutures.poll();
			if (future == null) {
				future = new DefaultReadFuture(this);
				readyReadFutures.offer(future);
			}
		}
		return future;
	}

	/**
	 * 返回可被读数据队列
	 */
	private Queue<ReadFuture> getReadyReadFutures() {
		// 从会话映射表中取出可被读数据队列
		Queue<ReadFuture> readyReadFutures = (Queue<ReadFuture>) getAttribute(READY_READ_FUTURES_KEY);
		if (readyReadFutures == null) {// 第一次读数据
			// 构造一个新读数据队列
			readyReadFutures = new ConcurrentLinkedQueue<ReadFuture>();

			Queue<ReadFuture> oldReadyReadFutures = (Queue<ReadFuture>) setAttributeIfAbsent(READY_READ_FUTURES_KEY, readyReadFutures);
			if (oldReadyReadFutures != null) {
				readyReadFutures = oldReadyReadFutures;
			}
		}
		return readyReadFutures;
	}

	/**
	 * TODO Add method documentation
	 */
	private Queue<ReadFuture> getWaitingReadFutures() {
		Queue<ReadFuture> waitingReadyReadFutures = (Queue<ReadFuture>) getAttribute(WAITING_READ_FUTURES_KEY);
		if (waitingReadyReadFutures == null) {
			waitingReadyReadFutures = new ConcurrentLinkedQueue<ReadFuture>();

			Queue<ReadFuture> oldWaitingReadyReadFutures = (Queue<ReadFuture>) setAttributeIfAbsent(WAITING_READ_FUTURES_KEY, waitingReadyReadFutures);
			if (oldWaitingReadyReadFutures != null) {
				waitingReadyReadFutures = oldWaitingReadyReadFutures;
			}
		}
		return waitingReadyReadFutures;
	}

	/**
	 * {@inheritDoc}
	 */
	public WriteFuture write(Object message) {
		return write(message, null);
	}

	/**
	 * 写数据到指定远端地址的过程，可以写三种类型数据：IoBuffer，整个文件或文件的部分区域，
	 * 这会通过传递写请求给过滤器链条来完成数据向目的远端的传输。
	 * 
	 * @param Object
	 *            消息体
	 * @param SocketAddress
	 *            远端IP地址
	 */
	public WriteFuture write(Object message, SocketAddress remoteAddress) {
		if (message == null) {
			// 空消息
			throw new IllegalArgumentException("message");
		}

		// 我们不能将消息发送到一个连接会话，如果我们没有足够的远程地址
		if (!getTransportMetadata().isConnectionless() && (remoteAddress != null)) {
			throw new UnsupportedOperationException();
		}

		// 如果会话已经关闭或正在关闭，我们不能或者发送信息到远程端。我们的未来产生一个包含一个异常。
		if (isClosing() || !isConnected()) {
			WriteFuture future = new DefaultWriteFuture(this);
			WriteRequest request = new DefaultWriteRequest(message, future, remoteAddress);
			WriteException writeException = new WriteToClosedSessionException(request);
			future.setException(writeException);
			return future;
		}

		FileChannel openedFileChannel = null;

		// TODO: 删除此代码只要我们使用的InputStream，而不是为消息对象。
		try {
			if ((message instanceof IoBuffer) && !((IoBuffer) message).hasRemaining()) {
				// 没什么可写：可能是一个错误的用户代码
				throw new IllegalArgumentException("message is empty. Forgot to call flip()?");
			} else if (message instanceof FileChannel) {// 要发送的是文件的某一区域
				FileChannel fileChannel = (FileChannel) message;
				message = new DefaultFileRegion(fileChannel, 0, fileChannel.size());
			} else if (message instanceof File) {// 要发送的是文件,打开文件通道
				File file = (File) message;
				openedFileChannel = new FileInputStream(file).getChannel();
				message = new FilenameFileRegion(file, openedFileChannel, 0, openedFileChannel.size());
			}
		} catch (IOException e) {
			ExceptionMonitor.getInstance().exceptionCaught(e);
			return DefaultWriteFuture.newNotWrittenFuture(this, e);
		}

		// 现在，我们可以编写信息。首先，创建一个DefaultWriteFuture.
		WriteFuture writeFuture = new DefaultWriteFuture(this);
		// 构造写请求,通过过滤器链发送出去，写请求中指明了要发送的消息，目的地址，以及返回的结果
		WriteRequest writeRequest = new DefaultWriteRequest(message, writeFuture, remoteAddress);

		// 然后，脱离过滤链
		IoFilterChain filterChain = getFilterChain();
		// WriteRequest注入到filterChain
		filterChain.fireFilterWrite(writeRequest);

		// TODO : 这不是我们的业务！来电者创造了一个FileChannel，他不得不关闭它！
		// 如果打开了一个文件通道（发送的文件的部分区域或全部），就必须在写请求完成时关闭文件通道
		if (openedFileChannel != null) {
			// 如果我们打开了一个FileChannel，它需要被关闭时，写操作完成
			final FileChannel finalChannel = openedFileChannel;
			writeFuture.addListener(new IoFutureListener<WriteFuture>() {
				public void operationComplete(WriteFuture future) {
					try {
						// 关闭文件通道
						finalChannel.close();
					} catch (IOException e) {
						ExceptionMonitor.getInstance().exceptionCaught(e);
					}
				}
			});
		}

		// Return the WriteFuture.
		return writeFuture;
	}

	/**
	 * 获得附件
	 */
	public final Object getAttachment() {
		return getAttribute("");
	}

	/**
	 * 设置附件
	 */
	public final Object setAttachment(Object attachment) {
		return setAttribute("", attachment);
	}

	/**
	 * 获取属性
	 */
	public final Object getAttribute(Object key) {
		return getAttribute(key, null);
	}

	/**
	 * 获取属性
	 */
	public final Object getAttribute(Object key, Object defaultValue) {
		return attributes.getAttribute(this, key, defaultValue);
	}

	/**
	 * 设置属性
	 */
	public final Object setAttribute(Object key, Object value) {
		return attributes.setAttribute(this, key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public final Object setAttribute(Object key) {
		return setAttribute(key, Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final Object setAttributeIfAbsent(Object key, Object value) {
		return attributes.setAttributeIfAbsent(this, key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public final Object setAttributeIfAbsent(Object key) {
		return setAttributeIfAbsent(key, Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final Object removeAttribute(Object key) {
		return attributes.removeAttribute(this, key);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean removeAttribute(Object key, Object value) {
		return attributes.removeAttribute(this, key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean replaceAttribute(Object key, Object oldValue, Object newValue) {
		return attributes.replaceAttribute(this, key, oldValue, newValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean containsAttribute(Object key) {
		return attributes.containsAttribute(this, key);
	}

	/**
	 * {@inheritDoc}
	 */
	public final Set<Object> getAttributeKeys() {
		return attributes.getAttributeKeys(this);
	}

	/**
	 * TODO Add method documentation
	 */
	public final IoSessionAttributeMap getAttributeMap() {
		return attributes;
	}

	/**
	 * TODO Add method documentation
	 */
	public final void setAttributeMap(IoSessionAttributeMap attributes) {
		this.attributes = attributes;
	}

	/**
	 * Create a new close aware write queue, based on the given write queue.
	 * 
	 * @param writeRequestQueue
	 *            The write request queue
	 */
	public final void setWriteRequestQueue(WriteRequestQueue writeRequestQueue) {
		this.writeRequestQueue = new CloseAwareWriteQueue(writeRequestQueue);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void suspendRead() {
		readSuspended = true;
		if (isClosing() || !isConnected()) {
			return;
		}
		getProcessor().updateTrafficControl(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void suspendWrite() {
		writeSuspended = true;
		if (isClosing() || !isConnected()) {
			return;
		}
		getProcessor().updateTrafficControl(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final void resumeRead() {
		readSuspended = false;
		if (isClosing() || !isConnected()) {
			return;
		}
		getProcessor().updateTrafficControl(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final void resumeWrite() {
		writeSuspended = false;
		if (isClosing() || !isConnected()) {
			return;
		}
		getProcessor().updateTrafficControl(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isReadSuspended() {
		return readSuspended;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isWriteSuspended() {
		return writeSuspended;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getReadBytes() {
		return readBytes;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getWrittenBytes() {
		return writtenBytes;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getReadMessages() {
		return readMessages;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getWrittenMessages() {
		return writtenMessages;
	}

	/**
	 * {@inheritDoc}
	 */
	public final double getReadBytesThroughput() {
		return readBytesThroughput;
	}

	/**
	 * {@inheritDoc}
	 */
	public final double getWrittenBytesThroughput() {
		return writtenBytesThroughput;
	}

	/**
	 * {@inheritDoc}
	 */
	public final double getReadMessagesThroughput() {
		return readMessagesThroughput;
	}

	/**
	 * {@inheritDoc}
	 */
	public final double getWrittenMessagesThroughput() {
		return writtenMessagesThroughput;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateThroughput(long currentTime, boolean force) {
		int interval = (int) (currentTime - lastThroughputCalculationTime);

		long minInterval = getConfig().getThroughputCalculationIntervalInMillis();
		if ((minInterval == 0) || (interval < minInterval)) {
			if (!force) {
				return;
			}
		}

		readBytesThroughput = (readBytes - lastReadBytes) * 1000.0 / interval;
		writtenBytesThroughput = (writtenBytes - lastWrittenBytes) * 1000.0 / interval;
		readMessagesThroughput = (readMessages - lastReadMessages) * 1000.0 / interval;
		writtenMessagesThroughput = (writtenMessages - lastWrittenMessages) * 1000.0 / interval;

		lastReadBytes = readBytes;
		lastWrittenBytes = writtenBytes;
		lastReadMessages = readMessages;
		lastWrittenMessages = writtenMessages;

		lastThroughputCalculationTime = currentTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getScheduledWriteBytes() {
		return scheduledWriteBytes.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getScheduledWriteMessages() {
		return scheduledWriteMessages.get();
	}

	/**
	 * TODO Add method documentation
	 */
	protected void setScheduledWriteBytes(int byteCount) {
		scheduledWriteBytes.set(byteCount);
	}

	/**
	 * TODO Add method documentation
	 */
	protected void setScheduledWriteMessages(int messages) {
		scheduledWriteMessages.set(messages);
	}

	/**
	 * TODO Add method documentation
	 */
	public final void increaseReadBytes(long increment, long currentTime) {
		if (increment <= 0) {
			return;
		}

		readBytes += increment;
		lastReadTime = currentTime;
		idleCountForBoth.set(0);
		idleCountForRead.set(0);

		if (getService() instanceof AbstractIoService) {
			((AbstractIoService) getService()).getStatistics().increaseReadBytes(increment, currentTime);
		}
	}

	/**
	 * TODO Add method documentation
	 */
	public final void increaseReadMessages(long currentTime) {
		readMessages++;
		lastReadTime = currentTime;
		idleCountForBoth.set(0);
		idleCountForRead.set(0);

		if (getService() instanceof AbstractIoService) {
			((AbstractIoService) getService()).getStatistics().increaseReadMessages(currentTime);
		}
	}

	/**
	 * TODO Add method documentation
	 */
	public final void increaseWrittenBytes(int increment, long currentTime) {
		if (increment <= 0) {
			return;
		}

		writtenBytes += increment;
		lastWriteTime = currentTime;
		idleCountForBoth.set(0);
		idleCountForWrite.set(0);

		if (getService() instanceof AbstractIoService) {
			((AbstractIoService) getService()).getStatistics().increaseWrittenBytes(increment, currentTime);
		}

		increaseScheduledWriteBytes(-increment);
	}

	/**
	 * TODO Add method documentation
	 */
	public final void increaseWrittenMessages(WriteRequest request, long currentTime) {
		Object message = request.getMessage();
		if (message instanceof IoBuffer) {
			IoBuffer b = (IoBuffer) message;
			if (b.hasRemaining()) {
				return;
			}
		}

		writtenMessages++;
		lastWriteTime = currentTime;
		if (getService() instanceof AbstractIoService) {
			((AbstractIoService) getService()).getStatistics().increaseWrittenMessages(currentTime);
		}

		decreaseScheduledWriteMessages();
	}

	/**
	 * TODO Add method documentation
	 */
	public final void increaseScheduledWriteBytes(int increment) {
		scheduledWriteBytes.addAndGet(increment);
		if (getService() instanceof AbstractIoService) {
			((AbstractIoService) getService()).getStatistics().increaseScheduledWriteBytes(increment);
		}
	}

	/**
	 * TODO Add method documentation
	 */
	public final void increaseScheduledWriteMessages() {
		scheduledWriteMessages.incrementAndGet();
		if (getService() instanceof AbstractIoService) {
			((AbstractIoService) getService()).getStatistics().increaseScheduledWriteMessages();
		}
	}

	/**
	 * TODO Add method documentation
	 */
	private void decreaseScheduledWriteMessages() {
		scheduledWriteMessages.decrementAndGet();
		if (getService() instanceof AbstractIoService) {
			((AbstractIoService) getService()).getStatistics().decreaseScheduledWriteMessages();
		}
	}

	/**
	 * TODO Add method documentation
	 */
	public final void decreaseScheduledBytesAndMessages(WriteRequest request) {
		Object message = request.getMessage();
		if (message instanceof IoBuffer) {
			IoBuffer b = (IoBuffer) message;
			if (b.hasRemaining()) {
				increaseScheduledWriteBytes(-((IoBuffer) message).remaining());
			} else {
				decreaseScheduledWriteMessages();
			}
		} else {
			decreaseScheduledWriteMessages();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final WriteRequestQueue getWriteRequestQueue() {
		if (writeRequestQueue == null) {
			throw new IllegalStateException();
		}
		return writeRequestQueue;
	}

	/**
	 * {@inheritDoc}
	 */
	public final WriteRequest getCurrentWriteRequest() {
		return currentWriteRequest;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Object getCurrentWriteMessage() {
		WriteRequest req = getCurrentWriteRequest();
		if (req == null) {
			return null;
		}
		return req.getMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setCurrentWriteRequest(WriteRequest currentWriteRequest) {
		this.currentWriteRequest = currentWriteRequest;
	}

	/**
	 * TODO Add method documentation
	 */
	public final void increaseReadBufferSize() {
		int newReadBufferSize = getConfig().getReadBufferSize() << 1;
		if (newReadBufferSize <= getConfig().getMaxReadBufferSize()) {
			getConfig().setReadBufferSize(newReadBufferSize);
		} else {
			getConfig().setReadBufferSize(getConfig().getMaxReadBufferSize());
		}

		deferDecreaseReadBuffer = true;
	}

	/**
	 * TODO Add method documentation
	 */
	public final void decreaseReadBufferSize() {
		if (deferDecreaseReadBuffer) {
			deferDecreaseReadBuffer = false;
			return;
		}

		if (getConfig().getReadBufferSize() > getConfig().getMinReadBufferSize()) {
			getConfig().setReadBufferSize(getConfig().getReadBufferSize() >>> 1);
		}

		deferDecreaseReadBuffer = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getCreationTime() {
		return creationTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getLastIoTime() {
		return Math.max(lastReadTime, lastWriteTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getLastReadTime() {
		return lastReadTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getLastWriteTime() {
		return lastWriteTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isIdle(IdleStatus status) {
		if (status == IdleStatus.BOTH_IDLE) {
			return idleCountForBoth.get() > 0;
		}

		if (status == IdleStatus.READER_IDLE) {
			return idleCountForRead.get() > 0;
		}

		if (status == IdleStatus.WRITER_IDLE) {
			return idleCountForWrite.get() > 0;
		}

		throw new IllegalArgumentException("Unknown idle status: " + status);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isBothIdle() {
		return isIdle(IdleStatus.BOTH_IDLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isReaderIdle() {
		return isIdle(IdleStatus.READER_IDLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isWriterIdle() {
		return isIdle(IdleStatus.WRITER_IDLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getIdleCount(IdleStatus status) {
		if (getConfig().getIdleTime(status) == 0) {
			if (status == IdleStatus.BOTH_IDLE) {
				idleCountForBoth.set(0);
			}

			if (status == IdleStatus.READER_IDLE) {
				idleCountForRead.set(0);
			}

			if (status == IdleStatus.WRITER_IDLE) {
				idleCountForWrite.set(0);
			}
		}

		if (status == IdleStatus.BOTH_IDLE) {
			return idleCountForBoth.get();
		}

		if (status == IdleStatus.READER_IDLE) {
			return idleCountForRead.get();
		}

		if (status == IdleStatus.WRITER_IDLE) {
			return idleCountForWrite.get();
		}

		throw new IllegalArgumentException("Unknown idle status: " + status);
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getLastIdleTime(IdleStatus status) {
		if (status == IdleStatus.BOTH_IDLE) {
			return lastIdleTimeForBoth;
		}

		if (status == IdleStatus.READER_IDLE) {
			return lastIdleTimeForRead;
		}

		if (status == IdleStatus.WRITER_IDLE) {
			return lastIdleTimeForWrite;
		}

		throw new IllegalArgumentException("Unknown idle status: " + status);
	}

	/**
	 * TODO Add method documentation
	 */
	public final void increaseIdleCount(IdleStatus status, long currentTime) {
		if (status == IdleStatus.BOTH_IDLE) {
			idleCountForBoth.incrementAndGet();
			lastIdleTimeForBoth = currentTime;
		} else if (status == IdleStatus.READER_IDLE) {
			idleCountForRead.incrementAndGet();
			lastIdleTimeForRead = currentTime;
		} else if (status == IdleStatus.WRITER_IDLE) {
			idleCountForWrite.incrementAndGet();
			lastIdleTimeForWrite = currentTime;
		} else {
			throw new IllegalArgumentException("Unknown idle status: " + status);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getBothIdleCount() {
		return getIdleCount(IdleStatus.BOTH_IDLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getLastBothIdleTime() {
		return getLastIdleTime(IdleStatus.BOTH_IDLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getLastReaderIdleTime() {
		return getLastIdleTime(IdleStatus.READER_IDLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getLastWriterIdleTime() {
		return getLastIdleTime(IdleStatus.WRITER_IDLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getReaderIdleCount() {
		return getIdleCount(IdleStatus.READER_IDLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getWriterIdleCount() {
		return getIdleCount(IdleStatus.WRITER_IDLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public SocketAddress getServiceAddress() {
		IoService service = getService();
		if (service instanceof IoAcceptor) {
			return ((IoAcceptor) service).getLocalAddress();
		}

		return getRemoteAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	/**
	 * {@inheritDoc} TODO This is a ridiculous implementation. Need to be
	 * replaced.
	 */
	@Override
	public final boolean equals(Object o) {
		return super.equals(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (isConnected() || isClosing()) {
			String remote = null;
			String local = null;

			try {
				remote = String.valueOf(getRemoteAddress());
			} catch (Throwable t) {
				remote = "Cannot get the remote address informations: " + t.getMessage();
			}

			try {
				local = String.valueOf(getLocalAddress());
			} catch (Throwable t) {
				local = "Cannot get the local address informations: " + t.getMessage();
			}

			if (getService() instanceof IoAcceptor) {
				return "(" + getIdAsString() + ": " + getServiceName() + ", server, " + remote + " => " + local + ')';
			}

			return "(" + getIdAsString() + ": " + getServiceName() + ", client, " + local + " => " + remote + ')';
		}

		return "(" + getIdAsString() + ") Session disconnected ...";
	}

	/**
	 * TODO Add method documentation
	 */
	private String getIdAsString() {
		String id = Long.toHexString(getId()).toUpperCase();

		// Somewhat inefficient, but it won't happen that often
		// because an ID is often a big integer.
		while (id.length() < 8) {
			id = '0' + id; // padding
		}
		id = "0x" + id;

		return id;
	}

	/**
	 * TODO Add method documentation
	 */
	private String getServiceName() {
		TransportMetadata tm = getTransportMetadata();
		if (tm == null) {
			return "null";
		}

		return tm.getProviderName() + ' ' + tm.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public IoService getService() {
		return service;
	}

	/**
	 * Fires a {@link IoEventType#SESSION_IDLE} event to any applicable sessions
	 * in the specified collection.
	 * 
	 * @param currentTime
	 *            the current time (i.e. {@link System#currentTimeMillis()})
	 */
	public static void notifyIdleness(Iterator<? extends IoSession> sessions, long currentTime) {
		IoSession s = null;
		while (sessions.hasNext()) {
			s = sessions.next();
			notifyIdleSession(s, currentTime);
		}
	}

	/**
	 * Fires a {@link IoEventType#SESSION_IDLE} event if applicable for the
	 * specified {@code session}.
	 * 
	 * @param currentTime
	 *            the current time (i.e. {@link System#currentTimeMillis()})
	 */
	public static void notifyIdleSession(IoSession session, long currentTime) {
		notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.BOTH_IDLE), IdleStatus.BOTH_IDLE,
				Math.max(session.getLastIoTime(), session.getLastIdleTime(IdleStatus.BOTH_IDLE)));

		notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.READER_IDLE), IdleStatus.READER_IDLE,
				Math.max(session.getLastReadTime(), session.getLastIdleTime(IdleStatus.READER_IDLE)));

		notifyIdleSession0(session, currentTime, session.getConfig().getIdleTimeInMillis(IdleStatus.WRITER_IDLE), IdleStatus.WRITER_IDLE,
				Math.max(session.getLastWriteTime(), session.getLastIdleTime(IdleStatus.WRITER_IDLE)));

		notifyWriteTimeout(session, currentTime);
	}

	private static void notifyIdleSession0(IoSession session, long currentTime, long idleTime, IdleStatus status, long lastIoTime) {
		if ((idleTime > 0) && (lastIoTime != 0) && (currentTime - lastIoTime >= idleTime)) {
			session.getFilterChain().fireSessionIdle(status);
		}
	}

	private static void notifyWriteTimeout(IoSession session, long currentTime) {

		long writeTimeout = session.getConfig().getWriteTimeoutInMillis();
		if ((writeTimeout > 0) && (currentTime - session.getLastWriteTime() >= writeTimeout) && !session.getWriteRequestQueue().isEmpty(session)) {
			WriteRequest request = session.getCurrentWriteRequest();
			if (request != null) {
				session.setCurrentWriteRequest(null);
				WriteTimeoutException cause = new WriteTimeoutException(request);
				request.getFuture().setException(cause);
				session.getFilterChain().fireExceptionCaught(cause);
				// WriteException is an IOException, so we close the session.
				session.close(true);
			}
		}
	}

	/**
	 * WriteRequestQueue的实现，唯一加入的一个功能就是若在队头发现是请求关闭，则会去关闭会话。
	 * 
	 * TODO : Check that when closing a session, all the pending requests are
	 * correctly sent.
	 */
	private class CloseAwareWriteQueue implements WriteRequestQueue {

		/** 内部实际的写请求队列*/
		private final WriteRequestQueue queue;

		/**
		 * {@inheritDoc}
		 */
		public CloseAwareWriteQueue(WriteRequestQueue queue) {
			this.queue = queue;
		}

		/**
		 * {@inheritDoc}
		 */
		public synchronized WriteRequest poll(IoSession session) {
			WriteRequest answer = queue.poll(session);

			if (answer == CLOSE_REQUEST) {
				AbstractIoSession.this.close();
				dispose(session);
				answer = null;
			}

			return answer;
		}

		/**
		 * {@inheritDoc}
		 */
		public void offer(IoSession session, WriteRequest e) {
			queue.offer(session, e);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isEmpty(IoSession session) {
			return queue.isEmpty(session);
		}

		/**
		 * {@inheritDoc}
		 */
		public void clear(IoSession session) {
			queue.clear(session);
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose(IoSession session) {
			queue.dispose(session);
		}
	}
}
