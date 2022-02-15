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

import com.palawan.gradle.dsl.NodeExtension;
import com.palawan.gradle.internal.ExecutableData;
import com.palawan.gradle.util.ValueHolder;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public abstract class ExecutionTask extends DefaultTask {

	private final ValueHolder<NodeExtension> nodeExtension = ValueHolder.racy(() -> NodeExtension.get(getProject()));

	private boolean ignoreExitValue = false;

	private Map<String, String> environment = Map.of();

	@Nullable
	private File workingDir;

	@TaskAction
	public void execute() {
		ExecutableData executable = getExecutable()
				.setWorkingDir(getWorkingDirOrProjectDir())
				.setIgnoreExitValue(ignoreExitValue)
				.addEnvironmentVariables(environment);

		execute(executable);
	}

	protected ExecResult execute(ExecutableData executable) {
		if (getNodeExtension().getDownload()) {
			executable.withPathLocation(getNodeExtension().getNodeManager().getBinDir().toAbsolutePath().toString());
		}

		return getProject().exec(executable);
	}

	@Internal
	protected NodeExtension getNodeExtension() {
		return nodeExtension.get();
	}

	@Internal
	protected abstract ExecutableData getExecutable();

	/**
	 * Get value of ignoreExitValue of task execution
	 *
	 * @return ignoreExitValue
	 */
	@Input
	public boolean getIgnoreExitValue() {
		return ignoreExitValue;
	}

	/**
	 * Set value for task execution ignoreExitValue
	 *
	 * @param ignoreExitValue Set value of ignoreExitValue
	 */
	public void setIgnoreExitValue(boolean ignoreExitValue) {
		this.ignoreExitValue = ignoreExitValue;
	}

	/**
	 * Get additional environment environment variables
	 *
	 * @return environment
	 */
	@Input
	public Map<String, String> getEnvironment() {
		return environment;
	}

	/**
	 * Defines additional environment environment
	 *
	 * @param environment Set value of environment
	 */
	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}

	/**
	 * Get value of workingDir
	 *
	 * @return workingDir
	 */
	@Internal
	@Nullable
	public File getWorkingDir() {
		return workingDir;
	}

	/**
	 * Set value for property workingDir
	 *
	 * @param workingDir Set value of workingDir
	 */
	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * Get path of working dir as input gradle task parameter
	 * @return Working dir path
	 */
	@Input
	@Optional
	@Nullable
	public String getWorkingDirString() {
		return workingDir == null ? null : workingDir.getAbsolutePath();
	}

	/**
	 * Gets either configured workingDir or project dir file.
	 * @return Working dir
	 */
	@Internal
	protected File getWorkingDirOrProjectDir() {
		return workingDir == null ? getProject().getProjectDir() : workingDir;
	}
}
