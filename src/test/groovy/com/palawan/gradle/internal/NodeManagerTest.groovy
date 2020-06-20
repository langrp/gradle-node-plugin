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
import com.palawan.gradle.tasks.DefaultPackagerCliTask
import com.palawan.gradle.tasks.DefaultPackagerTask
import com.palawan.gradle.tasks.NodeTask
import spock.lang.IgnoreIf

import java.nio.file.Files
import java.nio.file.Path
/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
class NodeManagerTest extends AbstractProjectTest {

    NodeManager nodeManager

    def setup() {
        nodeManager = nodeExtension.getNodeManager()
    }

    def "Apply"() {

        when:
        nodeManager.apply(project)

        then:
        project.getExtensions().getExtraProperties().get(NodePlugin.NODE_TASK_TYPE) == NodeTask.class
        project.getExtensions().getExtraProperties().get("NpmTask") == DefaultPackagerTask.class
        project.getExtensions().getExtraProperties().get("NpxTask") == DefaultPackagerCliTask.class

        when:
        def versionTask = project.getTasks().create("nodeVersion", NodeTask.class)

        then:
        versionTask.group == NodePlugin.NODE_GROUP
        versionTask.description == NodePlugin.NODE_TASK_DESC

        when:
        def setupTask = project.getTasks().named(NodePlugin.NODE_SETUP_TASK_NAME)

        then:
        setupTask.get().group == NodePlugin.NODE_GROUP
        setupTask.get().description == NodePlugin.NODE_SETUP_TASK_DESC

    }

    def "AfterEvaluate"() {

        given:
        nodeExtension.setDownload(true)
        nodeManager.apply(project)

        and:
        def versionTask = project.getTasks().create("nodeVersion", NodeTask.class)

        when:
        nodeManager.afterEvaluate(project, nodeExtension)

        then:
        versionTask.getDependsOn().contains(NodePlugin.NODE_SETUP_TASK_NAME)

    }

    def "AfterEvaluate no download"() {

        given:
        nodeManager.apply(project)

        and:
        def versionTask = project.getTasks().create("nodeVersion", NodeTask.class)

        when:
        nodeManager.afterEvaluate(project, nodeExtension)

        then:
        versionTask.getDependsOn().isEmpty()

    }

    def "ExecutableData download"() {

        given:
        mockLinux()

        and:
        nodeExtension.setDownload(true)

        and:
        nodeManager.afterEvaluate(project, nodeExtension)

        when:
        def data = nodeManager.executableData(["/tmp/junit/npm-cli.js"])

        then:
        isSameFile(data.executable, testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-linux-x64/bin/node").toString())
        data.args == ["/tmp/junit/npm-cli.js"]

    }

    def "ExecutableData win download"() {

        given:
        mockWindows()

        and:
        nodeExtension.setDownload(true)

        and:
        nodeManager.afterEvaluate(project, nodeExtension)

        when:
        def data = nodeManager.executableData(["/tmp/junit/npm-cli.js"])

        then:
        isSameFile(data.executable, testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-win-x64/node.exe").toString())
        data.args == ["/tmp/junit/npm-cli.js"]

    }

    def "ExecutableData no download"() {

        given:
        mockLinux()

        and:
        nodeManager.afterEvaluate(project, nodeExtension)

        when:
        def data = nodeManager.executableData(["/tmp/junit/npx-cli.js"])

        then:
        data.executable == "node"
        data.args == ["/tmp/junit/npx-cli.js"]

    }

    def "ExecutableData command"() {

        given:
        mockLinux()

        and:
        nodeExtension.setCommand("nodejs")
        nodeExtension.setDownload(true)

        and:
        nodeManager.afterEvaluate(project, nodeExtension)

        when:
        def data = nodeManager.executableData(["/tmp/junit/npm-cli.js"])

        then:
        isSameFile(data.executable, testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-linux-x64/bin/nodejs").toString())
        data.args == ["/tmp/junit/npm-cli.js"]

    }

    def "GetData"() {

        when:
        def data = nodeManager.getData()

        then:
        data.getCommand() == "node"
        data.getVersion() == "12.16.3"
        isSameFile(data.getWorkingDir(), testProjectDir.resolve(".gradle/nodejs"))
        data.getUrl() == "https://nodejs.org/dist"

    }

    def "GetData updated"() {

        given:
        nodeExtension.setCommand("nodejs")
        nodeExtension.setVersion("10.0.0")
        nodeExtension.setWorkingDir(testProjectDir.resolve(".nodejs").toFile())
        nodeExtension.setUrl("http://company.org/nodejs/dist")

        when:
        def data = nodeManager.getData()

        then:
        data.getCommand() == "nodejs"
        data.getVersion() == "10.0.0"
        isSameFile(data.getWorkingDir(), testProjectDir.resolve(".nodejs"))
        data.getUrl() == "http://company.org/nodejs/dist"

    }

    def "GetPackager"() {

        when:
        def packager = nodeManager.getPackager()

        then:
        packager.getNpmPackage() == "npm"

    }

    def "GetBinDir"() {

        given:
        mockLinux()

        when:
        def bin = nodeManager.getBinDir()

        then:
        isSameFile(bin, testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-linux-x64/bin"))

    }

    def "GetBinDir windows"() {

        given:
        mockWindows()

        when:
        def bin = nodeManager.getBinDir()

        then:
        isSameFile(bin, testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-win-x64"))

    }

    def "GetPackagerWorkingDir"() {

        given:
        mockLinux()

        when:
        def npm = nodeManager.getPackagerWorkingDir()

        then:
        isSameFile(npm, testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-linux-x64/lib/node_modules/npm"))

    }

    def "GetPackagerWorkingDir windows"() {

        given:
        mockWindows()

        when:
        def npm = nodeManager.getPackagerWorkingDir()

        then:
        isSameFile(npm, testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-win-x64/node_modules/npm"))

    }

    @IgnoreIf({ System.getProperty("os.name").contains("windows") })
    def "setExecutablePosixRights"() {

        given:
        def nodeJs = defineNodeExecutables()

        and:
        mockLinux()

        when:
        nodeManager.setExecutablePosixRights()

        then:
        Files.isExecutable(nodeJs.resolve("bin/node"))
        Files.isExecutable(nodeJs.resolve("bin/npm"))
        Files.isSymbolicLink(nodeJs.resolve("bin/npm"))
        Files.isExecutable(nodeJs.resolve("bin/npx"))
        Files.isSymbolicLink(nodeJs.resolve("bin/npx"))
        Files.isExecutable(nodeJs.resolve("bin/npm"))
        Files.isExecutable(nodeJs.resolve("lib/node_modules/npm/bin/npm-cli.js"))
        Files.isExecutable(nodeJs.resolve("lib/node_modules/npm/bin/npx-cli.js"))

    }

    def "setExecutablePosixRights not downloaded"() {

        given:
        mockLinux()

        and:
        def nodeJs = defineNodeExecutables(".gradle/nodejs")

        when:
        nodeManager.setExecutablePosixRights()

        then:
        !Files.isExecutable(nodeJs.resolve("bin/node"))

    }

    def "setExecutablePosixRights windows"() {

        given:
        mockWindows()

        when:
        nodeManager.setExecutablePosixRights()

        then:
        thrown(NodeException.class)

    }

//    def "setExecutablePosixRights IOException"() {
//
//        given:
//        def packager = Mock(PackagerInternal)
//        packager.setExecutablePosixRights(_) >> { throw new IOException("Unable to set permissions") }
//
//        and:
//        nodeManager.packager = packager
//
//        and:
//        mockLinux()
//
//        when:
//        nodeManager.setExecutablePosixRights()
//
//        then:
//        thrown(NodeException.class)
//
//    }

    private Path defineNodeExecutables() {
        defineNodeExecutables(".gradle/nodejs/node-v${NodePlugin.LTS_VERSION}-linux-x64")
    }

    private Path defineNodeExecutables(String path) {
        Path nodeJs = testProjectDir.resolve(path)

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
