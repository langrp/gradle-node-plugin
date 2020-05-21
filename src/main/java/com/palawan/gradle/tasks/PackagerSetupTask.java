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
import com.palawan.gradle.internal.NodeException;
import com.palawan.gradle.internal.NodeManager;
import com.palawan.gradle.internal.PackagerInternal;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class PackagerSetupTask extends ExecutionTask {

	private List<String> args = List.of();

	@Override
	protected ExecutableData getExecutable() {
		NodeManager nodeManager = getNodeExtension().getNodeManager();
		PackagerInternal defaultPackager = nodeManager.getPackager();
		PackagerInternal packager = getNodeExtension().getPackagerManager().getPackager()
				.orElseThrow(() -> new NodeException("Unable to setup packager"));

		String npmPackage = packager.getNpmPackage() + "@" + packager.getVersion();
		List<String> arguments = new ArrayList<>(List.of(
				"install", "--global", "--no-save", "--prefix", packager.getWorkingDir().toAbsolutePath().toString(),
				npmPackage
		));
		arguments.addAll(args);

		return defaultPackager.executableData(arguments)
				.withPathLocation(nodeManager.getBinDir().toAbsolutePath().toString());
	}

	@Nullable
	@OutputDirectory
	public File getWorkingDir() {
		return getNodeExtension()
				.getPackagerManager()
				.getPackager()
				.map(PackagerInternal::getWorkingDir)
				.map(Path::toFile)
				.orElse(null);
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
}
