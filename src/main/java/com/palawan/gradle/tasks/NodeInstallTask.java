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
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectories;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.util.Collection;
import java.util.List;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class NodeInstallTask extends CommandExecutionTask {

	public NodeInstallTask() {
		setCommand("install");
	}

	@Override
	protected ExecutableData executableData(List<String> arguments) {
		return packager.get().executableData(arguments);
	}

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public Collection<String> getInputFiles() {
		return packager.get().getInputFiles();
	}

	@OutputFiles
	public Collection<String> getOutputFiles() {
		return packager.get().getOutputFiles();
	}

	@OutputDirectories
	public Collection<String> getOutputDirectories() {
		return packager.get().getOutputDirectories();
	}

}
