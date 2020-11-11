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

import com.palawan.gradle.util.PlatformSpecific;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * @author petr.langr
 * @since 1.0.0
 */
abstract class AbstractExecutable {

	/** Platform specific */
	private PlatformSpecific platformSpecific;

	/** Packager name and default command */
	protected final String name;
	/** NodeManager available to default NodeJs packager only */
	@Nullable protected final NodeManager nodeManager;
	/** Parent of this packager, e.g. packager cli command requires parent */
	@Nullable private final AbstractExecutable parent;
	/** Indicates the command is available on system path */
	private boolean onSystemPath = true;

	public AbstractExecutable(String name) {
		this.name = name;
		this.nodeManager = null;
		this.parent = null;
	}

	public AbstractExecutable(String name, NodeManager nodeManager) {
		this.name = name;
		this.nodeManager = Objects.requireNonNull(nodeManager, "NodeManager required");
		this.parent = null;
	}

	public AbstractExecutable(String name, AbstractExecutable parent) {
		this.name = name;
		this.parent = parent;
		this.nodeManager = parent.nodeManager;
		this.platformSpecific = parent.platformSpecific;
	}

	/**
	 * Provides executable data with the specific command to be executed
	 * including arguments as defined in parameter.
	 * @param args Arguments to execute command with
	 * @return Executable data
	 */
	public ExecutableData executableData(List<String> args) {
		if (nodeManager == null || getOnSystemPath()) {
			return new ExecutableData()
					.setExecutable(computeExecutable())
					.setArgs(args);
		}
		List<String> arguments = new ArrayList<>();
		arguments.add(getScriptFile()
				.map(Path::toAbsolutePath)
				.map(Path::toString)
				.orElseThrow(() -> new NodeException("Packager is not default node packager")));
		arguments.addAll(args);
		return nodeManager.executableData(arguments);
	}

	/**
	 * Defines command available at system PATH. The command from system path
	 * will be used whenever onSystemPath is equal {@code true}.
	 * If this instance uses parent, then parent onSystemPath property is used.
	 * @param onSystemPath Indicates whether command is available at system path
	 */
	public void setOnSystemPath(boolean onSystemPath) {
		this.onSystemPath = onSystemPath;
	}

	/**
	 * Provides a command to be executed. It's up to this abstract class
	 * to decide whether system command is being used or the other
	 * @return Executable command
	 */
	protected abstract String getCommand();

	/**
	 * Provides executable bin directory where the {@link #getCommand()} will be
	 * triggered from. This method is called only in case the command is not
	 * available on system path.
	 * Default implementation uses parent to fetch bin directory. If no parent
	 * is defined an exception is thrown.
	 * @return	Path to executable bin directory.
	 */
	protected Path getExecutableBinDir() {
		Objects.requireNonNull(parent, "Unable to get executable bin directory.");
		return parent.getExecutableBinDir();
	}

	/**
	 * Get script file path for node executable script of this executable.
	 * The path will be converted to absolute path.
	 * @return Optional node executable script path.
	 */
	protected abstract Optional<Path> getScriptFile();

	/**
	 * Defines platform specific utility
	 * @param platformSpecific Platform specific utility
	 */
	protected void setPlatformSpecific(PlatformSpecific platformSpecific) {
		this.platformSpecific = platformSpecific;
	}

	protected PlatformSpecific getPlatformSpecific() {
		return platformSpecific;
	}

	private boolean getOnSystemPath() {
		return parent == null && nodeManager == null ? onSystemPath :
				parent != null ? parent.getOnSystemPath() : nodeManager.getOnSystemPath();
	}

	private String computeExecutable() {
		String command = getCommand();
		return getOnSystemPath() ? command :
				getExecutableBinDir().resolve(
						name.equals(command) ? platformSpecific.getCommand(command) : command
				).toAbsolutePath().toString();
	}

	/**
	 * Defines downloaded files to be executable. Available only on Unix base
	 * systems with posix file permissions.
	 * The method also takes care of proper link to script files from node manager
	 * bin directory
	 * @throws IOException File permission change error
	 */
	void setExecutablePosixRights(Set<PosixFilePermission> permissions) throws IOException {
		Objects.requireNonNull(nodeManager, "Operation requires NodeManager");
		Path link = nodeManager.getBinDir().resolve(getCommand());
		Path target = getScriptFile().orElseThrow(() -> new NodeException("Operation requires node packager script"));

		if (Files.deleteIfExists(link)) {
			Files.setPosixFilePermissions(target, permissions);
			Files.createSymbolicLink(link, link.getParent().relativize(target));
			Files.setPosixFilePermissions(link, permissions);
		}
	}

}
