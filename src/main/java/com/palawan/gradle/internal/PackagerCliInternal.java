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

import com.palawan.gradle.internal.data.PackagerCliData;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;


/**
 * @author petr.langr
 * @since 1.0.0
 */
public class PackagerCliInternal extends AbstractExecutable {

	private final PackagerCliData data;

	PackagerCliInternal(PackagerCliData data, PackagerInternal parent) {
		super(data.getCommand(), parent);
		this.data = data;
	}

	@Override
	protected String getCommand() {
		return data.getCommand();
	}

	@Override
	protected Optional<Path> getScriptFile() {
		Objects.requireNonNull(nodeManager);
		return data.getLocalScript().map(nodeManager.getPackagerWorkingDir()::resolve);
	}

	/**
	 * Packager CLI name. The name represents lower case default
	 * command name, e.g. npx, npm, yarn etc..
	 *
	 * @return name Cli name
	 */
	public String getName() {
		return name;
	}

}
