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

package com.palawan.gradle.dsl;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Interface represents known packager configuration
 *
 * @author petr.langr
 * @since 1.0.0
 */
public interface Packager {

	/**
	 * Configure packager command. Changing default command
	 * may cause incompatibility between system platforms. The
	 * default behaviour (command) takes care of adjusting
	 * windows command etc.
	 * @see NodeExtension#setDownload(boolean)
	 * @param command New packager command name
	 * @return This packager instance
	 */
	Packager setCommand(String command);

	/**
	 * Get packager command name
	 * @return Packager command name
	 */
	String getCommand();

	/**
	 * Defines packager CLI command if available. The same as
	 * to {@link #setCommand(String)} applies here.
	 * @param cliCommand New packager CLI command
	 * @return This packager instance
	 */
	Packager setCliCommand(String cliCommand);

	/**
	 * Get optional CLI command name.
	 * @return Optional CLI command name
	 */
	Optional<String> getCliCommand();

	/**
	 * Defines packager version to be used. This makes sense
	 * only when download is required. When not specified the
	 * 'latest' is used.
	 * @see NodeExtension#setDownload(boolean)
	 * @param version Packager version string ('6.14.4')
	 * @return This packager instance
	 */
	Packager setVersion(String version);

	/**
	 * Gets optional packager version.
	 * @return Optional packager version.
	 */
	Optional<String> getVersion();

	/**
	 * Defines base packager location. This makes sense only
	 * when download is required. The base packager location is
	 * used to download specific version into. Multiple packager
	 * versions may be stored here. Although only one can be used.
	 * @see NodeExtension#setDownload(boolean)
	 * @param workingDir Packager base download location
	 * @return This packager instance
	 */
	Packager setWorkingDir(File workingDir);

	/**
	 * Get packager base directory location
	 * @return Packager base directory as {@link Path}
	 */
	File getWorkingDir();

}
