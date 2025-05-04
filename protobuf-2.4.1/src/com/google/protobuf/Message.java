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
import java.util.Map;

/**
 * 实施协议消息对象的抽象接口。
 * <p>
 * 另请参阅{@link MessageLite}， 其中定义的方法，典型用户最关心。 这些方法不在"lite"运行时，增加{@link Message}
 * 的方法。 最大的增值功能是内省和反思-- i.e.， 消息类型的描述符和访问字段值动态。 which defines most of the
 * 
 * @author kenton@google.com Kenton Varda
 */
public interface Message extends MessageLite, MessageOrBuilder {

	// -----------------------------------------------------------------
	// Comparison and hashing

	/**
	 * 这种平等的消息比较指定的对象。 如果给定的对象是相同类型的消息{@code getDescriptorForType()}
	 * )定义和其所有字段相同的值，则返回<tt>true</tt>。 子类必须实现这一点; 继承的{@code Object.equals()}
	 * 是不正确的。
	 * 
	 * @param other
	 *            要比较平等与此消息的对象
	 * @return 如果指定的对象是等于这个消息，返回<tt>true</tt>
	 */
	@Override
	boolean equals(Object other);

	/**
	 * Returns the hash code value for this message. The hash code of a message
	 * should mix the message's type (object identity of the decsriptor) with
	 * its contents (known and unknown field values). Subclasses must implement
	 * this; inheriting {@code Object.hashCode()} is incorrect.
	 * 
	 * @return the hash code value for this message
	 * @see Map#hashCode()
	 */
	@Override
	int hashCode();

	// -----------------------------------------------------------------
	// Convenience methods.

	/**
	 * 协议缓冲区的文本格式的字符串转换消息。 这仅仅是微不足道的周围{@link TextFormat#printToString(Message)}。
	 */
	@Override
	String toString();

	// =================================================================
	// Builders

	// (From MessageLite, re-declared here only for return type covariance.)
	Builder newBuilderForType();

	Builder toBuilder();

	/**
	 * Abstract interface implemented by Protocol Message builders.
	 */
	interface Builder extends MessageLite.Builder, MessageOrBuilder {
		// (From MessageLite.Builder, re-declared here only for return type
		// covariance.)
		Builder clear();

		/**
		 * 合并到正在构建的{@code other}消息。 {@code other}必须有相同的类型，因为{@code this}(i.e.
		 * {@code getDescriptorForType() == other.getDescriptorForType()})。
		 * 合并发生如下。 对于每个字段：<br>
		 * *如果在{@code other}字段是奇异的原始字段， 然后{@code other}的值覆盖在此消息的价值。<br>
		 * *如果在{@code other}字段是奇异的消息字段，它被合并到相应的子消息使用相同的合并规则的消息。<br>
		 * *对于重复的字段，在此消息的元素与{@code other}元素连接在一起。 这相当于{@code Message::MergeFrom}
		 * 方法在C++。
		 */
		Builder mergeFrom(Message other);

		// (From MessageLite.Builder, re-declared here only for return type
		// covariance.)
		Message build();

		Message buildPartial();

		Builder clone();

		Builder mergeFrom(CodedInputStream input) throws IOException;

		Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException;

		/**
		 * 得到消息的类型的描述符。 参见{@link Message#getDescriptorForType()}。
		 */
		Descriptors.Descriptor getDescriptorForType();

		/**
		 * 创建一个给字段的相应类型的消息生成器。 这种内置的邮件，然后可以通过{@link Message#setField()}，
		 * {@link Message#setRepeatedField()}，或
		 * {@link Message#addRepeatedField()}。
		 */
		Builder newBuilderForField(Descriptors.FieldDescriptor field);

		/**
		 * 一个字段设置为给定值。 该值必须在这一领域的正确类型，即同一类型的
		 * {@link Message#getField(Descriptors.FieldDescriptor)}将返回。
		 */
		Builder setField(Descriptors.FieldDescriptor field, Object value);

		/**
		 * 清除字段。这是完全等同于调用生成的"clear"访问器方法对应到外地。
		 */
		Builder clearField(Descriptors.FieldDescriptor field);

		/**
		 * 设置重复的字段元素为给定值。 此字段的值必须是正确的类型， 即同一类型
		 * {@link Message#getRepeatedField(Descriptors.FieldDescriptor,int)}将返回。
		 * 
		 * @throws IllegalArgumentException
		 *             The field is not a repeated field, or
		 *             {@code field.getContainingType() != getDescriptorForType()}
		 *             .
		 */
		Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value);

		/**
		 * 如同{@code setRepeatedField}，但作为一个新的元素价值附加。
		 * element.
		 * 
		 * @throws IllegalArgumentException
		 *             The field is not a repeated field, or
		 *             {@code field.getContainingType() != getDescriptorForType()}
		 *             .
		 */
		Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value);

		/** 设置此消息为{@link UnknownFieldSet}。*/
		Builder setUnknownFields(UnknownFieldSet unknownFields);

		/**
		 * 合并到这个消息{@link UnknownFieldSet}一些未知字段。
		 */
		Builder mergeUnknownFields(UnknownFieldSet unknownFields);

		// ---------------------------------------------------------------
		// Convenience methods.

		// (From MessageLite.Builder, re-declared here only for return type
		// covariance.)
		Builder mergeFrom(ByteString data) throws InvalidProtocolBufferException;

		Builder mergeFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

		Builder mergeFrom(byte[] data) throws InvalidProtocolBufferException;

		Builder mergeFrom(byte[] data, int off, int len) throws InvalidProtocolBufferException;

		Builder mergeFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

		Builder mergeFrom(byte[] data, int off, int len, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException;

		Builder mergeFrom(InputStream input) throws IOException;

		Builder mergeFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException;

		boolean mergeDelimitedFrom(InputStream input) throws IOException;

		boolean mergeDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException;
	}
}
