// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// http://code.google.com/p/protobuf/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

// TODO(kenton):  Use generics?  E.g. Builder<BuilderType extends Builder>, then
//   mergeFrom*() could return BuilderType for better type-safety.

package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 实施协议消息对象的抽象接口。
 * 
 * 这个接口实现所有协议消息的对象。此外非-
 * Lite的消息实现消息的接口，这是一个子类的MessageLite。使用MessageLite，而不是当你只需要它支持的功能的一个子集 -
 * 即没有使用描述符或反射。您可以指示该协议的编译器来生成类实施MessageLite，不完整的邮件界面，通过添加后续行的原始文件。：
 * 
 * <pre>
 * option optimize_for = LITE_RUNTIME;
 * </pre>
 * 
 * <p>
 * 这是资源约束的完整的协议缓冲区运行时库是太大的系统尤其有用。
 * 
 * <p>
 * 请注意，非约束系统（如服务器）当你需要连接大量的协议定义，一个更好的方式来降低总的代码足迹的是使用
 * {@code optimize_for = CODE_SIZE}
 * 。这将使得生成的代码更小，同时仍然支持所有相同的功能（速度为代价）。当你只有一个链接到你的二进制文件，在这种情况下，
 * 该协议的大小的缓冲区运行时本身就是最大的问题的消息类型的少数{@code optimize_for = LITE_RUNTIME}是最好的。
 * 
 * @author kenton@google.com Kenton Varda
 */
public interface MessageLite extends MessageLiteOrBuilder {

	/**
	 * 序列化的消息，并将其写入到输出。 这不刷新或关闭流。
	 */
	void writeTo(CodedOutputStream output) throws IOException;

	/**
	 * 获取需要编码的这个消息的字节数。 其结果是只计算在第一次调用后memoized。
	 */
	int getSerializedSize();

	// -----------------------------------------------------------------
	// Convenience methods.

	/**
	 * 一个{@code ByteString}序列化消息，并返回它。这仅仅是一个{@link #writeTo(CodedOutputStream)}
	 * 周围的琐碎包装
	 */
	ByteString toByteString();

	/**
	 * 消息序列化到一个{@code byte}数组，并返回它。 这仅仅是一个{@link #writeTo(CodedOutputStream)}
	 * 周围的琐碎包装。
	 */
	byte[] toByteArray();

	/**
	 * 序列化的消息，并将其写入到输出。这仅仅是一个{@link #writeTo(CodedOutputStream)}
	 * 周围的琐碎包装。这不刷新或关闭流。
	 * <p>
	 * 注：协议缓冲器自我划定。因此，如果你写更多的数据流的消息后，你必须设法确保在接收端的分析器不解释作为该协议的消息的一部分。这是可以做到，
	 * 例如前的数据通过写邮件的大小 ，然后确保该大小限制在接收端（如包装于一体的InputStream输入限制）的输入。另外，只需使用
	 * {@link #writeDelimitedTo(OutputStream)}.
	 */
	void writeTo(OutputStream output) throws IOException;

	/**
	 * 类似 {@link #writeTo(OutputStream)}
	 * ，但作为一个varint写入写入数据之前，邮件的大小。这使得更多的数据被写入的消息后 ，而不需要自己划定的消息数据流。使用
	 * {@link Builder#mergeDelimitedFrom(InputStream)}（或静态方法
	 * {@code YourMessageType.parseDelimitedFrom(InputStream)}）来分析此方法写入的消息。
	 */
	void writeDelimitedTo(OutputStream output) throws IOException;

	// =================================================================
	// Builders

	/**
	 * 为此消息的同一类型的消息，构造一个新的{@code builder}。
	 */
	Builder newBuilderForType();

	/**
	 * 构造一个{@code builder} 初始化 当前的消息。从当前派生一个新消息。
	 */
	Builder toBuilder();

	/**
	 * 协议消息的{@code builder}抽象的接口实施
	 */
	interface Builder extends MessageLiteOrBuilder, Cloneable {
		/** 重置所有字段到默认值。 */
		Builder clear();

		/**
		 * 构建最终的消息。一旦调用了此方法，这个Builder将不再有效，并且调用任何其他方法，将导致未定义的行为
		 * 可能抛出<b>NullPointerException</b>。如果你需要调用{@code build()}
		 * 之后能继续工作，那么首先你得先调用 {@code clone()}。
		 * 
		 * @throws UninitializedMessageException
		 *             该消息{@link #isInitialized()}时缺少一个或多个所需的字段返回<b>false</b>)。
		 *             使用{@link #buildPartial()}绕过此检查。
		 */
		MessageLite build();

		/**
		 * 类似 {@link #build()}, 如果消息是缺少必需的字段，返回部分消息，并不抛出异常。
		 * 一旦调用了此方法，这个Builder将不再有效，并且调用任何其他方法，将导致未定义的行为
		 * 可能抛出NullPointerException。如果你需要调用{@code buildPartial()}
		 * 之后能继续工作，那么首先你得先调用 {@code clone()}。
		 */
		MessageLite buildPartial();

		/**
		 * 克隆{@code Builder()}.
		 * 
		 * @see Object#clone()
		 */
		Builder clone();

		/**
		 * 因为如果使用{@link Builder#mergeFrom(MessageLite)}解析输入这种类型的消息，所以融合了这个消息。
		 * <p>
		 * 
		 * 警告：这不验证所有必需的字段输入消息中。 如果您调用{@link #build()}（不设置所有必需的字段）， 它会抛出一个
		 * {@link UninitializedMessageException} 这是一个{@code RuntimeException}
		 * ，因此可能无法捕获。 有几个好办法来处理这个：
		 * <ul>
		 * <li>
		 * 调用{@link #isInitialized()}，以确认所有必需的字段之前建设。
		 * <li>
		 * 解析消息单独使用静态的{@code parseFrom}方法之一， 然后使用{@link #mergeFrom(MessageLite)}
		 * 这个合并。 parseFrom将抛出一个{@link InvalidProtocolBufferException}
		 * ，如果缺少一些必要的领域。
		 * <li>
		 * 使用{@code buildPartial()}来构建， 忽略缺少必需的领域。
		 * </ul>
		 * 
		 * <p>
		 * Note: The caller should call
		 * {@link CodedInputStream#checkLastTagWas(int)} after calling this to
		 * verify that the last tag seen was the appropriate end-group tag, or
		 * zero for EOF.
		 */
		Builder mergeFrom(CodedInputStream input) throws IOException;

		/**
		 * 如同 {@link Builder#mergeFrom(CodedInputStream)}，同时也解析扩展。
		 * 要能够解析的扩展，必须先注册{@code extensionRegistry}。 在注册表中的扩展，将被视为未知领域。
		 */
		Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException;

		// ---------------------------------------------------------------
		// Convenience methods.

		/**
		 * 作为这种类型的消息{@code data}解析和合并与正在兴建的消息中。 这仅仅是一个
		 * {@link #mergeFrom(CodedInputStream)}周围的小包装。
		 * 
		 * @return this
		 */
		Builder mergeFrom(ByteString data) throws InvalidProtocolBufferException;

		/**
		 * 作为这种类型的消息{@code data}解析和合并与正在兴建的消息中。 这仅仅是一个mergeFrom周围的小包装
		 * {@link #mergeFrom(CodedInputStream,ExtensionRegistry)}。
		 * 
		 * @return this
		 */
		Builder mergeFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

		/**
		 * 作为这种类型的消息{@code data}解析和合并与正在兴建的消息中。 这仅仅是一个
		 * {@link #mergeFrom(CodedInputStream)}周围的小包装。
		 * 
		 * @return this
		 */
		Builder mergeFrom(byte[] data) throws InvalidProtocolBufferException;

		/**
		 * 作为这种类型的消息{@code data}解析和合并与正在兴建的消息中。 这仅仅是一个
		 * {@link #mergeFrom(CodedInputStream)}周围的小包装。
		 * 
		 * @return this
		 */
		Builder mergeFrom(byte[] data, int off, int len) throws InvalidProtocolBufferException;

		/**
		 * 作为这种类型的消息{@code data}解析和合并与正在兴建的消息中。 这仅仅是一个mergeFrom周围的小包装
		 * {@link #mergeFrom(CodedInputStream,ExtensionRegistry)}。
		 * 
		 * @return this
		 */
		Builder mergeFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

		/**
		 * 作为这种类型的消息{@code data}解析和合并与正在兴建的消息中。 这仅仅是一个mergeFrom周围的小包装
		 * {@link #mergeFrom(CodedInputStream,ExtensionRegistry)}。
		 * 
		 * @return this
		 */
		Builder mergeFrom(byte[] data, int off, int len, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

		/**
		 * 从{@code input}解析这种类型的消息，合并与正在兴建的消息。 这仅仅是一个
		 * {@link #mergeFrom(CodedInputStream)}周围的小包装。
		 * 请注意，此方法始终读取<i>整个</i>输入（除非它抛出一个异常）。 如果你想让它停止较早， 你将需要用您的输入限制阅读一些包装流。或者，
		 * 使用{@link MessageLite#writeDelimitedTo(OutputStream)}写您的消息，
		 * {@link #mergeDelimitedFrom(InputStream)}来读取。
		 * <p>
		 * 尽管通常读取整个输入，这不关闭流。
		 * 
		 * @return this
		 */
		Builder mergeFrom(InputStream input) throws IOException;

		/**
		 * 从{@code input}解析这种类型的消息，合并与正在兴建的消息。 这仅仅是一个<b>mergeFrom</b>周围的小包装
		 * {@link #mergeFrom(CodedInputStream,ExtensionRegistry)}。
		 * 
		 * @return this
		 */
		Builder mergeFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException;

		/**
		 * 如同{@link #mergeFrom(InputStream)}，
		 * 但不读取直到EOF的。相反，消息（编码为一个varint）的大小是先读 然后消息数据。 使用
		 * {@link MessageLite#writeDelimitedTo(OutputStream)}这种格式写消息
		 * 
		 * @returns 如果成功返回<b>true</b>，如果
		 *          如果流的方法启动时，在EOF。任何其他错误（包括在解析过程中达成的EOF）将导致一个异常被抛出
		 *          ，则返回<b>false</b>。
		 */
		boolean mergeDelimitedFrom(InputStream input) throws IOException;

		/**
		 * 如同{@link #mergeDelimitedFrom(InputStream)}但支持扩展。
		 */
		boolean mergeDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException;
	}
}
