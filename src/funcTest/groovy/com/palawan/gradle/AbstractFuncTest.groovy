/*
 * MIT License
 *
 * Copyright (c) 2020 Petr Langr
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
 *
 */

package com.palawan.gradle

import com.palawan.gradle.util.PlatformSpecific
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
/**
 * @author petr.langr
 * @since 1.0.0
 */
abstract class AbstractFuncTest extends Specification {


	static PlatformSpecific platformSpecific = PlatformSpecific.getInstance()
	Path testProjectDir
	File buildFile

	def setup() {
		testProjectDir = Files.createTempDirectory("junit")
		buildFile = Files.createFile(testProjectDir.resolve("build.gradle")).toFile()

		File settings = Files.createFile(testProjectDir.resolve("settings.gradle")).toFile()
		settings << """
			rootProject.name = 'sample-app'
		"""

	}

	def cleanup() throws Exception {
		if (testProjectDir != null) {
			testProjectDir.toFile().deleteDir()
		}
	}

	def run(String... args) {
		return runner(args).build()
	}

	def runAndFail(String... args) {
		return runner(args).buildAndFail()
	}

	protected GradleRunner prepareRunner(GradleRunner runner) {
		runner
	}

	private GradleRunner runner(String[] args) {
		args = args + ["--stacktrace"]
		return prepareRunner(GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(args)
				.withPluginClasspath()
				.withJacoco()
				.forwardOutput())
	}

	def buildScript(String nodeExtension) {
		buildFile.text = """
			plugins {
				id 'com.palawanframe.node'
			}
			
			group = 'com.palawanframe.sample'
			version = '0.0.1-SNAPSHOT'

			$nodeExtension
		"""
	}

	def writeFile(String name, String content) {
		File file = Files.createFile(testProjectDir.resolve(name)).toFile()
		file << content
	}

	static String tempFile(String path) {
		path.replace("/private/", "/")
	}

	def downloadString(String address) {
		//downloadString("https://nodejs.org/dist/")
		address.toURL().withInputStream { is ->
			new InputStreamReader(is).readLines().forEach{l -> println(l)}
		}
	}

}
