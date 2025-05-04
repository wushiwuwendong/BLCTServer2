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
package org.apache.mina.core.service;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.IoUtil;
import org.apache.mina.core.filterchain.DefaultIoFilterChain;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.DefaultIoSessionDataStructureFactory;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionDataStructureFactory;
import org.apache.mina.core.session.IoSessionInitializationException;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.util.ExceptionMonitor;
import org.apache.mina.util.NamePreservingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * io服务器的基类 Base implementation of {@link IoService}s.
 * 
 * 包含了一个Executor来处理到来的事件。每个AbstractIoService都一个AtomicInteger类型的id号，确保每个id的唯一性。
 * 
 * An instance of IoService contains an Executor which will handle the incoming
 * events.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public abstract class AbstractIoService implements IoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIoService.class);
	/**
	 * 独特的数字标识服务。它为每个新创建的递增IoService. The unique number identifying the Service.
	 * It's incremented for each new IoService created.
	 */
	private static final AtomicInteger id = new AtomicInteger();

	/**
	 * 线程的名字从IoService建立继承的实例类的名称和标识IoService. The thread name built from the
	 * IoService inherited instance class name and the IoService Id
	 **/
	private final String threadName;

	/**
	 * 相关的执行人，负责处理的I/ O事件负责执行。 The associated executor, responsible for handling
	 * execution of I/O events.
	 */
	private final Executor executor;

	/**
	 * 用于表明当地执行程序已内创建此实例，而不是由主叫方传递一个标志。F的执行者是本地创建的，那么这将是一个对hreadPoolExecutor类的实例
	 * 。 A flag used to indicate that the local executor has been created inside
	 * this instance, and not passed by a caller.
	 * 
	 * If the executor is locally created, then it will be an instance of the
	 * ThreadPoolExecutor class.
	 */
	private final boolean createdExecutor;

	/**
	 * The IoHandler in charge of managing all the I/O Events. It is
	 */
	private IoHandler handler;

	/**
	 * The default {@link IoSessionConfig} which will be used to configure new
	 * sessions.
	 */
	private final IoSessionConfig sessionConfig;

	private final IoServiceListener serviceActivationListener = new IoServiceListener() {
		public void serviceActivated(IoService service) {
			// Update lastIoTime.
			AbstractIoService s = (AbstractIoService) service;
			IoServiceStatistics _stats = s.getStatistics();
			_stats.setLastReadTime(s.getActivationTime());
			_stats.setLastWriteTime(s.getActivationTime());
			_stats.setLastThroughputCalculationTime(s.getActivationTime());

		}

		public void serviceDeactivated(IoService service) {
			// Empty handler
		}

		public void serviceIdle(IoService service, IdleStatus idleStatus) {
			// Empty handler
		}

		public void sessionCreated(IoSession session) {
			// Empty handler
		}

		public void sessionDestroyed(IoSession session) {
			// Empty handler
		}
	};

	/**
	 * Current filter chain builder.
	 */
	private IoFilterChainBuilder filterChainBuilder = new DefaultIoFilterChainBuilder();

	private IoSessionDataStructureFactory sessionDataStructureFactory = new DefaultIoSessionDataStructureFactory();

	/**
	 * Maintains the {@link IoServiceListener}s of this service.
	 */
	private final IoServiceListenerSupport listeners;

	/**
	 * A lock object which must be acquired when related resources are
	 * destroyed.
	 */
	protected final Object disposalLock = new Object();

	private volatile boolean disposing;

	private volatile boolean disposed;

	/**
	 * {@inheritDoc}
	 */
	private IoServiceStatistics stats = new IoServiceStatistics(this);

	/**
	 * Constructor for {@link AbstractIoService}. You need to provide a default
	 * session configuration and an {@link Executor} for handling I/O events. If
	 * a null {@link Executor} is provided, a default one will be created using
	 * {@link Executors#newCachedThreadPool()}.
	 * 
	 * @param sessionConfig
	 *            the default configuration for the managed {@link IoSession}
	 * @param executor
	 *            the {@link Executor} used for handling execution of I/O
	 *            events. Can be <code>null</code>.
	 */
	protected AbstractIoService(IoSessionConfig sessionConfig, Executor executor) {
		if (sessionConfig == null) {
			throw new IllegalArgumentException("sessionConfig");
		}

		if (getTransportMetadata() == null) {
			throw new IllegalArgumentException("TransportMetadata");
		}

		if (!getTransportMetadata().getSessionConfigType().isAssignableFrom(sessionConfig.getClass())) {
			throw new IllegalArgumentException("sessionConfig type: " + sessionConfig.getClass() + " (expected: "
					+ getTransportMetadata().getSessionConfigType() + ")");
		}

		// Create the listeners, and add a first listener : a activation
		// listener
		// for this service, which will give information on the service state.
		listeners = new IoServiceListenerSupport(this);
		listeners.add(serviceActivationListener);

		// Stores the given session configuration
		this.sessionConfig = sessionConfig;

		// Make JVM load the exception monitor before some transports
		// change the thread context class loader.
		ExceptionMonitor.getInstance();

		// 它内部的Executor可以选择是从外部传递进构造函数中，也可以在实例内部自行构造，若是后者，则它将是ThreadPoolExecutor类的一个实例，即是Executor线程池中的一员。
		if (executor == null) {
			this.executor = Executors.newCachedThreadPool();
			createdExecutor = true;
		} else {
			this.executor = executor;
			createdExecutor = false;
		}

		threadName = getClass().getSimpleName() + '-' + id.incrementAndGet();
	}

	/**
	 * {@inheritDoc}
	 */
	public final IoFilterChainBuilder getFilterChainBuilder() {
		return filterChainBuilder;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setFilterChainBuilder(IoFilterChainBuilder builder) {
		if (builder == null) {
			builder = new DefaultIoFilterChainBuilder();
		}
		filterChainBuilder = builder;
	}

	/**
	 * {@inheritDoc}
	 */
	public final DefaultIoFilterChainBuilder getFilterChain() {
		if (filterChainBuilder instanceof DefaultIoFilterChainBuilder) {
			return (DefaultIoFilterChainBuilder) filterChainBuilder;
		}

		throw new IllegalStateException("Current filter chain builder is not a DefaultIoFilterChainBuilder.");
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addListener(IoServiceListener listener) {
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeListener(IoServiceListener listener) {
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isActive() {
		return listeners.isActive();
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isDisposing() {
		return disposing;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isDisposed() {
		return disposed;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void dispose() {
		dispose(false);
	}

	/**
	 * 在释放资源的方法时，首先去获取释放锁disposalLock才行，然后具体的释放动作是通过dispose0完成的，接着取消掉空闲检查线程，此外，
	 * 若线程是内部创建的线程池中的一员，则通过线程池去关闭线程。 
	 * {@inheritDoc}
	 */
	public final void dispose(boolean awaitTermination) {
		if (disposed) {
			return;
		}

		synchronized (disposalLock) {
			if (!disposing) {
				//获取释放锁
				disposing = true;

				try {
					//具体释放动作
					dispose0();
				} catch (Exception e) {
					ExceptionMonitor.getInstance().exceptionCaught(e);
				}
			}
		}

		if (createdExecutor) {
			//通过线程池去关闭线程
			ExecutorService e = (ExecutorService) executor;
			e.shutdownNow();
			if (awaitTermination) {

				// Thread.currentThread().setName();

				try {
					LOGGER.debug("awaitTermination on {} called by thread=[{}]", this, Thread.currentThread().getName());
					e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
					LOGGER.debug("awaitTermination on {} finished", this);
				} catch (InterruptedException e1) {
					LOGGER.warn("awaitTermination on [{}] was interrupted", this);
					// Restore the interrupted status
					Thread.currentThread().interrupt();
				}
			}
		}
		disposed = true;
	}

	/**
	 * Implement this method to release any acquired resources. This method is
	 * invoked only once by {@link #dispose()}.
	 */
	protected abstract void dispose0() throws Exception;

	/**
	 * {@inheritDoc}
	 */
	public final Map<Long, IoSession> getManagedSessions() {
		return listeners.getManagedSessions();
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getManagedSessionCount() {
		return listeners.getManagedSessionCount();
	}

	/**
	 * {@inheritDoc}
	 */
	public final IoHandler getHandler() {
		return handler;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setHandler(IoHandler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("handler cannot be null");
		}

		if (isActive()) {
			throw new IllegalStateException("handler cannot be set while the service is active.");
		}

		this.handler = handler;
	}

	/**
	 * {@inheritDoc}
	 */
	public IoSessionConfig getSessionConfig() {
		return sessionConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public final IoSessionDataStructureFactory getSessionDataStructureFactory() {
		return sessionDataStructureFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setSessionDataStructureFactory(IoSessionDataStructureFactory sessionDataStructureFactory) {
		if (sessionDataStructureFactory == null) {
			throw new IllegalArgumentException("sessionDataStructureFactory");
		}

		if (isActive()) {
			throw new IllegalStateException("sessionDataStructureFactory cannot be set while the service is active.");
		}

		this.sessionDataStructureFactory = sessionDataStructureFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public IoServiceStatistics getStatistics() {
		return stats;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long getActivationTime() {
		return listeners.getActivationTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public final Set<WriteFuture> broadcast(Object message) {
		// Convert to Set. We do not return a List here because only the
		// direct caller of MessageBroadcaster knows the order of write
		// operations.
		final List<WriteFuture> futures = IoUtil.broadcast(message, getManagedSessions().values());
		return new AbstractSet<WriteFuture>() {
			@Override
			public Iterator<WriteFuture> iterator() {
				return futures.iterator();
			}

			@Override
			public int size() {
				return futures.size();
			}
		};
	}

	public final IoServiceListenerSupport getListeners() {
		return listeners;
	}

	protected final void executeWorker(Runnable worker) {
		executeWorker(worker, null);
	}

	protected final void executeWorker(Runnable worker, String suffix) {
		String actualThreadName = threadName;
		if (suffix != null) {
			actualThreadName = actualThreadName + '-' + suffix;
		}
		executor.execute(new NamePreservingRunnable(worker, actualThreadName));
	}

	// TODO Figure out make it work without causing a compiler error / warning.
	@SuppressWarnings("unchecked")
	protected final void initSession(IoSession session, IoFuture future, IoSessionInitializer sessionInitializer) {
		// Update lastIoTime if needed.
		if (stats.getLastReadTime() == 0) {
			stats.setLastReadTime(getActivationTime());
		}

		if (stats.getLastWriteTime() == 0) {
			stats.setLastWriteTime(getActivationTime());
		}

		// Every property but attributeMap should be set now.
		// Now initialize the attributeMap. The reason why we initialize
		// the attributeMap at last is to make sure all session properties
		// such as remoteAddress are provided to IoSessionDataStructureFactory.
		try {
			//会话初始化完成后的动作每个session都保持有自己的属性映射图，在会话结束初始化时，应该设置这个AttributeMap
			((AbstractIoSession) session).setAttributeMap(session.getService().getSessionDataStructureFactory().getAttributeMap(session));
		} catch (IoSessionInitializationException e) {
			throw e;
		} catch (Exception e) {
			throw new IoSessionInitializationException("Failed to initialize an attributeMap.", e);
		}

		try {
			//应该为会话配置写请求队列
			((AbstractIoSession) session).setWriteRequestQueue(session.getService().getSessionDataStructureFactory().getWriteRequestQueue(session));
		} catch (IoSessionInitializationException e) {
			throw e;
		} catch (Exception e) {
			throw new IoSessionInitializationException("Failed to initialize a writeRequestQueue.", e);
		}

		//在初始化时会在会话的属性中加入一项SESSION_CREATED_FUTURE，这个属性会在连接真正建立后从会话中去除
		if ((future != null) && (future instanceof ConnectFuture)) {
			// DefaultIoFilterChain will notify the future. (We support
			// ConnectFuture only for now).
			session.setAttribute(DefaultIoFilterChain.SESSION_CREATED_FUTURE, future);
		}

		if (sessionInitializer != null) {
			sessionInitializer.initializeSession(session, future);
		}

		finishSessionInitialization0(session, future);
	}

	/**
	 * Implement this method to perform additional tasks required for session
	 * initialization. Do not call this method directly;
	 * {@link #initSession(IoSession, IoFuture, IoSessionInitializer)} will call
	 * this method instead.
	 */
	protected void finishSessionInitialization0(IoSession session, IoFuture future) {
		// Do nothing. Extended class might add some specific code
	}

	/** 预设服务的操作*/
	protected static class ServiceOperationFuture extends DefaultIoFuture {
		public ServiceOperationFuture() {
			super(null);
		}

		public final boolean isDone() {
			return getValue() == Boolean.TRUE;
		}

		public final void setDone() {
			setValue(Boolean.TRUE);
		}

		public final Exception getException() {
			if (getValue() instanceof Exception) {
				return (Exception) getValue();
			}

			return null;
		}

		public final void setException(Exception exception) {
			if (exception == null) {
				throw new IllegalArgumentException("exception");
			}
			setValue(exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getScheduledWriteBytes() {
		return stats.getScheduledWriteBytes();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getScheduledWriteMessages() {
		return stats.getScheduledWriteMessages();
	}

}
