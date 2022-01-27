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

import java.util.ArrayList;
import java.util.List;

/**
 * @author petr.langr
 * @since 1.0.0
 */
abstract class CommandExecutionTask extends ExecutionTask {

	/** Holds specific packager if any configured or default one */
	protected final ValueHolder<PackagerInternal> packager = ValueHolder.racy(this::getPackager);

	private String command;

	private List<String> args = List.of();

	@Override
	protected ExecutableData getExecutable() {
		List<String> arguments = new ArrayList<>(args.size() + 1);
		arguments.add(command);
		arguments.addAll(args);
		return executableData(arguments);
	}

	protected abstract ExecutableData executableData(List<String> arguments);

	/**
	 * Get value of command
	 *
	 * @return command
	 */
	@Input
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
	public List<String> getArgs() {
		return args;
	}

	/**
	 * Set value for property args
	 *
	 * @param args Set value of args
	 */
	public void setArgs(List<String> args) {
		this.args = args;
	}

	private PackagerInternal getPackager() {
		return getNodeExtension()
				.getPackagerManager()
				.getPackager()
				.orElseGet(() -> getNodeExtension().getNodeManager().getPackager());
	}
}
