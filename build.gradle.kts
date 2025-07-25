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

plugins {
	alias(libs.plugins.jmh)
	alias(libs.plugins.version.catalog.update)
}

rootProject.version = FacileJDBC.VERSION

tasks.named<Wrapper>("wrapper") {
	version = "8.14.3"
	distributionType = Wrapper.DistributionType.ALL
}

/**
 * Project configuration *before* the projects has been evaluated.
 */
allprojects {
	group =  FacileJDBC.GROUP
	version = FacileJDBC.VERSION

	repositories {
		flatDir {
			dirs("${rootDir}/buildSrc/lib")
		}
		mavenLocal()
		mavenCentral()
	}

	configurations.all {
		resolutionStrategy.failOnVersionConflict()
	}
}

/**
 * Project configuration *after* the projects has been evaluated.
 */
gradle.projectsEvaluated {
	subprojects {
		val project = this

		tasks.withType<JavaCompile> {
			options.compilerArgs.add("-Xlint:" + xlint())
		}

		plugins.withType<JavaPlugin> {
			configure<JavaPluginExtension> {
				sourceCompatibility = JavaVersion.VERSION_17
				targetCompatibility = JavaVersion.VERSION_17
			}

			configure<JavaPluginExtension> {
				modularity.inferModulePath.set(true)
			}

			setupJava(project)
			setupTestReporting(project)
			setupJavadoc(project)
		}

		if (plugins.hasPlugin("maven-publish")) {
			setupPublishing(project)
		}
	}

}

/**
 * Some common Java setup.
 */
fun setupJava(project: Project) {
	val attr = mutableMapOf(
		"Implementation-Title" to project.name,
		"Implementation-Version" to FacileJDBC.VERSION,
		"Implementation-URL" to FacileJDBC.URL,
		"Implementation-Vendor" to FacileJDBC.NAME,
		"ProjectName" to FacileJDBC.NAME,
		"Version" to FacileJDBC.VERSION,
		"Maintainer" to FacileJDBC.AUTHOR,
		"Project" to project.name,
		"Project-Version" to project.version,

		"Created-With" to "Gradle ${gradle.gradleVersion}",
		"Built-By" to Env.BUILD_BY,
		"Build-Date" to Env.BUILD_DATE,
		"Build-JDK" to Env.BUILD_JDK,
		"Build-OS-Name" to Env.BUILD_OS_NAME,
		"Build-OS-Arch" to Env.BUILD_OS_ARCH,
		"Build-OS-Version" to Env.BUILD_OS_VERSION
	)
	if (project.extra.has("moduleName")) {
		attr["Automatic-Module-Name"] = project.extra["moduleName"].toString()
	}

	project.tasks.withType<Jar> {
		manifest {
			attributes(attr)
		}
	}
}

/**
 * Setup of the Java test-environment and reporting.
 */
fun setupTestReporting(project: Project) {
	project.apply(plugin = "jacoco")

	project.configure<JacocoPluginExtension> {
		toolVersion = "0.8.11"
	}

	project.tasks {
		named<JacocoReport>("jacocoTestReport") {
			dependsOn("test")

			reports {
				html.required.set(true)
				xml.required.set(true)
				csv.required.set(true)
			}
		}

		named<Test>("test") {
			useTestNG()
			finalizedBy("jacocoTestReport")
		}
	}
}

/**
 * Setup of the projects Javadoc.
 */
fun setupJavadoc(project: Project) {
	project.tasks.withType<Javadoc> {
		val doclet = options as StandardJavadocDocletOptions
		doclet.addBooleanOption("Xdoclint:accessibility,html,reference,syntax", true)

		exclude("**/internal/**")

		doclet.memberLevel = JavadocMemberLevel.PROTECTED
		doclet.version(true)
		doclet.docEncoding = "UTF-8"
		doclet.charSet = "UTF-8"
		doclet.linkSource(true)
		doclet.linksOffline(
			"https://docs.oracle.com/en/java/javase/17/docs/api/",
			"${project.rootDir}/buildSrc/resources/javadoc/java.se"
		)
		doclet.windowTitle = "FacileJDBC ${project.version}"
		doclet.docTitle = "<h1>FacileJDBC ${project.version}</h1>"
		doclet.bottom = "&copy; ${Env.COPYRIGHT_YEAR} Franz Wilhelmst&ouml;tter  &nbsp;<i>(${Env.BUILD_DATE})</i>"
		doclet.stylesheetFile = project.file("${project.rootDir}/buildSrc/resources/javadoc/stylesheet.css")

		doclet.tags = listOf(
			"apiNote:a:API Note:",
			"implSpec:a:Implementation Requirements:",
			"implNote:a:Implementation Note:"
		)

		doLast {
			project.copy {
				from("src/main/java") {
					include("io/**/doc-files/*.*")
				}
				includeEmptyDirs = false
				into(destinationDir!!)
			}
		}
	}

	val javadoc = project.tasks.findByName("javadoc") as Javadoc?
	if (javadoc != null) {
		project.tasks.register<io.jenetics.gradle.ColorizerTask>("colorizer") {
			directory = javadoc.destinationDir!!
		}

		javadoc.doLast {
			val colorizer = project.tasks.findByName("colorizer")
			colorizer?.actions?.forEach {
				it.execute(colorizer)
			}
		}
	}
}

/**
 * The Java compiler XLint flags.
 */
fun xlint(): String {
	// See https://docs.oracle.com/javase/9/tools/javac.htm#JSWOR627
	return listOf(
		"auxiliaryclass",
		"cast",
		"classfile",
		"dep-ann",
		"deprecation",
		"divzero",
		"empty",
		"exports",
		"finally",
		"module",
		"opens",
		"overrides",
		"rawtypes",
		"removal",
		"serial",
		"static",
		"try",
		"unchecked"
	).joinToString(separator = ",")
}

val identifier = "${FacileJDBC.ID}-${FacileJDBC.VERSION}"

/**
 * Setup of the Maven publishing.
 */
fun setupPublishing(project: Project) {
	project.configure<JavaPluginExtension> {
		withJavadocJar()
		withSourcesJar()
	}

	project.tasks.named<Jar>("sourcesJar") {
		filter(
			org.apache.tools.ant.filters.ReplaceTokens::class, "tokens" to mapOf(
			"__identifier__" to identifier,
			"__year__" to Env.COPYRIGHT_YEAR
		)
		)
	}

	project.tasks.named<Jar>("javadocJar") {
		filter(
			org.apache.tools.ant.filters.ReplaceTokens::class, "tokens" to mapOf(
			"__identifier__" to identifier,
			"__year__" to Env.COPYRIGHT_YEAR
		)
		)
	}

	project.configure<PublishingExtension> {
		publications {
			create<MavenPublication>("mavenJava") {
				artifactId = FacileJDBC.ID
				from(project.components["java"])
				versionMapping {
					usage("java-api") {
						fromResolutionOf("runtimeClasspath")
					}
					usage("java-runtime") {
						fromResolutionResult()
					}
				}
				pom {
					name.set(FacileJDBC.ID)
					description.set(project.description)
					url.set(FacileJDBC.URL)
					inceptionYear.set("2019")

					licenses {
						license {
							name.set("The Apache License, Version 2.0")
							url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
							distribution.set("repo")
						}
					}
					developers {
						developer {
							id.set(FacileJDBC.ID)
							name.set(FacileJDBC.AUTHOR)
							email.set(FacileJDBC.EMAIL)
						}
					}
					scm {
						connection.set(Maven.SCM_CONNECTION)
						developerConnection.set(Maven.DEVELOPER_CONNECTION)
						url.set(Maven.SCM_URL)
					}
				}
			}
		}
		repositories {
			maven {
				url = if (version.toString().endsWith("SNAPSHOT"))
					uri(layout.buildDirectory.dir("repos/snapshots"))
				else
					uri(layout.buildDirectory.dir("repos/releases"))
			}
		}
	}

	project.apply(plugin = "signing")

	project.configure<SigningExtension> {
		sign(project.the<PublishingExtension>().publications["mavenJava"])
	}

}

