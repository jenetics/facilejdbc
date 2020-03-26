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

import static java.util.Objects.requireNonNull;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 */
final class Paper {
	private final String title;
	private final String isbn;
	private final Integer pages;

	public Paper(
		final String title,
		final String isbn,
		final Integer pages
	) {
		this.title = requireNonNull(title);
		this.isbn = isbn;
		this.pages = pages;
	}

	public String title() {
		return title;
	}

	public String isbn() {
		return isbn;
	}

	public Integer pages() {
		return pages;
	}
}
