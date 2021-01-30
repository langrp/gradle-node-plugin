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

import com.palawan.gradle.dsl.CustomPackager;
import com.palawan.gradle.dsl.PackagerCli;
import com.palawan.gradle.util.GroovySupport;
import groovy.lang.Closure;
import org.gradle.api.Action;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class PackagerData implements CustomPackager {

	private String command;
	private String version;
	private Path workingDir;
	private String npmPackage;
	@Nullable
	private String localScript;

	@Nullable
	private PackagerCliData cli;

	private List<String> inputFiles = List.of();
	private List<String> outputDirectories = List.of();
	private List<String> outputFiles = List.of();

	public Optional<PackagerCliData> getCli() {
		return Optional.ofNullable(cli);
	}

	/**
	 * Get value of command
	 *
	 * @return command
	 */
	@Override
	public String getCommand() {
		return command;
	}

	@Override
	public Optional<String> getCliCommand() {
		return Optional.ofNullable(cli).map(PackagerCliData::getCommand);
	}

	@Override
	public PackagerData setCommand(String command) {
		this.command = command;
		return this;
	}

	/**
	 * Get value of version
	 *
	 * @return version
	 */
	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(version);
	}

	@Override
	public PackagerData setVersion(String version) {
		this.version = version;
		return this;
	}

	/**
	 * Get value of workingDir
	 *
	 * @return workingDir
	 */
	@Override
	public Path getWorkingDir() {
		return workingDir;
	}

	@Override
	public PackagerData setWorkingDir(File workingDir) {
		this.workingDir = workingDir.toPath();
		return this;
	}

	public PackagerData setWorkingDir(Path workingDir) {
		this.workingDir = workingDir;
		return this;
	}

	/**
	 * Get value of npmPackage
	 *
	 * @return npmPackage
	 */
	@Override
	public String getNpmPackage() {
		return npmPackage;
	}

	@Override
	public PackagerData setNpmPackage(String npmPackage) {
		this.npmPackage = npmPackage;
		return this;
	}

	/**
	 * Get value of localScript
	 *
	 * @return localScript
	 */
	@Override
	public Optional<String> getLocalScript() {
		return Optional.ofNullable(localScript);
	}

	@Override
	public PackagerData cli(Action<PackagerCli> action) {
		PackagerCliData cli = new PackagerCliData();
		action.execute(cli);
		this.cli = cli;
		return this;
	}

	public PackagerData cli(Closure<PackagerCli> closure) {
		PackagerCliData cli = new PackagerCliData();
		GroovySupport.execute(closure, cli);
		this.cli = cli;
		return this;
	}

	@Override
	public PackagerData setLocalScript(String localScript) {
		this.localScript = localScript;
		return this;
	}

	/**
	 * Get value of inputFiles
	 *
	 * @return inputFiles
	 */
	@Override
	public List<String> getInputFiles() {
		return inputFiles;
	}

	@Override
	public PackagerData setInputFiles(List<String> inputFiles) {
		this.inputFiles = inputFiles;
		return this;
	}

	@Override
	public PackagerData addInputFile(String inputFile) {
		if (inputFiles.isEmpty()) {
			inputFiles = new ArrayList<>(4);
		}
		inputFiles.add(inputFile);
		return this;
	}

	/**
	 * Get value of outputDirectories
	 *
	 * @return outputDirectories
	 */
	@Override
	public List<String> getOutputDirectories() {
		return outputDirectories;
	}

	@Override
	public PackagerData setOutputDirectories(List<String> outputDirectories) {
		this.outputDirectories = outputDirectories;
		return this;
	}

	@Override
	public PackagerData addOutputDirectory(String outputDirectory) {
		if (outputDirectories.isEmpty()) {
			outputDirectories = new ArrayList<>(4);
		}
		outputDirectories.add(outputDirectory);
		return this;
	}

	/**
	 * Get value of outputFiles
	 *
	 * @return outputFiles
	 */
	@Override
	public List<String> getOutputFiles() {
		return outputFiles;
	}

	@Override
	public PackagerData setOutputFiles(List<String> outputFiles) {
		this.outputFiles = outputFiles;
		return this;
	}

	@Override
	public PackagerData addOutputFile(String outputFile) {
		if (outputFiles.isEmpty()) {
			outputFiles = new ArrayList<>(4);
		}
		outputFiles.add(outputFile);
		return this;
	}
}
