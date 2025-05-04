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
package org.apache.mina.core.polling;

import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.util.ExceptionMonitor;

/**
 * 
 * 继承自AbstractIoAcceptor,两个泛型参数分别是所处理的会话和服务器端socket连接.底层的sockets会被不断检测,
 * 并当有任何一个socket需要被处理时就会被唤醒去处理
 * .这个类封装了服务器端socket的bind,accept和dispose等动作,其成员变量Executor负责接受来自客户端的连接请求
 * ,另一个AbstractPollingIoProcessor用于处理客户端的I/O操作请求,如读写和关闭连接.
 * (所有的约束力，接受，收盘水平低的方法，需要提供由子类实现。)
 * 
 * @see NioSocketAcceptor for a example of implementation
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public abstract class AbstractPollingIoAcceptor<S extends AbstractIoSession, H> extends AbstractIoAcceptor {

	private final IoProcessor<S> processor;

	private final boolean createdProcessor;

	/** 注册队列 */
	private final Queue<AcceptorOperationFuture> registerQueue = new ConcurrentLinkedQueue<AcceptorOperationFuture>();

	/** 取消注册队列 */
	private final Queue<AcceptorOperationFuture> cancelQueue = new ConcurrentLinkedQueue<AcceptorOperationFuture>();

	/** 本地地址到服务器socket的映射表 */
	private final Map<SocketAddress, H> boundHandles = Collections.synchronizedMap(new HashMap<SocketAddress, H>());

	/** 预设一些服务器的操作 */
	private final ServiceOperationFuture disposalFuture = new ServiceOperationFuture();

	/** 一个标志设置时，承兑人已创建并初始化 */
	private volatile boolean selectable;

	/** 该线程负责接受传入的请求 */
	private AtomicReference<Acceptor> acceptorRef = new AtomicReference<Acceptor>();

	/** 是否复用地址。默认不复用 */
	protected boolean reuseAddress = false;

	/**
	 * 定义的socket数目，可以等待被接受。默认为50（如SocketServer默认值）。
	 */
	protected int backlog = 50;

	/**
	 * AbstractPollingIoAcceptor的构造函数。您需要提供一个默认的会话配置，
	 * 一类的IoProcessor将在SimpleIoProcessorPool}实例化更好的多处理器系统的结垢。默认的池大小将被使用
	 * 
	 * @see SimpleIoProcessorPool
	 * 
	 * @param sessionConfig
	 *            托管IoSession的默认配置
	 * @param processorClass
	 *            为关联的IoSession整机采用的IoProcessor类
	 */
	protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Class<? extends IoProcessor<S>> processorClass) {
		this(sessionConfig, null, new SimpleIoProcessorPool<S>(processorClass), true);
	}

	/**
	 * AbstractPollingIoAcceptor的构造函数。您需要提供一个默认的会话配置，
	 * 一类的IoProcessor将在SimpleIoProcessorPool实例化更好地利用多线程多处理器系统扩展。
	 * 
	 * @see SimpleIoProcessorPool
	 * 
	 * @param sessionConfig
	 *            托管IoSession的默认配置
	 * @param processorClass
	 *            为关联的IoSession整机采用的IoProcessor类
	 * @param processorCount
	 *            处理器池实例的数量
	 */
	protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Class<? extends IoProcessor<S>> processorClass, int processorCount) {
		this(sessionConfig, null, new SimpleIoProcessorPool<S>(processorClass, processorCount), true);
	}

	/**
	 * AbstractPollingIoAcceptor的构造函数。您需要提供一个默认的会话配置，默认执行人将创建使用Executors.
	 * newCachedThreadPool()。
	 * 
	 * {@see AbstractIoService#AbstractIoService(IoSessionConfig, Executor)}
	 * 
	 * @param sessionConfig
	 *            托管IoSession的默认配置
	 * @param processor
	 *            为处理这个IoSession传输的 IoProcessor，触发事件的绑定和处理IoFilter的锁链
	 */
	protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, IoProcessor<S> processor) {
		this(sessionConfig, null, processor, false);
	}

	/**
	 * AbstractPollingIoAcceptor的构造函数。您需要提供一个默认的会话配置和处理I/
	 * O事件的执行人。如果一个空执行人提供，默认将创建一个使用Executors.newCachedThreadPool()。
	 * 
	 * {@see AbstractIoService#AbstractIoService(IoSessionConfig, Executor)}
	 * 
	 * @param sessionConfig
	 *            托管IoSession的默认配置
	 * @param executor
	 *            执行人用于处理的I/ O事件的异步执行。可以为null。
	 * @param processor
	 *            为处理这个IoSession传输的 IoProcessor，触发事件的绑定和处理IoFilter的锁链
	 */
	protected AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Executor executor, IoProcessor<S> processor) {
		this(sessionConfig, executor, processor, false);
	}

	/**
	 * AbstractPollingIoAcceptor的构造函数。You need to provide a default session
	 * configuration and an {@link Executor} for handling I/O events. If a null
	 * {@link Executor} is provided, a default one will be created using
	 * {@link Executors#newCachedThreadPool()}.
	 * 
	 * {@see AbstractIoService#AbstractIoService(IoSessionConfig, Executor)}
	 * 
	 * @param sessionConfig
	 *            托管IoSession的默认配置
	 * @param executor
	 *            执行人用于处理的I/ O事件的异步执行。可以为null。
	 * @param processor
	 *            为处理这个IoSession传输的 IoProcessor，触发事件的绑定和处理IoFilter的锁链
	 * @param createdProcessor
	 *            标记为自动创建的处理器，因此它会自动处理
	 */
	private AbstractPollingIoAcceptor(IoSessionConfig sessionConfig, Executor executor, IoProcessor<S> processor, boolean createdProcessor) {
		super(sessionConfig, executor);

		if (processor == null) {
			throw new IllegalArgumentException("processor");
		}

		this.processor = processor;
		this.createdProcessor = createdProcessor;

		try {
			// 初始化selector
			init();

			// 现在的选择器是准备好的，我们可以切换的标志为true，以便可以接受传入的连接
			selectable = true;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeIoException("Failed to initialize.", e);
		} finally {
			if (!selectable) {
				try {
					destroy();
				} catch (Exception e) {
					ExceptionMonitor.getInstance().exceptionCaught(e);
				}
			}
		}
	}

	/**
	 * 初始化轮询系统， 将被称为在施工时间。
	 * 
	 * @throws Exception
	 *             any exception thrown by the underlying system calls
	 */
	protected abstract void init() throws Exception;

	/**
	 * 摧毁轮询系统，将这个IoAcceptor时调用的实施，将予以毁弃。
	 * 
	 * @throws Exception
	 *             any exception thrown by the underlying systems calls
	 */
	protected abstract void destroy() throws Exception;

	/**
	 * 
	 * 接受检查的连接， 中断时，至少一台服务器负责接收做好准备。 准备好所有的服务器套接字描述符需要由selectedHandles（）返回
	 * 
	 * @return The number of sockets having got incoming client
	 * @throws Exception
	 *             any exception thrown by the underlying systems calls
	 */
	protected abstract int select() throws Exception;

	/**
	 * 中断的select（）方法。 轮询时使用的设置需要进行修改。
	 */
	protected abstract void wakeup();

	/**
	 * 有关服务器套接字集的Iterator发现与在接受最后传入连接的select()调用
	 * 
	 * @return 返回服务器处理就绪的列表
	 */
	protected abstract Iterator<H> selectedHandles();

	/**
	 * 打开一个给定的本地地址的服务器套接字。
	 * 
	 * @param localAddress
	 *            相关的本地地址
	 * @return 打开服务器套接字
	 * @throws Exception
	 *             由底层系统调用抛出的任何异常
	 */
	protected abstract H open(SocketAddress localAddress) throws Exception;

	/**
	 * 获取本地地址与 一个给定的服务器套接字
	 * 
	 * @param handle
	 *            the server socket/服务器套接字
	 * @return the local {@link SocketAddress} associated with this handle
	 * @throws Exception
	 *             any exception thrown by the underlying systems calls
	 */
	protected abstract SocketAddress localAddress(H handle) throws Exception;

	/**
	 * 接受一个服务器套接字和客户端连接，并返回一个与给定IoProcessor相关的新IoSession
	 * 
	 * @param processor
	 *            为处理这个IoSession传输的 IoProcessor，触发事件的绑定和处理IoFilter的锁链
	 * @param handle
	 *            the server handle
	 * @return the created {@link IoSession}
	 * @throws Exception
	 *             any exception thrown by the underlying systems calls
	 */
	protected abstract S accept(IoProcessor<S> processor, H handle) throws Exception;

	/**
	 * 关闭一个服务的socket连接
	 * 
	 * @param handle
	 *            the server socket
	 * @throws Exception
	 *             由底层系统调用抛出的任何异常
	 */
	protected abstract void close(H handle) throws Exception;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void dispose0() throws Exception {
		unbind();

		startupAcceptor();
		wakeup();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final Set<SocketAddress> bindInternal(List<? extends SocketAddress> localAddresses) throws Exception {
		// 创建作为Future运作绑定的请求。当选择已经办理登记，这将标志着这一未来。
		AcceptorOperationFuture request = new AcceptorOperationFuture(localAddresses);

		// 将工作着的handle增加到注册请求在队列
		registerQueue.add(request);

		// 创建接受器实例并切有本地的executor执行他
		startupAcceptor();

		// 由于我们刚刚启动Acceptor， 我们要疏通select（），以便处理结合的要求，我们只需要添加到registerQueue。
		wakeup();

		// 现在，我们等待，直到完成该请求
		request.awaitUninterruptibly();

		if (request.getException() != null) {
			throw request.getException();
		}

		// 更新本地地址。setLocalAddresses（）不应该调用，因为会造成从工作线程死锁
		Set<SocketAddress> newLocalAddresses = new HashSet<SocketAddress>();

		for (H handle : boundHandles.values()) {
			newLocalAddresses.add(localAddress(handle));
		}

		return newLocalAddresses;
	}

	/**
	 * 如果Acceptor为空，Acceptor对象将被创建并由executor启动。如果Acceptor对象为null，
	 * 可能已经创建并且这个类已经工作中了，然后什么都不会发生，该方法将只返回。 ，
	 */
	private void startupAcceptor() {
		// 如果Acceptor是没有准备好，清除队列
		// TODO : 他们应该已经是干净的：我们必须做到这一点？
		if (!selectable) {
			registerQueue.clear();
			cancelQueue.clear();
		}

		// 如果Acceptor没有启动则启动他
		Acceptor acceptor = acceptorRef.get();

		if (acceptor == null) {
			acceptor = new Acceptor();

			if (acceptorRef.compareAndSet(null, acceptor)) {
				executeWorker(acceptor);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void unbind0(List<? extends SocketAddress> localAddresses) throws Exception {
		AcceptorOperationFuture future = new AcceptorOperationFuture(localAddresses);

		cancelQueue.add(future);
		startupAcceptor();
		wakeup();

		future.awaitUninterruptibly();
		if (future.getException() != null) {
			throw future.getException();
		}
	}

	/**
	 * 这个类是默认调用startupAcceptor（）方法，并放入一个NamePreservingRunnable类。
	 * 这是一个线程接受来自客户端的传入连接。 循环时停止所有绑定处理程序绑定。
	 */
	private class Acceptor implements Runnable {
		public void run() {
			assert (acceptorRef.get() == this);

			int nHandles = 0;

			while (selectable) {
				try {
					// Detect if we have some keys ready to be processed
					// The select() will be woke up if some new connection
					// have occurred, or if the selector has been explicitly
					// woke up
					int selected = select();

					// this actually sets the selector to OP_ACCEPT,
					// and binds to the port on which this class will
					// listen on
					nHandles += registerHandles();

					// Now, if the number of registred handles is 0, we can
					// quit the loop: we don't have any socket listening
					// for incoming connection.
					if (nHandles == 0) {
						acceptorRef.set(null);

						if (registerQueue.isEmpty() && cancelQueue.isEmpty()) {
							assert (acceptorRef.get() != this);
							break;
						}

						if (!acceptorRef.compareAndSet(null, this)) {
							assert (acceptorRef.get() != this);
							break;
						}

						assert (acceptorRef.get() == this);
					}

					if (selected > 0) {
						// We have some connection request, let's process
						// them here.
						processHandles(selectedHandles());
					}

					// check to see if any cancellation request has been made.
					nHandles -= unregisterHandles();
				} catch (ClosedSelectorException cse) {
					// If the selector has been closed, we can exit the loop
					break;
				} catch (Throwable e) {
					ExceptionMonitor.getInstance().exceptionCaught(e);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						ExceptionMonitor.getInstance().exceptionCaught(e1);
					}
				}
			}

			// Cleanup all the processors, and shutdown the acceptor.
			if (selectable && isDisposing()) {
				selectable = false;
				try {
					if (createdProcessor) {
						processor.dispose();
					}
				} finally {
					try {
						synchronized (disposalLock) {
							if (isDisposing()) {
								destroy();
							}
						}
					} catch (Exception e) {
						ExceptionMonitor.getInstance().exceptionCaught(e);
					} finally {
						disposalFuture.setDone();
					}
				}
			}
		}

		/**
		 * 处理来自客户端的连接请求：
		 * 这种方法将处理Worker类新的会话。 所有这一切都已经按照他们的状态更新的选择键。 selectedKeys（）方法将在这里进行处理。
		 * 只有键准备接受连接这里处理。
		 * <p/>
		 * Session对象创建新的实例操作为SocketSessionImpl，并通过Session对象的SocketIoProcessor类。
		 */
		@SuppressWarnings("unchecked")
		private void processHandles(Iterator<H> handles) throws Exception {
			while (handles.hasNext()) {
				H handle = handles.next();
				handles.remove();

				// 为一个服务器socket句柄handle真正接收来自客户端的请求，在给定的所关联的processor上返回会话session
				S session = accept(processor, handle);

				if (session == null) {
					break;
				}

				initSession(session, null, null);

				// 把这个session添加到SocketIoProcessor
				session.getProcessor().add(session);
			}
		}
	}

	/**
	 * 注册服务器sockets句柄: 建立了Socket通信。设置项目，如： 封锁重用地址接收缓冲区大小、绑定到监听端口寄存器 OP_ACCEPT
	 * for selector
	 */
	private int registerHandles() {
		for (;;) {
			// 该寄存器队列包含的服务列表管理此Acceptor
			AcceptorOperationFuture future = registerQueue.poll();

			if (future == null) {
				return 0;
			}

			// 我们创建了一个临时的映射来存储绑定的处理，因为我们可能必须删除所有，如果有一个开放的Socket时例外。
			Map<SocketAddress, H> newHandles = new ConcurrentHashMap<SocketAddress, H>();
			List<SocketAddress> localAddresses = future.getLocalAddresses();

			try {
				// 处理中过程中的所有地址
				for (SocketAddress a : localAddresses) {
					// 打开指定地址，返回服务器socket句柄
					H handle = open(a);
					// 加入地址—服务器socket映射表中
					newHandles.put(localAddress(handle), handle);
				}

				// 更新本地绑定地址集：一切正常，我们现在可以更新map中存储的所有绑定套接字。
				boundHandles.putAll(newHandles);

				// 完成注册过程
				future.setDone();
				return newHandles.size();
			} catch (Exception e) {
				// 我们存储在future的异常
				future.setException(e);
			} finally {
				// 如果没有绑定的所有地址，回滚操作
				if (future.getException() != null) {
					for (H handle : newHandles.values()) {
						try {
							// 关闭服务器socket句柄
							close(handle);
						} catch (Exception e) {
							ExceptionMonitor.getInstance().exceptionCaught(e);
						}
					}

					// TODO : add some comment : what is the wakeup() waking up?
					wakeup();
				}
			}
		}
	}

	/**
	 * 这种方法只检查是否有任何已被纳入取消队列中。 唯一应在cancelQueue对象是CancellationRequest
	 * 而唯一的地方出现这种情况是在doUnbind（）方法。
	 */
	private int unregisterHandles() {
		int cancelledHandles = 0;
		for (;;) {
			AcceptorOperationFuture future = cancelQueue.poll();
			if (future == null) {
				break;
			}

			// close the channels
			for (SocketAddress a : future.getLocalAddresses()) {
				H handle = boundHandles.remove(a);

				if (handle == null) {
					continue;
				}

				try {
					close(handle);
					wakeup(); // wake up again to trigger thread death
				} catch (Throwable e) {
					ExceptionMonitor.getInstance().exceptionCaught(e);
				} finally {
					cancelledHandles++;
				}
			}

			future.setDone();
		}

		return cancelledHandles;
	}

	/**
	 * {@inheritDoc}
	 */
	public final IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getBacklog() {
		return backlog;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBacklog(int backlog) {
		synchronized (bindLock) {
			if (isActive()) {
				throw new IllegalStateException("backlog can't be set while the acceptor is bound.");
			}

			this.backlog = backlog;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isReuseAddress() {
		return reuseAddress;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setReuseAddress(boolean reuseAddress) {
		synchronized (bindLock) {
			if (isActive()) {
				throw new IllegalStateException("backlog can't be set while the acceptor is bound.");
			}

			this.reuseAddress = reuseAddress;
		}
	}
}
