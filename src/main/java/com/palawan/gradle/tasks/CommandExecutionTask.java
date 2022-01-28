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
import com.palawan.gradle.internal.PackagerInternal;
import com.palawan.gradle.util.ValueHolder;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author petr.langr
 * @since 1.0.0
 */
abstract class CommandExecutionTask extends ExecutionTask {

	/** Holds specific packager if any configured or default one */
	protected final ValueHolder<PackagerInternal> packager = ValueHolder.racy(this::getPackager);

	private String command;

	private List<String> arguments = List.of();

	@Override
	protected ExecutableData getExecutable() {
		List<String> args = Stream.concat(Stream.of(command), arguments.stream())
				.filter(Objects::nonNull)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
		return executableData(args);
	}

	protected abstract ExecutableData executableData(List<String> arguments);

	/**
	 * Get value of command
	 *
	 * @return command
	 */
	@Input
	@Optional
	public String getCommand() {
		return command;
	}

	/**
	 * Set value for property command
	 *
	 * @param command Set value of command
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * Get value of args
	 *
	 * @return args
	 */
	@Input
	public List<String> getArguments() {
		return arguments;
	}

	/**
	 * Set value for property args
	 *
	 * @param arguments Set value of args
	 */
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	private PackagerInternal getPackager() {
		return getNodeExtension()
				.getPackagerManager()
				.getPackager()
				.orElseGet(() -> getNodeExtension().getNodeManager().getPackager());
	}
}
