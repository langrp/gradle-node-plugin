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

package com.palawan.gradle;

import com.palawan.gradle.dsl.NodeExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author petr.langr
 * @since 1.0.0
 */
public class NodePlugin implements Plugin<Project> {

	/** Node extension name */
	public static final String EXTENSION_NAME = "node";

	/** Node task group */
	public static final String NODE_GROUP = "Node";

	/** Node task type name */
	public static final String NODE_TASK_TYPE = "NodeTask";
	/** Description of node tasks */
	public static final String NODE_TASK_DESC = "Executes node command.";

	/** Download/setup local node task (executed only when download required) */
	public static final String NODE_SETUP_TASK_NAME = "nodeSetup";
	/** Description of node setup task */
	public static final String NODE_SETUP_TASK_DESC = "Prepares specific version of NodeJS.";

	/** Install node packages task name */
	public static final String NODE_INSTALL_TASK_NAME = "nodeInstall";
	/** Install node packages task description */
	public static final String NODE_INSTALL_TASK_DESC = "Install node packages using chosen packager";

	/** Latest LTS version used if no other defined */
	public static final String LTS_VERSION = "12.16.3";
	public static final String LTS_NPM_VERSION = "6.14.4";

	@Override
	public void apply(Project project) {
		NodeExtension extension = addExtension(project);

		extension.getNodeManager().apply(project);

		project.afterEvaluate(this::afterEvaluate);
	}

	private NodeExtension addExtension(Project project) {
		return project.getExtensions().create(EXTENSION_NAME, NodeExtension.class, project);
	}

	private void afterEvaluate(Project project) {
		NodeExtension nodeExtension = NodeExtension.get(project);
		nodeExtension.getNodeManager().afterEvaluate(project, nodeExtension);
		nodeExtension.getPackagerManager().afterEvaluate(project, nodeExtension);
	}

}
