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

package com.palawan.gradle.tasks

import com.palawan.gradle.AbstractProjectTest
import com.palawan.gradle.internal.ExecutableData
import org.gradle.process.ExecSpec

/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
class CommandExecutionTaskTest extends AbstractProjectTest {

	def "Allows adding argument"() {
		given:
		def task = project.tasks.create("packager", TestTask)

		when:
		task.addArgument("library")

		then:
		task.arguments == ["library"]
	}

	def "Allows adding arguments"() {
		given:
		def task = project.tasks.create("packager", TestTask)

		when:
		task.addArguments(["generate", "component"])

		then:
		task.arguments == ["generate", "component"]
	}

	def "Filters empty arguments"() {
		given:
		ExecSpec spec = Mock()
		def task = project.tasks.create("packager", TestTask)
		nodeExtension.getNodeManager().getPackager().afterEvaluate(project, nodeExtension)

		and:
		task.setCommand("")
		task.addArguments(["generate", null, "", "component"])

		when:
		task.getExecutable().execute(spec)

		then:
		with(spec) {
			1 * setArgs(["generate", "component"])
		}
	}

	static class TestTask extends CommandExecutionTask {

		@Override
		protected ExecutableData executableData(List<String> arguments) {
			return new ExecutableData().setArgs(arguments)
		}
	}
}
