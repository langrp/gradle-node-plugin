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

package com.palawan.gradle.dsl;

import com.palawan.gradle.NodePlugin;
import com.palawan.gradle.internal.PackagerManagerInternal;
import com.palawan.gradle.internal.NodeManager;
import com.palawan.gradle.util.PlatformSpecific;
import org.gradle.api.Action;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;

/**
 * Noe plugin extension
 *
 * @author petr.langr
 * @since 1.0.0
 */
public class NodeExtension implements PackagerManager {

	public static NodeExtension get(Project project) {
		return project.getExtensions().getByType(NodeExtension.class);
	}

	private Boolean download = false;
	private final NodeManager nodeManager;
	private final PackagerManagerInternal packagerManager;
	private final PlatformSpecific platformSpecific;

	public NodeExtension(Project project) {
		this(project, PlatformSpecific.getInstance());
	}

	public NodeExtension(Project project, PlatformSpecific platformSpecific) {
		Path baseDir = project.file(".gradle").toPath();
		this.nodeManager = new NodeManager(
				platformSpecific,
				"node",
				NodePlugin.LTS_VERSION,
				"https://nodejs.org/dist",
				baseDir.resolve("nodejs"));
		this.packagerManager = new PackagerManagerInternal(project, baseDir);
		this.platformSpecific = platformSpecific;
	}

	public NodeManager getNodeManager() {
		return nodeManager;
	}

	public PackagerManagerInternal getPackagerManager() {
		return packagerManager;
	}

	/**
	 * Defines whether local instance of NodeJS should be downloaded
	 * @return {@code true} if local instance will be downloaded
	 */
	public Boolean getDownload() {
		return download;
	}

	/**
	 * Defines whether local instance of NodeJS should be downloaded
	 * @param download {@code true} if local instance will be downloaded
	 */
	public void setDownload(boolean download) {
		this.download = download;
	}

	/**
	 * Defines node execution command
	 * @return Node execution command
	 */
	public String getCommand() {
		return nodeManager.getData().getCommand();
	}

	/**
	 * Defines node execution command.
	 * @param command new node execution command
	 */
	public void setCommand(String command) {
		nodeManager.getData().setCommand(command);
	}

	/**
	 * Gets specific version of NodeJS required to download
	 * @return NodeJS version required to download
	 */
	public String getVersion() {
		return nodeManager.getData().getVersion();
	}

	/**
	 * Defines nodeJs version to be downloaded
	 * @see #setDownload(boolean)
	 * @param version NodeJS version string
	 */
	public void setVersion(String version) {
		nodeManager.getData().setVersion(version);
	}

	/**
	 * Get NodeJS base directory location
	 * @return NodeJS base directory as {@link Path}
	 */
	public Path getWorkingDir() {
		return nodeManager.getData().getWorkingDir();
	}

	/**
	 * Defines base NodeJS location. This makes sense only
	 * when download is required. The base NodeJS location is
	 * used to download specific version into. Multiple NodeJS
	 * versions may be stored here. Although only one can be used.
	 * @see #setDownload(boolean)
	 * @param workingDir NodeJS base download location
	 */
	public void setWorkingDir(File workingDir) {
		nodeManager.getData().setWorkingDir(workingDir.toPath());
	}

	/**
	 * Gets URL location to NodeJS repository
	 * @return URL to NodeJS repository
	 */
	public String getUrl() {
		return nodeManager.getData().getUrl();
	}

	/**
	 * Defines URL location to NodeJS repository
	 * @param url URL to nodeJS repository
	 */
	public void setUrl(String url) {
		nodeManager.getData().setUrl(url);
	}

	/**
	 * Gets utility to handle platform specifics. It's managed via
	 * extension to make it accessible over the plugin plus it still
	 * stays as single instance in project.
	 * @return platform specifics utility
	 */
	public PlatformSpecific getPlatformSpecific() {
		return platformSpecific;
	}

	@Override
	public void npm(Action<Packager> action) {
		packagerManager.npm(action);
	}

	@Override
	public void pnpm(Action<Packager> action) {
		packagerManager.pnpm(action);
	}

	@Override
	public void cnpm(Action<Packager> action) {
		packagerManager.cnpm(action);
	}

	@Override
	public void yarn(Action<Packager> action) {
		packagerManager.yarn(action);
	}

	@Override
	public void custom(Action<CustomPackager> action) {
		packagerManager.custom(action);
	}
}
