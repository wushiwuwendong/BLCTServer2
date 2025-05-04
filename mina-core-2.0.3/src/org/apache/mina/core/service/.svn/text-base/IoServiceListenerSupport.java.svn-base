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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.ExceptionMonitor;

/**
 * 它提供了一个辅助类添加和IoServiceListener搬迁和射击事件。
 * 负责将IoService和其对应的各个IoServiceListener包装到一起进行管理 
 * A helper class which provides addition and removal of {@link IoServiceListener}s and firing events.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class IoServiceListenerSupport {
	/** The {@link IoService} that this instance manages. */
	private final IoService service;

	/** A list of {@link IoServiceListener}s. */
	private final List<IoServiceListener> listeners = new CopyOnWriteArrayList<IoServiceListener>();

	/** 被管理的会话集（其实就是服务所管理的会话集）Tracks managed sessions. */
	private final ConcurrentMap<Long, IoSession> managedSessions = new ConcurrentHashMap<Long, IoSession>();

	/** 上面的会话集的只读版 Read only version of {@link #managedSessions}. */
	private final Map<Long, IoSession> readOnlyManagedSessions = Collections.unmodifiableMap(managedSessions);

	/** 被管理的服务是否处于激活状态*/
	private final AtomicBoolean activated = new AtomicBoolean();

	/** listenerSupport激活的时间 */
	private volatile long activationTime;

	/**
	 * 用于存储管理以来，我们已启动listenerSupport最大会话A计数器
	 * A counter used to store the maximum sessions we managed since the
	 * listenerSupport has been activated
	 */
	private volatile int largestManagedSessionCount = 0;

	/**
	 * 一个全局计数器来计数的会话数自启动管理 
	 * A global counter to count the number of sessions managed since the start */
	private volatile long cumulativeManagedSessionCount = 0;

	/**
	 * 创建一个新实例的listenerSupport。
	 * Creates a new instance of the listenerSupport.
	 * 
	 * @param service
	 *            The associated IoService
	 */
	public IoServiceListenerSupport(IoService service) {
		if (service == null) {
			throw new IllegalArgumentException("service");
		}

		this.service = service;
	}

	/**
	 * Adds a new listener.
	 * 
	 * @param listener
	 *            The added listener
	 */
	public void add(IoServiceListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes an existing listener.
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	public void remove(IoServiceListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	/**
	 * @return The time (in ms) this instance has been activated
	 */
	public long getActivationTime() {
		return activationTime;
	}

	public Map<Long, IoSession> getManagedSessions() {
		return readOnlyManagedSessions;
	}

	public int getManagedSessionCount() {
		return managedSessions.size();
	}

	/**
	 * @return The largest number of managed session since the creation of this
	 *         listenerSupport
	 */
	public int getLargestManagedSessionCount() {
		return largestManagedSessionCount;
	}

	/**
	 * @return The total number of sessions managed since the initilization of
	 *         this ListenerSupport
	 */
	public long getCumulativeManagedSessionCount() {
		return cumulativeManagedSessionCount;
	}

	/**
	 * @return true if the instance is active
	 */
	public boolean isActive() {
		return activated.get();
	}

	/**
	 * Calls {@link IoServiceListener#serviceActivated(IoService)} for all
	 * registered listeners.
	 */
	public void fireServiceActivated() {
		if (!activated.compareAndSet(false, true)) {
			// The instance is already active
			return;
		}

		activationTime = System.currentTimeMillis();

		// Activate all the listeners now
		for (IoServiceListener listener : listeners) {
			try {
				listener.serviceActivated(service);
			} catch (Throwable e) {
				ExceptionMonitor.getInstance().exceptionCaught(e);
			}
		}
	}

	/**
	 * Calls {@link IoServiceListener#serviceDeactivated(IoService)} for all
	 * registered listeners.
	 */
	public void fireServiceDeactivated() {
		if (!activated.compareAndSet(true, false)) {
			// The instance is already desactivated
			return;
		}

		// Desactivate all the listeners
		try {
			for (IoServiceListener listener : listeners) {
				try {
					listener.serviceDeactivated(service);
				} catch (Throwable e) {
					ExceptionMonitor.getInstance().exceptionCaught(e);
				}
			}
		} finally {
			disconnectSessions();
		}
	}

	/**
	 * Calls {@link IoServiceListener#sessionCreated(IoSession)} for all
	 * registered listeners.
	 * 
	 * @param session
	 *            The session which has been created
	 */
	public void fireSessionCreated(IoSession session) {
		boolean firstSession = false;

		if (session.getService() instanceof IoConnector) {//若服务类型是Connector，则说明是客户端的连接服务
			synchronized (managedSessions) {//锁住当前已经建立的会话集
				firstSession = managedSessions.isEmpty();//看服务所管理的会话集是否为空集
			}
		}

		// 如果已经注册的，忽视
		if (managedSessions.putIfAbsent(session.getId(), session) != null) {
			return;
		}

		// 第一个连接会话，fire一个虚拟的服务激活事件
		if (firstSession) {
			fireServiceActivated();
		}

		
		IoFilterChain filterChain = session.getFilterChain();//呼叫过滤器的事件处理
		filterChain.fireSessionCreated();// 会话创建
		filterChain.fireSessionOpened();//会话打开

		int managedSessionCount = managedSessions.size();

		//统计管理的会话数目
		if (managedSessionCount > largestManagedSessionCount) {
			largestManagedSessionCount = managedSessionCount;
		}

		cumulativeManagedSessionCount++;

		//呼叫监听者的事件处理函数
		for (IoServiceListener l : listeners) {
			try {
				l.sessionCreated(session);
			} catch (Throwable e) {
				ExceptionMonitor.getInstance().exceptionCaught(e);
			}
		}
	}

	/**
	 * Calls {@link IoServiceListener#sessionDestroyed(IoSession)} for all
	 * registered listeners.
	 * 
	 * @param session
	 *            The session which has been destroyed
	 */
	public void fireSessionDestroyed(IoSession session) {
		// Try to remove the remaining empty session set after removal.
		if (managedSessions.remove(session.getId()) == null) {
			return;
		}

		// Fire session events.
		session.getFilterChain().fireSessionClosed();

		// Fire listener events.
		try {
			for (IoServiceListener l : listeners) {
				try {
					l.sessionDestroyed(session);
				} catch (Throwable e) {
					ExceptionMonitor.getInstance().exceptionCaught(e);
				}
			}
		} finally {
			// Fire a virtual service deactivation event for the last session of
			// the connector.
			if (session.getService() instanceof IoConnector) {
				boolean lastSession = false;

				synchronized (managedSessions) {
					lastSession = managedSessions.isEmpty();
				}

				if (lastSession) {
					fireServiceDeactivated();
				}
			}
		}
	}

	/**
	 * 断开连接会话，设置了一个监听锁，直到所有连接会话被关闭后才放开这个锁
	 * Close all the sessions TODO disconnectSessions.
	 * 
	 */
	private void disconnectSessions() {
		if (!(service instanceof IoAcceptor)) {//确保服务类型是IoAcceptor
			// We don't disconnect sessions for anything but an Acceptor
			return;
		}

		if (!((IoAcceptor) service).isCloseOnDeactivation()) {
			return;// IoAcceptor是否设置为在服务失效时关闭所有连接会话
		}

		//监听锁
		Object lock = new Object();
		IoFutureListener<IoFuture> listener = new LockNotifyingListener(lock);

		for (IoSession s : managedSessions.values()) {
			s.close(true).addListener(listener);//为每个会话的close动作增加一个监听者
		}

		try {
			synchronized (lock) {
				while (!managedSessions.isEmpty()) {//所管理的会话还没有全部结束，持锁等待
					lock.wait(500);
				}
			}
		} catch (InterruptedException ie) {
			// Ignored
		}
	}

	/**
	 * A listener in charge of releasing the lock when the close has been
	 * completed
	 */
	private static class LockNotifyingListener implements IoFutureListener<IoFuture> {
		private final Object lock;

		public LockNotifyingListener(Object lock) {
			this.lock = lock;
		}

		public void operationComplete(IoFuture future) {
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}
}
