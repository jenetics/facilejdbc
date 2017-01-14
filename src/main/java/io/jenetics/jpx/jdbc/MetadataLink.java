/*
 * Java GPX Library (@__identifier__@).
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
package io.jenetics.jpx.jdbc;

import static java.lang.String.format;

import io.jenetics.jpx.jdbc.internal.util.Pair;

/**
 * Represents a row in the "metadata_link" table.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public final class MetadataLink {
	private final long _metadataID;
	private final long _linkID;

	private MetadataLink(final long metadataID, final long linkID) {
		_metadataID = metadataID;
		_linkID = linkID;
	}

	long getMetadataID() {
		return _metadataID;
	}

	long getLinkID() {
		return _linkID;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash += 37*Long.hashCode(_metadataID) + 31;
		hash += 37*Long.hashCode(_linkID) + 31;

		return hash;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof MetadataLink &&
			Long.compare(((MetadataLink)obj)._metadataID, _metadataID) == 0 &&
			Long.compare(((MetadataLink)obj)._linkID, _linkID) == 0;
	}

	@Override
	public String toString() {
		return format("MetadataLink[%d, %d]", _metadataID, _linkID);
	}

	public static MetadataLink of(final long metadataID, final long linkID) {
		return new MetadataLink(metadataID, linkID);
	}

	public static MetadataLink of(final Pair<Long, Long> pair) {
		return new MetadataLink(pair._1, pair._2);
	}
}
