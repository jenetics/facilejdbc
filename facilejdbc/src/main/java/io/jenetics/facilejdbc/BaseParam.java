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

/**
 * This is the, effectively sealed, base interface of the {@link Param} and
 * {@link MultiParam} class. Since it is currently not possible to enforce this
 * behavior (until the <em>sealed classes</em> feature is released) an exception
 * is thrown at runtime, when an other implementation than {@link Param} or
 * {@link MultiParam} is detected in the {@link Query#on(BaseParam...)} method.
 *
 * @see Param
 * @see MultiParam
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.3
 * @since 1.3
 */
public /*sealed*/ interface BaseParam /*permits Param, MultiParam*/ {

	/**
	 * Return the parameter name.
	 *
	 * @return the parameter name
	 */
	String name();

}
