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


/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version 1.0
 */

import java.time.Year
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

ext {
	javaVersion = '11'

	now = ZonedDateTime.now()
	year = Year.now()
	copyrightYear = "2019-${year}".toString()
	identifier = "${rootProject.name}-${version}".toString()
	dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
}

allprojects {
	group = 'io.jenetics'
	version = '1.1.0'

	repositories {
		mavenCentral()
	}
}

allprojects { Project prj ->
	if (prj.plugins.hasPlugin('java')) {
		sourceCompatibility = javaVersion
		targetCompatibility = javaVersion
	}

	def XLINT_OPTIONS = [
		'cast',
		'classfile',
		'deprecation',
		'dep-ann',
		'divzero',
		'finally',
		'overrides',
		'rawtypes',
		'serial',
		'try',
		'unchecked'
	]

	prj.tasks.withType(JavaCompile) { JavaCompile compile ->
		compile.options.compilerArgs = ["-Xlint:${XLINT_OPTIONS.join(',')}"]
	}
}

task upgradeGradle(type: Wrapper) {
	gradleVersion = '6.3'
}

