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

import org.gradle.api.Action;

/**
 * Packager manager interface to handle packager selection
 *
 * @author petr.langr
 * @since 1.0.0
 */
public interface PackagerManager {

	/**
	 * Configures node packager to use NPM with specific version.
	 * @param action action configures NPM packager
	 */
	void npm(Action<Packager> action);

	/**
	 * Configures node packager to use PNPM with specific version.
	 * @param action action configures PNPM packager
	 */
	void pnpm(Action<Packager> action);

	/**
	 * Configures node packager to use CNPM with specific version.
	 * @param action action configures CNPM packager
	 */
	void cnpm(Action<Packager> action);

	/**
	 * Configures node packager to use Yarn with specific version.
	 * @param action action configures Yarn packager
	 */
	void yarn(Action<Packager> action);

	/**
	 * Configures custom node packager to use.
	 * @param action action configures custom packager
	 */
	void custom(Action<CustomPackager> action);

}
