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

package com.palawan.gradle.internal.data

import spock.lang.Specification

import java.nio.file.Paths

/**
 *
 * @author petr.langr* @since 1.0.0
 */
class PackagerDataTest extends Specification {

    def "Test packager data"() {

        when:
        def data = new PackagerData()
                .setCommand("npm")
                .setVersion("6.14.2")
                .setCliCommand("npx")
                .setNpmPackage("npm")
                .setWorkingDir(Paths.get("/tmp/junit"))
                .setLocalScript("bin/npm-cli.js")
                .setInputFiles(["package.json"])
                .addInputFile("package-lock.json")
                .setOutputDirectories(["node_modules"])
                .addOutputDirectory(".npm")
                .setOutputFiles(["package-lock.json"])
                .addOutputFile("npm.lock")

        then:
        data.command == "npm"
        data.version.get() == "6.14.2"
        data.npmPackage == "npm"
        data.workingDir == new File("/tmp/junit")
        data.localScript.get() == "bin/npm-cli.js"
        data.inputFiles == ["package.json", "package-lock.json"]
        data.outputDirectories == ["node_modules", ".npm"]
        data.outputFiles == ["package-lock.json", "npm.lock"]
        data.cliCommand.get() == "npx"

    }

    def "Cli"() {

        when:
        def data = new PackagerData()
                .setWorkingDir(Paths.get("/tmp/junit").toFile())
        data.cli { c -> c.setCommand("npx").setLocalScript("bin/npx-cli.js") }

        then:
        data.workingDir == new File("/tmp/junit")
        data.getCli().isPresent()
        data.getCli().get().command == "npx"
        data.getCli().get().localScript.get() == "bin/npx-cli.js"

    }
}
