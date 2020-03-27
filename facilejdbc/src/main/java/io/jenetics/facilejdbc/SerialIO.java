/*
 * Facile JDBC Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.facilejdbc;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.1
 * @since 1.1
 */
final class SerialIO {
	private SerialIO() {
	}

	/**
	 * Write the given string {@code value} to the given data output.
	 *
	 * @param value the string value to write
	 * @param out the data output
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeString(final String value, final DataOutput out)
		throws IOException
	{
		final byte[] bytes = value.getBytes(UTF_8);
		writeInt(bytes.length, out);
		out.write(bytes);
	}

	/**
	 * Reads a string value from the given data input.
	 *
	 * @param in the data input
	 * @return the read string value
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 * @throws IOException if an I/O error occurs
	 */
	public static String readString(final DataInput in) throws IOException {
		final byte[] bytes = new byte[readInt(in)];
		in.readFully(bytes);
		return new String(bytes, UTF_8);
	}

	/**
	 * Writes an int value to a series of bytes. The values are written using
	 * <a href="http://lucene.apache.org/core/3_5_0/fileformats.html#VInt">variable-length</a>
	 * <a href="https://developers.google.com/protocol-buffers/docs/encoding?csw=1#types">zig-zag</a>
	 * coding. Each {@code int} value is written in 1 to 5 bytes.
	 *
	 * @see #readInt(DataInput)
	 *
	 * @param value the integer value to write
	 * @param out the data output the integer value is written to
	 * @throws NullPointerException if the given data output is {@code null}
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeInt(final int value, final DataOutput out)
		throws IOException
	{
		// Zig-zag encoding.
		int n = (value << 1)^(value >> 31);
		if ((n & ~0x7F) != 0) {
			out.write((byte)((n | 0x80) & 0xFF));
			n >>>= 7;
			if (n > 0x7F) {
				out.write((byte)((n | 0x80) & 0xFF));
				n >>>= 7;
				if (n > 0x7F) {
					out.write((byte)((n | 0x80) & 0xFF));
					n >>>= 7;
					if (n > 0x7F) {
						out.write((byte)((n | 0x80) & 0xFF));
						n >>>= 7;
					}
				}
			}
		}
		out.write((byte)n);
	}

	/**
	 * Reads an int value from the given data input. The integer value must have
	 * been written by the {@link #writeInt(int, DataOutput)} before.
	 *
	 * @see #writeInt(int, DataOutput)
	 *
	 * @param in the data input where the integer value is read from
	 * @return the read integer value
	 * @throws NullPointerException if the given data input is {@code null}
	 * @throws IOException if an I/O error occurs
	 */
	public static int readInt(final DataInput in) throws IOException {
		int b = in.readByte() & 0xFF;
		int n = b & 0x7F;

		if (b > 0x7F) {
			b = in.readByte() & 0xFF;
			n ^= (b & 0x7F) << 7;
			if (b > 0x7F) {
				b = in.readByte() & 0xFF;
				n ^= (b & 0x7F) << 14;
				if (b > 0x7F) {
					b = in.readByte() & 0xFF;
					n ^= (b & 0x7F) << 21;
					if (b > 0x7F) {
						b = in.readByte() & 0xFF;
						n ^= (b & 0x7F) << 28;
						if (b > 0x7F) {
							throw new IOException("Invalid int encoding.");
						}
					}
				}
			}
		}

		return (n >>> 1)^-(n & 1);
	}

}
