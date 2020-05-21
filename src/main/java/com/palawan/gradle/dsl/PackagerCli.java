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

import java.util.Optional;

/**
 * Packager CLI configuration
 *
 * @author petr.langr
 * @since 1.0.0
 */
public interface PackagerCli {

	/**
	 * Configure packager CLI command. Changing default command
	 * may cause incompatibility between system platforms. The
	 * default behaviour (command) takes care of adjusting
	 * windows command etc.
	 * @see NodeExtension#setDownload(boolean)
	 * @param command New packager CLI command name
	 * @return This packager CLI instance
	 */
	PackagerCli setCommand(String command);

	/**
	 * Get packager CLI command name
	 * @return Packager CLI command name
	 */
	String getCommand();

	/**
	 * Defines path to local script if available. By local script
	 * is meant location to node executable script within
	 * {@code node_modules/<package>}. If packager defines this
	 * script and the given file exists, it will be used instead
	 * of any specific version of packager CLI.
	 * <p>This allows to define your package.json with packager
	 * version</p>
	 * @param localScript Local script path string
	 * @return This packager CLI instance
	 */
	PackagerCli setLocalScript(String localScript);

	/**
	 * Get optional local node script to execute packager via node.
	 * @return Local script path as string
	 */
	Optional<String> getLocalScript();

}
