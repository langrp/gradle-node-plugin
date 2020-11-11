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

package com.palawan.gradle.internal;

import com.palawan.gradle.NodePlugin;
import com.palawan.gradle.dsl.NodeExtension;
import com.palawan.gradle.internal.data.NodeData;
import com.palawan.gradle.tasks.NodeSetupTask;
import com.palawan.gradle.tasks.NodeTask;
import com.palawan.gradle.util.PlatformSpecific;
import com.palawan.gradle.util.ValueHolder;
import org.gradle.api.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class NodeManager {

	private final PlatformSpecific platformSpecific;
	private final ValueHolder<Path> versionWorkingDir = ValueHolder.racy(this::computeWorkingDir);

	private final NodeData data;
	private final PackagerInternal packager;
	private boolean onSystemPath = true;

	public NodeManager(PlatformSpecific platformSpecific, String command, String version, String url, Path workingDir) {
		this.platformSpecific = platformSpecific;
		this.data = new NodeData(command, version, workingDir, url);

		this.packager = PackagerInternal.npm(this);
	}

	/**
	 * Applies node plugin to given project.
	 * @param project Project to apply node to
	 */
	public void apply(Project project) {
		project.getExtensions().getExtraProperties().set(NodePlugin.NODE_TASK_TYPE, NodeTask.class);
		project.getTasks().withType(NodeTask.class, t -> {
			t.setGroup(NodePlugin.NODE_GROUP);
			t.setDescription(NodePlugin.NODE_TASK_DESC);
		});

		project.getTasks().register(NodePlugin.NODE_SETUP_TASK_NAME, NodeSetupTask.class, t -> {
			t.setGroup(NodePlugin.NODE_GROUP);
			t.setDescription(NodePlugin.NODE_SETUP_TASK_DESC);
		});

		packager.applyDefault(project);
	}

	/**
	 * Post evaluation process of node.
	 * @param project Project with applied node
	 * @param nodeExtension Final node configuration
	 */
	public void afterEvaluate(Project project, NodeExtension nodeExtension) {
		onSystemPath = !nodeExtension.getDownload();
		if (nodeExtension.getDownload()) {
			project.getTasks().withType(NodeTask.class, t -> t.dependsOn(NodePlugin.NODE_SETUP_TASK_NAME));
		}

		packager.afterEvaluateDefault(project, nodeExtension);
	}

	/**
	 * Provides executable data for node with specified arguments
	 * @param args Arguments to execute command with
	 * @return Executable data
	 */
	public ExecutableData executableData(List<String> args) {
		return new ExecutableData()
				.setExecutable(computeExecutable())
				.setArgs(args);
	}

	/**
	 * Get node data.
	 * @return Node data
	 */
	public NodeData getData() {
		return data;
	}

	/**
	 * Gets default node packager instance.
	 * @return	Default node packager
	 */
	public PackagerInternal getPackager() {
		return packager;
	}

	/**
	 * Gets node bin directory as if it would be downloaded. For cases
	 * where download is not required the return path will be not-existent.
	 * @return Node bin directory path
	 */
	public Path getBinDir() {
		return platformSpecific.getBinPath(versionWorkingDir.get());
	}

	/**
	 * Defines downloaded files to be executable. Available only on Unix base
	 * systems with posix file permissions.
	 * The method may throw {@link NodeException} in any failure
	 */
	public void setExecutablePosixRights() {
		if (platformSpecific.isWindows()) {
			throw new NodeException("Only posix file permissions can be defined");
		}
		Path exec = computeExecutablePath();

		try {
			if (Files.exists(exec)) {
				Set<PosixFilePermission> executable = PosixFilePermissions.fromString("rwxr--r--");
				Files.setPosixFilePermissions(exec, executable);
				packager.setExecutablePosixRights(executable);
			}

		} catch (IOException e) {
			throw new NodeException("Unable to define node executables", e);
		}
	}

	/**
	 * Get working directory with default node packager.
	 * @return Default packager working directory.
	 */
	Path getPackagerWorkingDir() {
		Path nodeModules = platformSpecific.isWindows() ?
				versionWorkingDir.get() : versionWorkingDir.get().resolve("lib");
		return nodeModules.resolve("node_modules/npm");
	}

	boolean getOnSystemPath() {
		return onSystemPath;
	}

	private Path computeWorkingDir() {
		String osName = platformSpecific.getOsName();
		String osArch = platformSpecific.getOsArch();
		return data.getWorkingDir().resolve("node-v" + data.getVersion() + "-" + osName + "-" + osArch);
	}

	private String computeExecutable() {
		if (onSystemPath) {
			return data.getCommand();
		}
		return computeExecutablePath().toAbsolutePath().toString();
	}

	private Path computeExecutablePath() {
		// default command needs to be adjusted to specific platform
		String exec = "node".equals(data.getCommand()) ?
				platformSpecific.getExecutable(data.getCommand()) : data.getCommand();
		return getBinDir().resolve(exec);
	}
}
