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

package com.palawan.gradle.tasks;

import com.palawan.gradle.dsl.NodeExtension;
import com.palawan.gradle.util.PlatformSpecific;
import com.palawan.gradle.util.ValueHolder;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class NodeSetupTask extends DefaultTask {

	private final PlatformSpecific platformSpecific = PlatformSpecific.getInstance();
	private final ValueHolder<NodeExtension> nodeExtension = ValueHolder.racy(() -> NodeExtension.get(getProject()));

	@TaskAction
	public void execute() {
		NodeExtension extension = nodeExtension.get();
		if (extension.getDownload()) {
			addRepository();
			unpackNode();
		}
	}

	@Input
	public Set<Object> getInputProperties() {
		NodeExtension extension = nodeExtension.get();
		return Set.of(
				extension.getDownload(),
				extension.getUrl(),
				extension.getWorkingDir().toString(),
				extension.getVersion()
		);
	}

	@OutputDirectory
	public Path getOutputDirectory() {
		return nodeExtension.get().getWorkingDir();
	}

	private void addRepository() {
		getProject().getRepositories().ivy(r -> {
			NodeExtension extension = nodeExtension.get();
			r.setUrl(extension.getUrl());
			r.patternLayout(l -> {
				//https://nodejs.org/dist/v12.8.1/node-v12.8.1-linux-arm64.tar.gz
				l.artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]");
				l.ivy("v[revision]/ivy.xml");
			});
			r.metadataSources(IvyArtifactRepository.MetadataSources::artifact);
		});
	}

	private void unpackNode() {
		getProject().copy(s -> {
			NodeExtension extension = nodeExtension.get();
			FileTree archive = platformSpecific.isWindows() ?
					getProject().zipTree(resolveNodeFile()) :
					getProject().tarTree(resolveNodeFile());
			s.from(archive);
			s.into(extension.getWorkingDir());
		});

		/* Defines downloaded node scripts as executable on Unix base systems.
		 * This is important for tests to use system path with downloaded scripts */
		if (!platformSpecific.isWindows()) {
			nodeExtension.get().getNodeManager().setExecutablePosixRights();
		}
	}

	private File resolveNodeFile() {
		Dependency dep = getProject().getDependencies().create(getDependency());
		Configuration conf = getProject().getConfigurations().detachedConfiguration(dep);
		conf.setTransitive(false);
		Set<File> files = conf.resolve();
		return files.iterator().next();
	}

	private String getDependency() {
		NodeExtension extension = nodeExtension.get();
		String type = platformSpecific.isWindows() ? "zip" : "tar.gz";
		String osName = platformSpecific.getOsName();
		String osArch = platformSpecific.getOsArch();
		return "org.nodejs:node:"+extension.getVersion()+":"+osName+"-"+osArch+"@"+type;
	}

}
