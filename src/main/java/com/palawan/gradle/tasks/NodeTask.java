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

package com.palawan.gradle.tasks;

import com.palawan.gradle.internal.ExecutableData;
import com.palawan.gradle.internal.NodeManager;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.options.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class NodeTask extends ExecutionTask {

	private String script;
	private List<String> options = new ArrayList<>(0);
	private List<String> arguments = new ArrayList<>(0);

	@Override
	protected ExecutableData getExecutable() {
		List<String> args = Stream.concat(Stream.concat(options.stream(), Stream.of(script)), arguments.stream())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		NodeManager nodeManager = getNodeExtension().getNodeManager();
		return nodeManager.executableData(args);
	}

	@Option(
			option = "script",
			description = "Define node script."
	)
	public void setScript(String script) {
		this.script = script;
	}

	@Input
	@Optional
	public String getScript() {
		return script;
	}

	@Option(
			option = "args",
			description = "Define node script arguments."
	)
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	@Input
	public List<String> getArguments() {
		return arguments;
	}

	@Option(
			option = "opts",
			description = "Define node options."
	)
	public void setOptions(List<String> options) {
		this.options = options;
	}

	@Input
	public List<String> getOptions() {
		return options;
	}
}
