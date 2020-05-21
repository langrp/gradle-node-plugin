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

package com.palawan.gradle.util;

import org.gradle.api.GradleException;

import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public interface PlatformSpecific {

	String OS_NAME = "os.name";

	String OS_ARCH = "os.arch";

	/**
	 * Default platform specific utility uses system environment properties.
	 * @return Singleton instance of default platform specifics
	 */
	static PlatformSpecific getInstance() {
		return DefaultPlatformSpecific.INSTANCE;
	}

	/**
	 * Custom platform specific utility using defined properties. This method
	 * is used only in unit test cases.
	 * @param properties System properties
	 * @return New instance of platform specifics using given properties
	 */
	static PlatformSpecific getInstance(Properties properties) {
		return new DefaultPlatformSpecific(properties);
	}

	/**
	 * Gets operating system name usable for nodejs. Which means whatever
	 * system the plugin is running should reflect its implementation
	 * name in NodeJS directory.
	 * @return Operating system name for NodeJS implementation
	 */
	String getOsName();

	/**
	 * Gets operating architecture name usable for nodejs. Which means whatever
	 * system the plugin is running should reflect its implementation
	 * name in NodeJS directory.
	 * @return System architecture name for NodeJS implementation
	 */
	String getOsArch();

	/**
	 * Is plugin running on windows?
	 * @return {@code true} for windows
	 */
	default boolean isWindows() {
		return "win".equals(getOsName());
	}

	/**
	 * Translates command for underlying operating system. More precisely
	 * windows commands use '.cmd' suffix.
	 * @param command Command base
	 * @return Translated command
	 */
	default String getCommand(String command) {
		return isWindows() ? command + ".cmd" : command;
	}

	/**
	 * Translates executable for underlying operating system. More precisely
	 * windows executables use '.exe' suffix.
	 * @param exec Executable base
	 * @return Translated executable
	 */
	default String getExecutable(String exec) {
		return isWindows() ? exec + ".exe" : exec;
	}

	static String execute(String... command) throws IOException, InterruptedException {
		Process process = new ProcessBuilder()
				.command(command)
				.redirectInput(ProcessBuilder.Redirect.PIPE)
				.redirectError(ProcessBuilder.Redirect.PIPE)
				.start();
		process.waitFor(60L, TimeUnit.SECONDS);

		try (InputStream is = process.getInputStream()) {
			return new BufferedReader(new InputStreamReader(is)).readLine().trim();
		}
	}

}

class DefaultPlatformSpecific implements PlatformSpecific {

	static final DefaultPlatformSpecific INSTANCE = new DefaultPlatformSpecific(System.getProperties());

	private final ValueHolder<String> osName = ValueHolder.racy(this::osName);
	private final ValueHolder<String> osArch = ValueHolder.racy(this::osArch);

	private final Properties properties;

	public DefaultPlatformSpecific(Properties properties) {
		this.properties = properties;
	}

	@Override
	public String getOsName() {
		return osName.get();
	}

	@Override
	public String getOsArch() {
		return osArch.get();
	}

	private String osName() {
		String name = properties.getProperty(OS_NAME).toLowerCase(Locale.ENGLISH);
		if (name.contains("windows")) return "win";
		else if (name.contains("mac")) return "darwin";
		else if (name.contains("linux")) return "linux";
		else if (name.contains("freebsd")) return "linux";
		else if (name.contains("sunos")) return "sunos";
		throw new IllegalStateException("Unsupported OS " + name);
	}

	private String osArch() {
		String name = properties.getProperty(OS_ARCH).toLowerCase();

		try {
			if (name.equals("arm") || name.startsWith("aarch")) {
				String arch = PlatformSpecific.execute("uname", "-m");
				return "armv8l".equals(arch) ? "arm64" : arch;
			} else if (name.contains("64")) {
				return "x64";
			}
			return "x86";
		} catch (IOException | InterruptedException e) {
			throw new GradleException("Unable to get system arch.", e);
		}
	}

}
