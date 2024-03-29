/*
 * MIT License
 *
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
 *
 */

package com.palawan.gradle

import com.palawan.gradle.util.PlatformSpecific
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Path

/**
 * @author petr.langr
 * @since 1.0.0
 */
class GroovySupport {

	static GradleRunner withJacoco(GradleRunner self) {
		new File(self.projectDir, "gradle.properties")
				.append(self.class.classLoader.getResourceAsStream("testkit-gradle.properties"))
		return self
	}

	static GradleRunner withNode(GradleRunner self, Path nodeHome, PlatformSpecific platformSpecific) {
		String pathName = System.getenv().containsKey("Path") ? "Path" : "PATH"
		String nodePath = platformSpecific.getBinPath(nodeHome).toString()
		String path = System.getenv(pathName) + File.pathSeparator + nodePath
		return self.withEnvironment(Map.of(pathName, path))
	}

}
