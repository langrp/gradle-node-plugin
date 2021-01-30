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

package com.palawan.gradle.internal.data;

import java.nio.file.Path;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class NodeData {

	private String command;
	private String version;
	private Path workingDir;
	private String url;

	public NodeData(String command, String version, Path workingDir, String url) {
		this.command = command;
		this.version = version;
		this.workingDir = workingDir;
		this.url = url;
	}

	/**
	 * Get value of command
	 *
	 * @return command
	 */
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
	 * Get value of version
	 *
	 * @return version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Set value for property version
	 *
	 * @param version Set value of version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Get value of workingDir
	 *
	 * @return workingDir
	 */
	public Path getWorkingDir() {
		return workingDir;
	}

	/**
	 * Set value for property workingDir
	 *
	 * @param workingDir Set value of workingDir
	 */
	public void setWorkingDir(Path workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * Get value of url
	 *
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set value for property url
	 *
	 * @param url Set value of url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}
