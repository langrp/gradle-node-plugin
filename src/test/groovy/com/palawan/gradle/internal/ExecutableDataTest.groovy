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

import org.gradle.process.ExecSpec
import spock.lang.Specification

import java.nio.file.Paths
/**
 *
 * @author petr.langr* @since 1.0.0
 */
class ExecutableDataTest extends Specification {

    ExecSpec spec = Mock()

    def "executableData"() {

        given:
        def data = new ExecutableData()
                .setExecutable("node")
                .setArgs(["/tmp/junit/bin/npm-cli.js", "install"])
                .setWorkingDir(Paths.get("/tmp/junit").toFile())
                .setIgnoreExitValue(true)
                .addEnvironmentVariables([NODE_HOME: "/tmp/junit-nodejs/bin"])
                .addEnvironmentVariables([YARN_HOME: "/tmp/junit-yarn/bin"])
                .addEnvironmentVariables([:])
                .withPathLocation("/tmp/junit-nodejs/bin")
                .withPathLocation("/tmp/junit-npm/bin")

        when:
        data.execute(spec)

        then:
        with(spec) {
            1 * setIgnoreExitValue(true)
            1 * setExecutable("node")
            1 * setArgs(["/tmp/junit/bin/npm-cli.js", "install"])
            1 * setWorkingDir(Paths.get("/tmp/junit").toFile())
            1 * setEnvironment(!null) >> { args ->
                final Map<String, ?> env = args[0]
                final String pathName = System.getenv().containsKey("Path") ? "Path" : "PATH"
                assert env[pathName] == System.getenv().get(pathName) + File.pathSeparator +
                        "/tmp/junit-nodejs/bin${File.pathSeparator}/tmp/junit-npm/bin"
                assert env["NODE_HOME"] == "/tmp/junit-nodejs/bin"
            }
            0 * _                           // don't allow any other interaction
        }

    }

    def "executableData no system path"() {

        given:
        def data = new ExecutableData(Map.of())
                .setExecutable("npm")
                .setArgs(["-version"])
                .setWorkingDir(Paths.get("/tmp/junit").toFile())
                .withPathLocation("/tmp/junit-nodejs/bin")
                .withPathLocation("/tmp/junit-npm/bin")

        when:
        data.execute(spec)

        then:
        with(spec) {
            1 * setIgnoreExitValue(false)
            1 * setExecutable("npm")
            1 * setArgs(["-version"])
            1 * setWorkingDir(Paths.get("/tmp/junit").toFile())
            1 * setEnvironment(!null) >> { args ->
                final Map<String, ?> env = args[0]
                assert env["PATH"] == "/tmp/junit-nodejs/bin${File.pathSeparator}/tmp/junit-npm/bin"
            }
        }

    }

    def "executableData windows"() {

        given:
        def data = new ExecutableData(Map.of("Path", "c:\\Program Files"))
                .setExecutable("npm")
                .setArgs(["-version"])
                .setWorkingDir(Paths.get("/tmp/junit").toFile())
                .withPathLocation("/tmp/junit-nodejs/bin")
                .withPathLocation("/tmp/junit-npm/bin")

        when:
        data.execute(spec)

        then:
        with(spec) {
            1 * setIgnoreExitValue(false)
            1 * setExecutable("npm")
            1 * setArgs(["-version"])
            1 * setWorkingDir(Paths.get("/tmp/junit").toFile())
            1 * setEnvironment(!null) >> { args ->
                final Map<String, ?> env = args[0]
                assert env["Path"] == "c:\\Program Files${File.pathSeparator}/tmp/junit-nodejs/bin${File.pathSeparator}/tmp/junit-npm/bin"
            }
        }

    }

    def "Test default workingDir"() {

        when:
        def data = new ExecutableData()
                .setExecutable("node")
                .setArgs(["/tmp/junit/bin/npm-cli.js", "install"])
                .setDefaultWorkingDir(Paths.get("/tmp/junit").toFile())
        data.execute(spec)

        then:
        1 * spec.setWorkingDir(Paths.get("/tmp/junit").toFile())

    }

    def "Test default workingDir unused"() {

        when:
        def data = new ExecutableData()
                .setExecutable("node")
                .setWorkingDir(Paths.get("/tmp/junit").toFile())
                .setArgs(["/tmp/junit/bin/npm-cli.js", "install"])
                .setDefaultWorkingDir(Paths.get("/tmp/junit-default").toFile())
        data.execute(spec)

        then:
        1 * spec.setWorkingDir(Paths.get("/tmp/junit").toFile())

    }

}
