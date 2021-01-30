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

package com.palawan.gradle.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public interface ProcessExecutor {

	static ProcessExecutor getInstance() {
		return DefaultProcessExecutor.INSTANCE;
	}

	String execute(String... command) throws IOException, InterruptedException;

}

class DefaultProcessExecutor implements ProcessExecutor {

	static final DefaultProcessExecutor INSTANCE = new DefaultProcessExecutor();

	@Override
	public String execute(String... command) throws IOException, InterruptedException {
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
