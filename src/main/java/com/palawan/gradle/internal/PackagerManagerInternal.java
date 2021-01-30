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

package com.palawan.gradle.internal;

import com.palawan.gradle.dsl.CustomPackager;
import com.palawan.gradle.dsl.NodeExtension;
import com.palawan.gradle.dsl.Packager;
import com.palawan.gradle.dsl.PackagerManager;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class PackagerManagerInternal implements PackagerManager {

	private Project project;
	private Path baseDir;

	@Nullable
	private PackagerInternal packager;

	public PackagerManagerInternal(Project project, Path baseDir) {
		this.project = project;
		this.baseDir = baseDir;
	}

	@Override
	public void npm(Action<Packager> action) {
		configure(action, PackagerInternal.npm()
				.workingDir(baseDir.resolve("npm")));
	}

	@Override
	public void pnpm(Action<Packager> action) {
		configure(action, PackagerInternal.pnpm()
				.workingDir(baseDir.resolve("pnpm")));
	}

	@Override
	public void cnpm(Action<Packager> action) {
		configure(action, PackagerInternal.cnpm()
				.workingDir(baseDir.resolve("cnpm")));
	}

	@Override
	public void yarn(Action<Packager> action) {
		configure(action, PackagerInternal.yarn()
				.workingDir(baseDir.resolve("yarn")));
	}

	@Override
	public void custom(Action<CustomPackager> action) {
		configure(action, PackagerInternal.custom()
				.workingDir(baseDir.resolve("custom")));
	}

	public Optional<PackagerInternal> getPackager() {
		return Optional.ofNullable(packager);
	}

	public void afterEvaluate(Project project, NodeExtension nodeExtension) {
		if (packager != null) {
			packager.afterEvaluate(project, nodeExtension);
		}
	}

	private void configure(Action<? super CustomPackager> action, PackagerInternal packager) {
		if (this.packager != null) {
			throw new NodeException("Multiple packagers defined. Please configure single packager!");
		}

		action.execute(packager.getData());
		packager.apply(project);

		this.packager = packager;
	}

}
