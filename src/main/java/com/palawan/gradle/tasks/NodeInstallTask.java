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

package com.palawan.gradle.tasks;

import com.palawan.gradle.internal.ExecutableData;
import com.palawan.gradle.internal.PackagerInternal;
import com.palawan.gradle.util.ValueHolder;
import org.gradle.api.tasks.*;

import java.util.Collection;
import java.util.List;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class NodeInstallTask extends CommandExecutionTask {

	private final ValueHolder<PackagerInternal> packager = ValueHolder.racy(this::getPackager);

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
	@PathSensitive(PathSensitivity.RELATIVE)
	public Collection<String> getOutputFiles() {
		return packager.get().getOutputFiles();
	}

	@OutputDirectories
	@PathSensitive(PathSensitivity.RELATIVE)
	public Collection<String> getOutputDirectories() {
		return packager.get().getOutputDirectories();
	}

	private PackagerInternal getPackager() {
		return getNodeExtension()
				.getPackagerManager()
				.getPackager()
				.orElseGet(() -> getNodeExtension().getNodeManager().getPackager());
	}

}
