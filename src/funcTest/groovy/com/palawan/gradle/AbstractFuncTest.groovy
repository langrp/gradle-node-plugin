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
import org.gradle.internal.impldep.org.apache.tools.tar.TarInputStream
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream
/**
 * @author petr.langr
 * @since 1.0.0
 */
abstract class AbstractFuncTest extends Specification {

	static Path nodeJsDir
	static PlatformSpecific platformSpecific = PlatformSpecific.getInstance()
	Path testProjectDir
	File buildFile

	def setupSpec() {
		nodeJsDir = Files.createTempDirectory("junit-nodejs")
		nodeJsDir = downloadNode("v${NodePlugin.LTS_VERSION}")
		defineExecutable()

//		downloadString("https://nodejs.org/dist/")
	}

	def cleanupSpec() {
		if (nodeJsDir != null) {
			nodeJsDir.toFile().deleteDir()
		}
	}

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

	private GradleRunner runner(String[] args) {
		return GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(args)
				.withPluginClasspath()
				.withNode(nodeJsDir, platformSpecific)
				.withJacoco()
				.forwardOutput()
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

	def getAddress(String version) {
		def osName = platformSpecific.getOsName()
		def osArch = platformSpecific.getOsArch()
		def ext = platformSpecific.isWindows() ? "zip" : "tar.gz"
		return "https://nodejs.org/dist/$version/node-$version-$osName-$osArch.$ext"
	}

	def getInputStream(InputStream s, String address) {
		return address.endsWith("zip") ? new ZipInputStream( s ) : new TarInputStream( new GZIPInputStream(s) )
	}

	def downloadString(String address) {
		address.toURL().withInputStream { is ->
			new InputStreamReader(is).readLines().forEach{l -> println(l)}
		}
	}

	def downloadNode(String version) {
		def address = getAddress(version)
		address.toURL().withInputStream { s ->
			getInputStream(s, address).with { zs ->
				def entry
				while( entry = zs.nextEntry ) {
					def local = nodeJsDir.resolve( entry.name ).toFile()

					if( entry.isDirectory() ) {
						local.mkdir()
					} else {
						local << zs
					}

				}
				zs.close()
			}
		}

		Files.list(nodeJsDir).findFirst().orElseThrow{ new IllegalStateException("No nodejs") }
	}

	def defineExecutable() {
		if (!platformSpecific.isWindows()) {
			setExecutable(nodeJsDir.resolve("bin/node"), false)
			setExecutable(nodeJsDir.resolve("bin/npm"), true)
			setExecutable(nodeJsDir.resolve("bin/npx"), true)
		}
	}

	def setExecutable(Path path, boolean link) {
		if (link) {
			defineSymlink(path.getFileName().toString(), path)
					.toFile().setExecutable(true, true)
		} else {
			path.toFile().setExecutable(true, true)
		}
	}

	def defineSymlink(String name, Path link) {
		if (Files.deleteIfExists(link)) {
			Path parent = link.getParent()
			Path target = parent.getParent().resolve("lib/node_modules/npm/bin/${name}-cli.js")
			if (Files.exists(target)) {
				target.toFile().setExecutable(true, true)
				link = Files.createSymbolicLink(link, parent.relativize(target))
			}
		}
		return link
	}

}
