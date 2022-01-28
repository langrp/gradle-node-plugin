/*
 * Copyright (c) 2022 Petr Langr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	id("java")
	id("groovy")
	id("java-gradle-plugin")
	id("idea")
	id("com.gradle.plugin-publish") version "0.20.0"
	id("pl.droidsonroids.jacoco.testkit") version "1.0.7"
	id("signing")
}

apply( from = "gradle/publishing.gradle.kts" )
apply( from = "gradle/functional-tests.gradle.kts" )

repositories {
    mavenCentral()
	gradlePluginPortal()
}


group = "com.palawanframe.build"
description = "Gradle plugin enabling build of node application."

java {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
	plugins {
		register("node") {
			id = "com.palawanframe.node"
			displayName = "Node Plugin"
			description = """
                Build your node frontend application along with your gradle base backend application. Plugin manages
                various node packagers and their execution. Enables compilation of node base project without need
				of installing NodeJS - all done via local download.
            """
			implementationClass = "com.palawan.gradle.NodePlugin"
		}
	}
}

pluginBundle {
	website = "https://github.com/langrp/${rootProject.name}"
	vcsUrl = "https://github.com/langrp/${rootProject.name}"
	description = project.description
	tags = listOf("node", "nodejs", "npm", "pnpm", "cnpm", "yarn")

//    mavenCoordinates {
//        groupId = project.group as String
//        artifactId = project.name
//        version = project.version as String
//    }
}

dependencies {
	implementation(gradleApi())
	implementation(localGroovy())

	testImplementation(gradleTestKit())
	testImplementation(platform("org.spockframework:spock-bom:2.0-groovy-3.0"))
	testImplementation("org.spockframework:spock-core")
}

tasks.withType<JacocoCoverageVerification> {
	classDirectories.setFrom(
		sourceSets["main"].output.asFileTree.matching {
			exclude("com/palawan/gradle/util/DoubleChecked.class")
		}
	)
}

tasks.withType<Test> {
	maxParallelForks = 4
	useJUnitPlatform()
	testLogging {
		events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
		showCauses = true
		showStandardStreams = false
	}
}
