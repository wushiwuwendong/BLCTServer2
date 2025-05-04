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

import java.util.Map;
import java.util.Set;

import org.apache.mina.core.IoUtil;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionDataStructureFactory;

/**
 * Base interface for all {@link IoAcceptor}s and {@link IoConnector}s that
 * provide I/O service and manage {@link IoSession}s.
 * 是所有IoAcceptor和IoConnector的基接口
 * 1.1.底层的元数据信息TransportMetadata，比如底层的网络服务提供（NIO,ARP,RXTX等）；
 * 1.2.通过这个服务创建一个新会话时，新会话的默认配置IoSessionConfig； 1.3.此服务所管理的所有会话；
 * 1.4.与这个服务相关所产生的事件所对应的监听者（IoServiceListener）；
 * 1.5.处理这个服务所管理的所有连接的处理器(IoHandler)；
 * 1.6.每个会话都有一个过滤器链（IoFilterChain），每个过滤器链通过其对应的IoFilterChainBuilder来负责构建；
 * 1.7.由于此服务管理了一系列会话
 * ，因此可以通过广播的方式向所有会话发送消息,返回结果是一个WriteFuture集，后者是一种表示未来预期结果的数据结构；
 * 1.8.服务创建的会话（IoSession）相关的数据通过IoSessionDataStructureFactory来提供；
 * 1.9.发送消息时有一个写缓冲队列。 1.10.服务的闲置状态有三种：读端空闲，写端空闲，双端空闲。
 * 1.11.还提供服务的一些统计信息，比如时间，数据量等。 IoService这个服务是对于服务器端的接受连接和客户端发起连接这两种行为的抽象。
 * 
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface IoService {
	/**
	 * 返回这项服务运行的{@link TransportMetadata}。
	 */
	TransportMetadata getTransportMetadata();

	/**
	 * 添加一个{@link IoServiceListener}侦听与此服务有关的任何事件。
	 */
	void addListener(IoServiceListener listener);

	/**
	 * 删除现有{@link IoServiceListener}监听与此服务有关的任何事件。
	 */
	void removeListener(IoServiceListener listener);

	/**
	 * 返回<tt>true</tt>，如果只是{@link #dispose()}方法被调用。 请注意，此方法将返回<tt>true</tt>
	 * ，即使所有相关的资源被释放。
	 */
	boolean isDisposing();

	/**
	 * 返回<tt>true</tt>如果只有这种处理器的所有资源，如果​​处理完毕。
	 */
	boolean isDisposed();

	/**
	 * 释放这项服务分配的任何资源。请注意，此方法可能会阻止只要有这种服务管理的任何会议。
	 */
	void dispose();

	/**
	 * 释放这项服务分配的任何资源。 请注意，此方法可能会阻止只要有这种服务管理的任何会议。 警告：调用IoFutureListener此方法与
	 * <code>awaitTermination</code>= true ，将可能导致死锁。
	 * 
	 * @param awaitTermination
	 *            When true this method will block until the underlying
	 *            ExecutorService is terminated
	 */
	void dispose(boolean awaitTermination);

	/**
	 * 返回的handler 将处理此服务管理所有连接。
	 */
	IoHandler getHandler();

	/**
	 * Sets the handler which will handle all connections managed by this
	 * service.
	 */
	void setHandler(IoHandler handler);

	/**
	 * 返回目前这项服务管理的所有会话的地图。 地图的<br>
	 * key</br>是{@link IoSession#getId() ID}。
	 * 
	 * @return the sessions. An empty collection if there's no session.
	 */
	Map<Long, IoSession> getManagedSessions();

	/**
	 * 返回目前这项服务管理的所有会话的数量。
	 */
	int getManagedSessionCount();

	/**
	 * 返回此服务创造了新的{@link IoSession}的默认配置。
	 */
	IoSessionConfig getSessionConfig();

	/**
	 * Returns the {@link IoFilterChainBuilder} which will build the
	 * {@link IoFilterChain} of all {@link IoSession}s which is created by this
	 * service. The default value is an empty
	 * {@link DefaultIoFilterChainBuilder}.
	 */
	IoFilterChainBuilder getFilterChainBuilder();

	/**
	 * Sets the {@link IoFilterChainBuilder} which will build the
	 * {@link IoFilterChain} of all {@link IoSession}s which is created by this
	 * service. If you specify <tt>null</tt> this property will be set to an
	 * empty {@link DefaultIoFilterChainBuilder}.
	 */
	void setFilterChainBuilder(IoFilterChainBuilder builder);

	/**
	 * A shortcut for <tt>( ( DefaultIoFilterChainBuilder ) </tt>
	 * {@link #getFilterChainBuilder()}<tt> )</tt>. Please note that the
	 * returned object is not a <b>real</b> {@link IoFilterChain} but a
	 * {@link DefaultIoFilterChainBuilder}. Modifying the returned builder won't
	 * affect the existing {@link IoSession}s at all, because
	 * {@link IoFilterChainBuilder}s affect only newly created {@link IoSession}
	 * s.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link IoFilterChainBuilder} is not a
	 *             {@link DefaultIoFilterChainBuilder}
	 */
	DefaultIoFilterChainBuilder getFilterChain();

	/**
	 * Returns a value of whether or not this service is active
	 * 
	 * @return whether of not the service is active.
	 */
	boolean isActive();

	/**
	 * Returns the time when this service was activated. It returns the last
	 * time when this service was activated if the service is not active now.
	 * 
	 * @return The time by using {@link System#currentTimeMillis()}
	 */
	long getActivationTime();

	/**
	 * 将指定的{@code message}写入到本服务的所有管理{@link IoSession}。 这种方法是一个
	 * {@link IoUtil#broadcast(Object, Collection)}。
	 */
	Set<WriteFuture> broadcast(Object message);

	/**
	 * 返回{@link IoSessionDataStructureFactory}提供这种服务创建一个新的会话相关的数据结构。
	 */
	IoSessionDataStructureFactory getSessionDataStructureFactory();

	/**
	 * Sets the {@link IoSessionDataStructureFactory} that provides related data
	 * structures for a new session created by this service.
	 */
	void setSessionDataStructureFactory(IoSessionDataStructureFactory sessionDataStructureFactory);

	/**
	 * 返回预定要写入的字节数
	 * 
	 * @return The number of bytes scheduled to be written
	 */
	int getScheduledWriteBytes();

	/**
	 * 返回预定要写入的消息数
	 * 
	 * @return The number of messages scheduled to be written
	 */
	int getScheduledWriteMessages();

	/**
	 * 返回此服务IoServiceStatistics对象。
	 * 
	 * @return The statistics object for this service.
	 */
	IoServiceStatistics getStatistics();
}
