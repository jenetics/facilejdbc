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
 * @version 1.2
 */
pluginManagement {
	repositories {
		mavenLocal()
		gradlePluginPortal()
	}
}

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			alias("assertj").to("org.assertj:assertj-core:3.20.2")
			alias("commonsmath").to("org.apache.commons:commons-math3:3.6.1")
			alias("equalsverifier").to("nl.jqno.equalsverifier:equalsverifier:3.7.1")
			alias("hsqldb").to("org.hsqldb:hsqldb:2.6.1")
			alias("javafaker").to("com.github.javafaker:javafaker:1.0.2")
			alias("jpx").to("io.jenetics:jpx:2.3.0")
			alias("prngine").to("io.jenetics:prngine:2.0.0")
			alias("rxjava").to("io.reactivex.rxjava2:rxjava:2.1.14")
			alias("testng").to("org.testng:testng:7.4.0")
		}
	}
}

rootProject.name = "facilejdbc"

// The FacileJDBC projects.
include("facilejdbc")
