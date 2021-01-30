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

package com.palawan.gradle.internal;

import org.gradle.api.Action;
import org.gradle.process.ExecSpec;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class ExecutableData implements Action<ExecSpec> {

	private boolean ignoreExitValue = false;
	private String executable;
	private List<String> args = List.of();
	private File workingDir;
	private Map<String, String> environmentVariables = Map.of();
	private List<String> path = List.of();

	private final Map<String, String> systemVariables;

	public ExecutableData() {
		this(System.getenv());
	}

	public ExecutableData(Map<String, String> systemVariables) {
		this.systemVariables = systemVariables;
	}

	/**
	 * Performs this action against the given object.
	 *
	 * @param execSpec The object to perform the action on.
	 */
	@Override
	public void execute(ExecSpec execSpec) {
		execSpec.setIgnoreExitValue(ignoreExitValue);
		execSpec.setExecutable(executable);
		execSpec.setArgs(args);
		execSpec.setWorkingDir(workingDir);
		execSpec.setEnvironment(computePath());
	}

	public ExecutableData setIgnoreExitValue(boolean ignoreExitValue) {
		this.ignoreExitValue = ignoreExitValue;
		return this;
	}

	public ExecutableData setExecutable(String executable) {
		this.executable = executable;
		return this;
	}

	public ExecutableData setArgs(List<String> args) {
		this.args = args;
		return this;
	}

	public ExecutableData setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
		return this;
	}

	public ExecutableData setDefaultWorkingDir(File workingDir) {
		if (this.workingDir == null) {
			this.workingDir = workingDir;
		}
		return this;
	}

	public ExecutableData addEnvironmentVariables(Map<String, String> environmentVariables) {
		if (!environmentVariables.isEmpty()) {
			if (this.environmentVariables.isEmpty()) {
				this.environmentVariables = new HashMap<>();
			}
			this.environmentVariables.putAll(environmentVariables);
		}
		return this;
	}

	public ExecutableData withPathLocation(String location) {
		if (path.isEmpty()) {
			path = new ArrayList<>(4);
		}
		path.add(location);
		return this;
	}

	private Map<String, ?> computePath() {
		Map<String, String> env = new HashMap<>();
		env.putAll(systemVariables);
		env.putAll(environmentVariables);

		if (!path.isEmpty()) {

			String pathName = env.containsKey("Path") ? "Path" : "PATH";
			final String pathValue;

			if (env.get(pathName) != null) {
				pathValue = Stream.concat(Arrays.stream(env.get(pathName).split(File.pathSeparator)), path.stream())
						.collect(Collectors.joining(File.pathSeparator));
			} else {
				pathValue = String.join(File.pathSeparator, path);
			}

			env.put(pathName, pathValue);

			return env;
		}
		return env;
	}

}
