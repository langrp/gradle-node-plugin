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
import com.palawan.gradle.internal.data.PackagerData;
import com.palawan.gradle.tasks.*;
import com.palawan.gradle.util.ValueHolder;
import org.gradle.api.Project;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class PackagerInternal extends AbstractExecutable {

	public static PackagerInternal npm() {
		return new PackagerInternal("npm", npmData());
	}

	public static PackagerInternal npm(NodeManager nodeManager) {
		Objects.requireNonNull(nodeManager, "Missing NodeManager");
		return new PackagerInternal("npm", npmData(), nodeManager);
	}

	private static PackagerData npmData() {
		return new PackagerData()
				.setCommand("npm")
				.setNpmPackage("npm")
				.setLocalScript("bin/npm-cli.js")
				.addInputFile("package.json")
				.addInputFile("package-lock.json")
				.addOutputDirectory("node_modules")
				.addOutputFile("package-lock.json")
				.cli(c ->
					c.setCommand("npx")
					.setLocalScript("bin/npx-cli.js")
				);
	}

	public static PackagerInternal pnpm() {
		return new PackagerInternal("pnpm", new PackagerData()
				.setCommand("pnpm")
				.setNpmPackage("pnpm")
				.setLocalScript("bin/pnpm.js")
				.addInputFile("package.json")
				.addOutputDirectory("node_modules")
				.addOutputFile("pnpm-lock.yaml")
				.cli(c ->
						c.setCommand("pnpx")
						.setLocalScript("bin/pnpx.js")
				));
	}

	public static PackagerInternal cnpm() {
		return new PackagerInternal("cnpm", new PackagerData()
				.setCommand("cnpm")
				.setNpmPackage("cnpm")
				.addInputFile("package.json")
				.addOutputDirectory("node_modules"));
	}

	public static PackagerInternal yarn() {
		return new PackagerInternal("yarn", new PackagerData()
				.setCommand("yarn")
				.setNpmPackage("yarn")
				.addInputFile("package.json")
				.addInputFile("yarn.lock")
				.addOutputFile("yarn.lock")
				.addOutputDirectory("node_modules"));
	}

	public static PackagerInternal custom() {
		return new PackagerInternal("custom", new PackagerData()
				.setCommand("custom")
				.setNpmPackage("custom"));
	}



	/** Packager data contains command, working directory etc. */
	private final PackagerData data;
	/** Computed version specific working directory lazy holder */
	private final ValueHolder<Path> workingDir = ValueHolder.racy(this::computeWorkingDir);
	/** Computed setup task name for the packager, lazy holder */
	private final ValueHolder<String> setupTaskName = ValueHolder.racy(this::computeSetupTaskName);
	/** Packager cli executor - must be lazy loaded to properly wait for configuration */
	private final ValueHolder<PackagerCliInternal> cli = ValueHolder.racy(this::createCli);


	private PackagerInternal(String name, PackagerData data) {
		super(name);
		this.data = data;
	}

	private PackagerInternal(String name, PackagerData data, NodeManager nodeManager) {
		super(name, nodeManager);
		this.data = data;
	}

	/**
	 * Applies packager with overridden parameters. This may replace default node packager
	 * or add additional packager.
	 * @param project Project to apply packager to
	 */
	public void apply(Project project) {

		project.getTasks().register(setupTaskName.get(), PackagerSetupTask.class, t -> {
			t.setGroup(NodePlugin.NODE_GROUP);
			t.setDescription("Prepares specific version of '" + name + "' packager.");
		});

		String taskName = capitalize(name) + "Task";
		project.getExtensions().getExtraProperties().set(taskName, PackagerTask.class);
		project.getTasks().withType(PackagerTask.class, t -> {
			t.setGroup(NodePlugin.NODE_GROUP);
			t.setDescription("Executes '" + name + "' command.");
		});

		if (cli.get() != null) {
			String cliName = capitalize(cli.get().getName()) + "Task";
			project.getExtensions().getExtraProperties().set(cliName, PackagerCliTask.class);
			project.getTasks().withType(PackagerCliTask.class, t -> {
				t.setGroup(NodePlugin.NODE_GROUP);
				t.setDescription("Executes cli using '" + cli.get().getName() + "' command.");
			});
		}

	}

	/**
	 * Applies this packager as default node. This means the NodeManager will
	 * provide working directory for the execution. The command and other
	 * specific can't be overridden.
	 * @param project Project to apply packager to
	 */
	public void applyDefault(Project project) {

		String taskName = capitalize(name) + "Task";
		project.getExtensions().getExtraProperties().set(taskName, DefaultPackagerTask.class);
		project.getTasks().withType(DefaultPackagerTask.class, t -> {
			t.setGroup(NodePlugin.NODE_GROUP);
			t.setDescription("Executes '" + name + "' command.");
		});

		project.getTasks().register(NodePlugin.NODE_INSTALL_TASK_NAME, NodeInstallTask.class, t -> {
			t.setGroup(NodePlugin.NODE_GROUP);
			t.setDescription(NodePlugin.NODE_INSTALL_TASK_DESC);
		});

		if (cli.get() != null) {
			String cliName = capitalize(cli.get().getName()) + "Task";
			project.getExtensions().getExtraProperties().set(cliName, DefaultPackagerCliTask.class);
			project.getTasks().withType(DefaultPackagerCliTask.class, t -> {
				t.setGroup(NodePlugin.NODE_GROUP);
				t.setDescription("Executes cli using '" + cli.get().getName() + "' command.");
			});
		}

	}

	/**
	 * Post evaluation process of packager.
	 * @param project Project with applied packager
	 * @param nodeExtension Final node configuration
	 */
	public void afterEvaluate(Project project, NodeExtension nodeExtension) {
		setOnSystemPath(!nodeExtension.getDownload());
		setPlatformSpecific(nodeExtension.getPlatformSpecific());
		getCli().ifPresent(c -> c.setPlatformSpecific(nodeExtension.getPlatformSpecific()));
		if (nodeExtension.getDownload()) {
			project.getTasks().withType(PackagerSetupTask.class, t -> t.dependsOn(NodePlugin.NODE_SETUP_TASK_NAME));
			project.getTasks().withType(PackagerTask.class, t -> t.dependsOn(setupTaskName.get()));
			project.getTasks().withType(NodeInstallTask.class, t -> t.dependsOn(setupTaskName.get()));

			if (cli.get() != null) {
				project.getTasks().withType(PackagerCliTask.class, t -> t.dependsOn(setupTaskName.get()));
			}
		}

	}

	/**
	 * Post evaluation of default packager.
	 * @param project Project with default packager applied
	 * @param nodeExtension Final node configuration
	 */
	public void afterEvaluateDefault(Project project, NodeExtension nodeExtension) {
		setOnSystemPath(!nodeExtension.getDownload());
		setPlatformSpecific(nodeExtension.getPlatformSpecific());
		getCli().ifPresent(c -> c.setPlatformSpecific(nodeExtension.getPlatformSpecific()));
		if (nodeExtension.getDownload()) {
			project.getTasks().withType(DefaultPackagerTask.class, t -> t.dependsOn(NodePlugin.NODE_SETUP_TASK_NAME));
			project.getTasks().withType(NodeInstallTask.class, t -> t.dependsOn(NodePlugin.NODE_SETUP_TASK_NAME));

			if (cli.get() != null) {
				project.getTasks().withType(DefaultPackagerCliTask.class, t -> t.dependsOn(NodePlugin.NODE_SETUP_TASK_NAME));
			}
		}
	}

	@Override
	protected String getCommand() {
		return data.getCommand();
	}

	@Override
	protected Path getExecutableBinDir() {
		return getPlatformSpecific().getBinPath(workingDir.get());
	}

	@Override
	protected Optional<Path> getScriptFile() {
		Objects.requireNonNull(nodeManager, "Missing NodeManager");
		return data.getLocalScript().map(nodeManager.getPackagerWorkingDir()::resolve);
	}

	public Optional<PackagerCliInternal> getCli() {
		return Optional.ofNullable(cli.get());
	}

	/**
	 * Packager configurable data. This method is used internally to
	 * get data for DSL configuration.
	 * @return Packager data to be configured from DSL
	 */
	PackagerData getData() {
		return data;
	}

	public List<String> getInputFiles() {
		return data.getInputFiles();
	}

	public List<String> getOutputDirectories() {
		return data.getOutputDirectories();
	}

	public List<String> getOutputFiles() {
		return data.getOutputFiles();
	}

	/**
	 * Defines packager location directory, which is used to
	 * calculate executable when download is used.
	 * @param workingDir Working directory
	 * @return This instance of packager
	 */
	public PackagerInternal workingDir(Path workingDir) {
		this.data.setWorkingDir(workingDir);
		return this;
	}

	/**
	 * Get npm package name for download.
	 * @return Npm package name
	 */
	public String getNpmPackage() {
		return data.getNpmPackage();
	}

	/**
	 * Packager version is needed for npm install command. The
	 * missing version is represented by 'latest'. This allows
	 * build script to skip packager version.
	 * @return Packager version string required for install
	 */
	public String getVersion() {
		return data.getVersion().orElse("latest");
	}

	/**
	 * Gets a working directory of specific packager version for
	 * download.
	 * @return Packager download location as working directory
	 */
	public Path getWorkingDir() {
		return workingDir.get();
	}

	private Path computeWorkingDir() {
		Objects.requireNonNull(data.getWorkingDir(), "Missing working directory");
		String version = data.getVersion().map(v -> "-v" + v).orElse("-latest");
		return data.getWorkingDir().resolve(name + version);
	}

	private String computeSetupTaskName() {
		return name + "Setup";
	}

	@Nullable
	private PackagerCliInternal createCli() {
		return data.getCli()
				.map(d -> new PackagerCliInternal(d, this))
				.orElse(null);
	}

	private static String capitalize(String str) {
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	@Override
	void setExecutablePosixRights(Set<PosixFilePermission> permissions) throws IOException {
		super.setExecutablePosixRights(permissions);
		if (getCli().isPresent()) {
			cli.get().setExecutablePosixRights(permissions);
		}
	}

}
