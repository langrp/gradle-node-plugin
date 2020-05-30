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

package com.palawan.gradle.internal

import com.palawan.gradle.AbstractProjectTest
import com.palawan.gradle.NodePlugin
import com.palawan.gradle.internal.data.PackagerData
import com.palawan.gradle.tasks.*
import spock.lang.IgnoreIf

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
class PackagerInternalTest extends AbstractProjectTest {

    PackagerInternal packager

    def "Apply"() {

        given:
        packager = PackagerInternal.npm()

        when:
        packager.apply(project)


        then:
        project.getExtensions().getExtraProperties().get("NpmTask") == PackagerTask.class
        project.getExtensions().getExtraProperties().get("NpxTask") == PackagerCliTask.class
        project.getTasks().findByName(NodePlugin.NODE_INSTALL_TASK_NAME) == null

        when:
        def setupTask = project.getTasks().named("npmSetup")

        then:
        setupTask.get().group == NodePlugin.NODE_GROUP
        setupTask.get().description == "Prepares specific version of 'npm' packager."

        when:
        def npmTask = project.getTasks().create("npmVersion", PackagerTask.class)
        def npxTask = project.getTasks().create("npxVersion", PackagerCliTask.class)

        then:
        npmTask.group == NodePlugin.NODE_GROUP
        npxTask.group == NodePlugin.NODE_GROUP
        npmTask.description == "Executes 'npm' command."
        npxTask.description == "Executes cli using 'npx' command."

    }

    def "Apply yarn"() {

        given:
        packager = PackagerInternal.yarn()

        when:
        packager.apply(project)

        then:
        project.getExtensions().getExtraProperties().get("YarnTask") == PackagerTask.class
        project.getTasks().findByName(NodePlugin.NODE_INSTALL_TASK_NAME) == null

        when:
        def setupTask = project.getTasks().named("yarnSetup")

        then:
        setupTask.get().group == NodePlugin.NODE_GROUP
        setupTask.get().description == "Prepares specific version of 'yarn' packager."

        when:
        def npmTask = project.getTasks().create("yarnVersion", PackagerTask.class)

        then:
        npmTask.group == NodePlugin.NODE_GROUP
        npmTask.description == "Executes 'yarn' command."

    }

    def "ApplyDefault"() {

        given:
        packager = PackagerInternal.npm(nodeExtension.getNodeManager())

        when:
        packager.applyDefault(project)

        then:
        project.getExtensions().getExtraProperties().get("NpmTask") == DefaultPackagerTask.class
        project.getExtensions().getExtraProperties().get("NpxTask") == DefaultPackagerCliTask.class

        when:
        def npmTask = project.getTasks().create("npmVersion", DefaultPackagerTask.class)
        def npxTask = project.getTasks().create("npxVersion", DefaultPackagerCliTask.class)
        def installTask = project.getTasks().getByName(NodePlugin.NODE_INSTALL_TASK_NAME)

        then:
        npmTask.group == NodePlugin.NODE_GROUP
        npxTask.group == NodePlugin.NODE_GROUP
        installTask.group == NodePlugin.NODE_GROUP
        npmTask.description == "Executes 'npm' command."
        npxTask.description == "Executes cli using 'npx' command."
        installTask.description == NodePlugin.NODE_INSTALL_TASK_DESC

    }


    def "AfterEvaluate"() {

        given:
        packager = PackagerInternal.npm()

        and:
        packager.applyDefault(project)

        and:
        packager.apply(project)

        and:
        def setupTask = project.getTasks().named("npmSetup", PackagerSetupTask.class)
        def npmTask = project.getTasks().create("npmVersion", PackagerTask.class)
        def npxTask = project.getTasks().create("npxVersion", PackagerCliTask.class)
        def installTask = project.getTasks().getByName(NodePlugin.NODE_INSTALL_TASK_NAME)

        when:
        packager.afterEvaluate(project, nodeExtension)

        then:
        get(packager, "platformSpecific") != null
        get(packager.cli.get(), "platformSpecific") != null
        setupTask.get().getDependsOn().isEmpty()
        npmTask.getDependsOn().isEmpty()
        npxTask.getDependsOn().isEmpty()
        installTask.getDependsOn().isEmpty()

    }

    def "AfterEvaluate download"() {

        given:
        packager = PackagerInternal.npm()

        and:
        nodeExtension.setDownload(true)

        and:
        packager.applyDefault(project)

        and:
        packager.apply(project)

        and:
        def setupTask = project.getTasks().named("npmSetup", PackagerSetupTask.class)
        def npmTask = project.getTasks().create("npmVersion", PackagerTask.class)
        def npxTask = project.getTasks().create("npxVersion", PackagerCliTask.class)
        def installTask = project.getTasks().getByName(NodePlugin.NODE_INSTALL_TASK_NAME)

        when:
        packager.afterEvaluate(project, nodeExtension)

        then:
        setupTask.get().getDependsOn().toList() == [NodePlugin.NODE_SETUP_TASK_NAME]
        npmTask.getDependsOn().toList() == ["npmSetup"]
        npxTask.getDependsOn().toList() == ["npmSetup"]
        installTask.getDependsOn().toList() == ["npmSetup"]

    }

    def "AfterEvaluate download yarn"() {

        given:
        packager = PackagerInternal.yarn()

        and:
        nodeExtension.setDownload(true)

        and:
        packager.applyDefault(project)

        and:
        packager.apply(project)

        and:
        def setupTask = project.getTasks().named("yarnSetup", PackagerSetupTask.class)
        def yarnTask = project.getTasks().create("yarnVersion", PackagerTask.class)
        def cliTask = project.getTasks().create("unknown", PackagerCliTask.class)
        def installTask = project.getTasks().getByName(NodePlugin.NODE_INSTALL_TASK_NAME)

        when:
        packager.afterEvaluate(project, nodeExtension)

        then:
        setupTask.get().getDependsOn().toList() == [NodePlugin.NODE_SETUP_TASK_NAME]
        yarnTask.getDependsOn().toList() == ["yarnSetup"]
        installTask.getDependsOn().toList() == ["yarnSetup"]
        cliTask.getDependsOn().isEmpty()

    }

    def "AfterEvaluateDefault"() {

        given:
        packager = PackagerInternal.npm(nodeExtension.getNodeManager())

        and:
        packager.applyDefault(project)

        and:
        def npmTask = project.getTasks().create("npmVersion", DefaultPackagerTask.class)
        def npxTask = project.getTasks().create("npxVersion", DefaultPackagerCliTask.class)
        def installTask = project.getTasks().getByName(NodePlugin.NODE_INSTALL_TASK_NAME)

        when:
        packager.afterEvaluateDefault(project, nodeExtension)

        then:
        npmTask.getDependsOn().isEmpty()
        npxTask.getDependsOn().isEmpty()
        installTask.getDependsOn().isEmpty()

    }

    def "AfterEvaluateDefault download"() {

        given:
        nodeExtension.setDownload(true)

        and:
        packager = PackagerInternal.npm(nodeExtension.getNodeManager())

        and:
        packager.applyDefault(project)

        and:
        def npmTask = project.getTasks().create("npmVersion", DefaultPackagerTask.class)
        def npxTask = project.getTasks().create("npxVersion", DefaultPackagerCliTask.class)
        def installTask = project.getTasks().getByName(NodePlugin.NODE_INSTALL_TASK_NAME)

        when:
        packager.afterEvaluateDefault(project, nodeExtension)

        then:
        npmTask.getDependsOn().toList() == [NodePlugin.NODE_SETUP_TASK_NAME]
        npxTask.getDependsOn().toList() == [NodePlugin.NODE_SETUP_TASK_NAME]
        installTask.getDependsOn().toList() == [NodePlugin.NODE_SETUP_TASK_NAME]

    }

    def "AfterEvaluateDefault download yarn"() {

        given:
        nodeExtension.setDownload(true)

        and:
        packager = PackagerInternal.yarn()

        and:
        packager.applyDefault(project)

        and:
        def yarnTask = project.getTasks().create("yarnVersion", DefaultPackagerTask.class)
        def installTask = project.getTasks().getByName(NodePlugin.NODE_INSTALL_TASK_NAME)

        when:
        packager.afterEvaluateDefault(project, nodeExtension)

        then:
        yarnTask.getDependsOn().toList() == [NodePlugin.NODE_SETUP_TASK_NAME]
        installTask.getDependsOn().toList() == [NodePlugin.NODE_SETUP_TASK_NAME]

    }

    def "GetCommand"() {

        given:
        packager = PackagerInternal.npm()

        when:
        def command = packager.getCommand()

        then:
        command == "npm"

        when:
        packager.getData().setCommand("nodeNpm")
        def command2 = packager.getCommand()

        then:
        command2 == "nodeNpm"

    }

    def "GetExecutableBinDir"() {

        given:
        packager = PackagerInternal.cnpm().workingDir(testProjectDir.resolve(".gradle/cnpm"))

        when:
        def bin = packager.getExecutableBinDir()

        then:
        bin == testProjectDir.resolve(".gradle/cnpm/cnpm-latest/bin")

    }

    def "GetExecutableBinDir version"() {

        given:
        packager = PackagerInternal.pnpm().workingDir(testProjectDir.resolve(".gradle/pnpm"))

        and:
        packager.getData().setVersion("6.14.4")

        when:
        def bin = packager.getExecutableBinDir()

        then:
        bin == testProjectDir.resolve(".gradle/pnpm/pnpm-v6.14.4/bin")

    }

    def "GetExecutableBinDir no workingDir"() {

        given:
        packager = PackagerInternal.pnpm()

        and:
        packager.getData().setVersion("6.14.4")

        when:
        packager.getExecutableBinDir()

        then:
        thrown(NullPointerException.class)

    }

    def "GetScriptFile"() {

        given:
        mockLinux()

        and:
        packager = PackagerInternal.npm(nodeExtension.getNodeManager())

        when:
        def script = packager.getScriptFile()

        then:
        script.get() == testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-linux-x64/lib/node_modules/npm/bin/npm-cli.js")

    }

    def "GetScriptFile windows"() {

        given:
        mockWindows()

        and:
        packager = PackagerInternal.npm(nodeExtension.getNodeManager())

        when:
        def script = packager.getScriptFile()

        then:
        script.get() == testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-win-x64/node_modules/npm/bin/npm-cli.js")

    }

    def "GetScriptFile empty"() {

        given:
        packager = PackagerInternal.yarn()

        when:
        packager.getScriptFile()

        then:
        thrown(NullPointerException.class)

    }

    def "GetCli"() {

        given:
        packager = PackagerInternal.npm()

        when:
        def cli = packager.getCli()

        then:
        cli.isPresent()
        cli.get().getCommand() == "npx"

    }

    def "GetCli yarn"() {

        given:
        packager = PackagerInternal.yarn()

        when:
        def cli = packager.getCli()

        then:
        cli.isEmpty()

    }

    def "GetData"() {

        given:
        packager = PackagerInternal.custom()

        when:
        def data = packager.getData()

        then:
        data.getCommand() == "custom"
        data.getNpmPackage() == "custom"

    }

    def "GetInputFiles"() {

        given:
        packager = PackagerInternal.pnpm()

        when:
        def inputs = packager.getInputFiles()

        then:
        inputs == ["package.json"]

    }

    def "GetOutputDirectories"() {

        given:
        packager = PackagerInternal.yarn()

        when:
        def outpus = packager.getOutputDirectories()

        then:
        outpus == ["node_modules"]

    }

    def "GetOutputFiles"() {

        given:
        packager = PackagerInternal.npm()

        when:
        def outputs = packager.getOutputFiles()

        then:
        outputs == ["package-lock.json"]

    }

    def "GetNpmPackage"() {

        given:
        packager = PackagerInternal.pnpm()

        when:
        def npmPackage = packager.getNpmPackage()

        then:
        npmPackage == "pnpm"

    }

    def "GetNpmPackage other"() {

        given:
        packager = PackagerInternal.custom()

        and:
        packager.getData().setNpmPackage("cnpm-home")

        when:
        def npmPackage = packager.getNpmPackage()

        then:
        npmPackage == "cnpm-home"

    }

    def "GetVersion"() {

        given:
        packager = PackagerInternal.npm()

        and:
        packager.getData().setVersion("6.14.4")

        when:
        def version = packager.getVersion()

        then:
        version == "6.14.4"

    }

    def "GetVersion latest"() {

        given:
        packager = PackagerInternal.npm()

        when:
        def version = packager.getVersion()

        then:
        version == "latest"

    }

    def "getWorkingDir"() {

        given:
        packager = PackagerInternal.pnpm().workingDir(testProjectDir.resolve(".gradle/pnpm"))

        when:
        def dir = packager.getWorkingDir()

        then:
        dir == testProjectDir.resolve(".gradle/pnpm/pnpm-latest")

    }

    def "getWorkingDir version"() {

        given:
        packager = PackagerInternal.pnpm().workingDir(testProjectDir.resolve(".gradle/pnpm"))

        and:
        packager.getData().setVersion("6.14.4")

        when:
        def dir = packager.getWorkingDir()

        then:
        dir == testProjectDir.resolve(".gradle/pnpm/pnpm-v6.14.4")

    }

    def "executableData"() {

        given:
        mockLinux()

        and:
        packager = PackagerInternal.npm().workingDir(testProjectDir.resolve(".gradle/npm"))
        packager.setPlatformSpecific(nodeExtension.getPlatformSpecific())
        packager.setOnSystemPath(true)

        when:
        def data = packager.executableData(["-version"])

        then:
        data.executable == "npm"
        data.args == ["-version"]
        data.workingDir == null
        data.environmentVariables.isEmpty()
        data.path.isEmpty()
        !data.ignoreExitValue

    }

    def "executableData windows"() {

        given:
        mockWindows()

        and:
        packager = PackagerInternal.pnpm().workingDir(testProjectDir.resolve(".gradle/pnpm"))
        packager.setPlatformSpecific(nodeExtension.getPlatformSpecific())
        packager.setOnSystemPath(true)

        when:
        def data = packager.executableData(["-version"])

        then:
        data.executable == "pnpm"
        data.args == ["-version"]
        data.workingDir == null
        data.environmentVariables.isEmpty()
        data.path.isEmpty()
        !data.ignoreExitValue

    }

    def "executableData download"() {

        given:
        mockLinux()

        and:
        packager = PackagerInternal.yarn().workingDir(testProjectDir.resolve(".gradle/yarn"))
        packager.setPlatformSpecific(nodeExtension.getPlatformSpecific())
        packager.setOnSystemPath(false)

        when:
        def data = packager.executableData(["-version"])

        then:
        data.executable == testProjectDir.resolve(".gradle/yarn/yarn-latest/bin/yarn").toString()
        data.args == ["-version"]
        data.workingDir == null
        data.environmentVariables.isEmpty()
        data.path.isEmpty()
        !data.ignoreExitValue

    }

    def "executableData download windows"() {

        given:
        mockWindows()

        and:
        packager = PackagerInternal.yarn().workingDir(testProjectDir.resolve(".gradle/yarn"))
        packager.setPlatformSpecific(nodeExtension.getPlatformSpecific())
        packager.setOnSystemPath(false)

        when:
        def data = packager.executableData(["-version"])

        then:
        data.executable == testProjectDir.resolve(".gradle/yarn/yarn-latest/bin/yarn.cmd").toString()
        data.args == ["-version"]
        data.workingDir == null
        data.environmentVariables.isEmpty()
        data.path.isEmpty()
        !data.ignoreExitValue

    }

    def "executableData node script"() {

        given:
        mockLinux()

        and:
        nodeExtension.setDownload(true)
        nodeExtension.getNodeManager().afterEvaluate(project, nodeExtension)

        and:
        packager = PackagerInternal.npm(nodeExtension.getNodeManager())

        when:
        def data = packager.executableData(["-version"])

        then:
        data.executable == testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-linux-x64/bin/node").toString()
        data.args == [testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-linux-x64/lib/node_modules/npm/bin/npm-cli.js").toString(), "-version"]
        data.workingDir == null
        data.environmentVariables.isEmpty()
        data.path.isEmpty()
        !data.ignoreExitValue

    }

    def "executableData command"() {

        given:
        mockLinux()

        and:
        packager = PackagerInternal.npm().workingDir(testProjectDir.resolve(".gradle/npm"))
        packager.getData().setCommand("npx")
        packager.setOnSystemPath(false)

        when:
        def data = packager.executableData(["-version"])

        then:
        data.executable == testProjectDir.resolve(".gradle/npm/npm-latest/bin/npx").toString()
        data.args == ["-version"]
        data.workingDir == null
        data.environmentVariables.isEmpty()
        data.path.isEmpty()
        !data.ignoreExitValue


    }

    def "executableData nodeManager notOnSystem"() {

        given:
        mockWindows()

        and:
        nodeExtension.setDownload(false)
        nodeExtension.getNodeManager()afterEvaluate(project, nodeExtension)

        and:
        packager = PackagerInternal.npm(nodeExtension.getNodeManager())

        when:
        def data = packager.executableData(["-version"])

        then:
        data.executable == "npm"
        data.args == ["-version"]
        data.workingDir == null
        data.environmentVariables.isEmpty()
        data.path.isEmpty()
        !data.ignoreExitValue

    }

    def "executableData no scriptFile"() {

        given:
        mockWindows()

        and:
        nodeExtension.setDownload(true)
        nodeExtension.nodeManager.afterEvaluate(project, nodeExtension)

        and:
        packager = new PackagerInternal("yarn", new PackagerData()
                .setCommand("yarn")
                .setNpmPackage("yarn"), nodeExtension.getNodeManager())

        when:
        packager.executableData(["-version"])

        then:
        thrown(NodeException.class)

    }

    @IgnoreIf({ System.getProperty("os.name").contains("windows") })
    def "setExecutablePosixRights"() {

        given:
        mockLinux()

        and:
        def nodeJs = defineNodeExecutables()

        and:
        packager = PackagerInternal.npm(nodeExtension.getNodeManager())

        and:
        Set<PosixFilePermission> executable = PosixFilePermissions.fromString("rwxr--r--");

        when:
        packager.setExecutablePosixRights(executable)

        then:
        !Files.isExecutable(nodeJs.resolve("bin/node"))
        Files.isExecutable(nodeJs.resolve("bin/npm"))
        Files.isSymbolicLink(nodeJs.resolve("bin/npm"))
        Files.isExecutable(nodeJs.resolve("bin/npx"))
        Files.isSymbolicLink(nodeJs.resolve("bin/npx"))

    }

    def "setExecutablePosixRights yarn"() {

        given:
        mockLinux()

        and:
        def nodeJs = defineNodeExecutables()

        and:
        packager = new PackagerInternal("yarn", new PackagerData()
                .setCommand("yarn")
                .setWorkingDir(testProjectDir.resolve(".gradle/yarn"))
                .setLocalScript("bin/yarn.js")
                .setNpmPackage("yarn"), nodeExtension.getNodeManager())

        and:
        Set<PosixFilePermission> executable = PosixFilePermissions.fromString("rwxr--r--");

        when:
        packager.setExecutablePosixRights(executable)

        then:
        !Files.isExecutable(nodeJs.resolve("bin/node"))
        !Files.isExecutable(nodeJs.resolve("bin/npm"))
        !Files.isSymbolicLink(nodeJs.resolve("bin/npm"))
        !Files.isExecutable(nodeJs.resolve("bin/npx"))
        !Files.isSymbolicLink(nodeJs.resolve("bin/npx"))

    }

    def "setExecutablePosixRights no script"() {

        given:
        mockLinux()

        and:
        packager = new PackagerInternal("yarn", new PackagerData()
                .setCommand("yarn")
                .setWorkingDir(testProjectDir.resolve(".gradle/yarn"))
                .setNpmPackage("yarn"), nodeExtension.getNodeManager())

        and:
        Set<PosixFilePermission> executable = PosixFilePermissions.fromString("rwxr--r--");

        when:
        packager.setExecutablePosixRights(executable)

        then:
        thrown(NodeException.class)

    }

    def "setExecutablePosixRights no nodeManager"() {

        given:
        mockLinux()

        and:
        packager = PackagerInternal.yarn()

        and:
        Set<PosixFilePermission> executable = PosixFilePermissions.fromString("rwxr--r--");

        when:
        packager.setExecutablePosixRights(executable)

        then:
        thrown(NullPointerException.class)

    }

    private Path defineNodeExecutables() {
        Path nodeJs = testProjectDir.resolve(".gradle/nodejs/node-v${NodePlugin.LTS_VERSION}-linux-x64")

        Files.createDirectories(nodeJs)
        Files.createDirectories(nodeJs.resolve("bin"))
        Files.createDirectories(nodeJs.resolve("lib/node_modules/npm/bin"))

        Files.writeString(nodeJs.resolve("bin/node"), "#!/bin/env node")
        Files.createFile(nodeJs.resolve("bin/npm"))
        Files.createFile(nodeJs.resolve("bin/npx"))
        Files.writeString(nodeJs.resolve("bin/node"), "#!/bin/env node")
        Files.writeString(nodeJs.resolve("lib/node_modules/npm/bin/npm-cli.js"), "#!/bin/env node")
        Files.writeString(nodeJs.resolve("lib/node_modules/npm/bin/npx-cli.js"), "#!/bin/env node")

        return nodeJs
    }

}