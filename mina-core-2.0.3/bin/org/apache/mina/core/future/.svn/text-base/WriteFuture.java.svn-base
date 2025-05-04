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
 * 一种IoFuture，为了异步写入请求。
 *
 * <h3>Example</h3>
 * <pre>
 * IoSession session = ...;
 * WriteFuture future = session.write(...);
 * // Wait until the message is completely written out to the O/S buffer.
 * future.join();
 * if( future.isWritten() )
 * {
 *     // The message has been written successfully.
 * }
 * else
 * {
 *     // The messsage couldn't be written out completely for some reason.
 *     // (e.g. Connection is closed)
 * }
 * </pre>
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface WriteFuture extends IoFuture {
    /**
     * 如果写操作成功完成，返回true
     */
    boolean isWritten();
    
    /**
     * 返回写入失败的原因，当且仅当写操作失败，原因是一个例外。否则，则返回null。
     */
    Throwable getException();

    /**
     * 设置消息被写入，并通知所有等待线程这个未来。这个方法是调用MINA的内部。请不要直接调用此方法。
     */
    void setWritten();
    
    /**
     * 设置写入失败的原因，并通知所有等待线程这个未来。这个方法是调用MINA的内部。请不要直接调用此方法。
     */
    void setException(Throwable cause);

    /**
     * 等待异步操作完成。附加的听众时会通知操作完成。
     * 
     * @return 创建WriteFuture
     * @throws InterruptedException
     */
    WriteFuture await() throws InterruptedException;

    /**
     * {@inheritDoc}
     */
    WriteFuture awaitUninterruptibly();

    /**
     * {@inheritDoc}
     */
    WriteFuture addListener(IoFutureListener<?> listener);

    /**
     * {@inheritDoc}
     */
    WriteFuture removeListener(IoFutureListener<?> listener);
}
