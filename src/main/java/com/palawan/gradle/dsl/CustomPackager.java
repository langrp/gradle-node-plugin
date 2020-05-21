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

import org.gradle.api.Action;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Interface represents custom packager configuration
 *
 * @author petr.langr
 * @since 1.0.0
 */
public interface CustomPackager extends Packager {

	/**
	 * @see Packager#setCommand(String)
	 */
	@Override
	CustomPackager setCommand(String command);

	/**
	 * Shortcut to cli { command = "xxx" }
	 * @see Packager#setCliCommand(String)
	 */
	@Override
	default CustomPackager setCliCommand(String cliCommand) {
		cli(c -> c.setCommand(cliCommand));
		return this;
	}

	/**
	 * @see Packager#setVersion(String)
	 */
	@Override
	CustomPackager setVersion(String version);

	/**
	 * @see Packager#setWorkingDir(File)
	 */
	@Override
	CustomPackager setWorkingDir(File workingDir);

	/**
	 * Defines NPM package name in order to download custom package.
	 * @param npmPackage NPM package name
	 * @return This packager instance
	 */
	CustomPackager setNpmPackage(String npmPackage);

	/**
	 * Gets NPM package name
	 * @return NPM package name
	 */
	String getNpmPackage();

	/**
	 * Defines path to local script if available. By local script
	 * is meant location to node executable script within
	 * {@code node_modules/<package>}. If packager defines this
	 * script and the given file exists, it will be used instead
	 * of any specific version of packager.
	 * <p>This allows to define your package.json with packager
	 * version</p>
	 * @param localScript Local script path string
	 * @return This packager instance
	 */
	CustomPackager setLocalScript(String localScript);

	/**
	 * Get optional local node script to execute packager via node.
	 * @return Local script path as string
	 */
	Optional<String> getLocalScript();

	/**
	 * Defines and configures packager CLI executor command.
	 * @param action Action to configure CLI executor
	 * @return This packager instance
	 */
	CustomPackager cli(Action<PackagerCli> action);


	/**
	 * Defines packager '{@code install}' command input files.
	 * Those can be defined either here or as task {@code nodeInstall}
	 * configuration. The path is defined as relative path to project
	 * directory.
	 * <pre>
	 * nodeInstall {
	 *     inputs.file("package.json")
	 * }
	 * </pre>
	 * @param inputFiles Input files relative path from project directory
	 * @return This packager instance
	 */
	CustomPackager setInputFiles(List<String> inputFiles);

	/**
	 * Adds new file as input to packager '{@code install}' command.
	 * @see #setInputFiles(List)
	 * @param inputFile Input file as relative path from project directory
	 * @return This packager instance
	 */
	CustomPackager addInputFile(String inputFile);

	/**
	 * Get packager '{@code install}' command input files as relative
	 * paths from project directory.
	 * @return Packager install input files
	 */
	List<String> getInputFiles();

	/**
	 * Defines packager '{@code install}' command output directories.
	 * Those can be defined either here or as task {@code nodeInstall}
	 * configuration. The path is defined as relative path to project
	 * directory.
	 * <pre>
	 * nodeInstall {
	 *     outputs.dir("node_modules")
	 * }
	 * </pre>
	 * @param outputDirectories Output directories relative path from
	 *                          project directory
	 * @return This packager instance
	 */
	CustomPackager setOutputDirectories(List<String> outputDirectories);

	/**
	 * Adds new directory as output to packager '{@code install}' command.
	 * @see #setOutputDirectories(List)
	 * @param outputDirectory Output directory as relative path from project
	 *                           directory
	 * @return This packager instance
	 */
	CustomPackager addOutputDirectory(String outputDirectory);

	/**
	 * Get packager '{@code install}' command output directories as relative
	 * paths from project directory.
	 * @return Packager install output directories
	 */
	List<String> getOutputDirectories();

	/**
	 * Defines packager '{@code install}' command output files.
	 * Those can be defined either here or as task {@code nodeInstall}
	 * configuration. The path is defined as relative path to project
	 * directory.
	 * <pre>
	 * nodeInstall {
	 *     outputs.file("package-lock.json")
	 * }
	 * </pre>
	 * @param outputFiles Output files relative path from project directory
	 * @return This packager instance
	 */
	CustomPackager setOutputFiles(List<String> outputFiles);

	/**
	 * Adds new file as output to packager '{@code install}' command.
	 * @see #setOutputFiles(List)
	 * @param outputFile Output file as relative path from project directory
	 * @return This packager instance
	 */
	CustomPackager addOutputFile(String outputFile);

	/**
	 * Get packager '{@code install}' command output files as relative
	 * paths from project directory.
	 * @return Packager install output files
	 */
	List<String> getOutputFiles();

}
