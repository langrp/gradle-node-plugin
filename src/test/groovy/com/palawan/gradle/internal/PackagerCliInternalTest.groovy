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
import com.palawan.gradle.internal.data.PackagerCliData

import java.nio.file.Paths

/**
 *
 * @author petr.langr* @since 1.0.0
 */
class PackagerCliInternalTest extends AbstractProjectTest {

    PackagerInternal parent
    PackagerCliInternal cli

    void setup() {
        parent = PackagerInternal.npm(nodeExtension.nodeManager)
                .workingDir(Paths.get("/tmp/junit"))
    }

    def "Verify data"() {

        given:
        mockLinux()

        and:
        cli = new PackagerCliInternal(new PackagerCliData().setCommand("npx").setLocalScript("npx-cli.js"), parent)

        when:
        def command = cli.getCommand()
        def script = cli.getScriptFile()

        then:
        cli.name == "npx"
        command == "npx"
        script.get() == testProjectDir.resolve(".gradle/nodejs/node-v12.16.3-linux-x64/lib/node_modules/npm/npx-cli.js")

    }

    def "getScriptFile no manager"() {

        given:
        cli = new PackagerCliInternal(new PackagerCliData(), PackagerInternal.pnpm())

        when:
        cli.getScriptFile()

        then:
        thrown(NullPointerException.class)

    }

    def "getExecutableBinDir"() {

        given:
        cli = new PackagerCliInternal(new PackagerCliData().setCommand("npx").setLocalScript("npx-cli.js"), parent)

        when:
        def bin = cli.getExecutableBinDir()

        then:
        bin == Paths.get("/tmp/junit/npm-latest/bin")

    }

    def "executableData"() {

        given:
        cli = new PackagerCliInternal(new PackagerCliData().setCommand("pnpx").setLocalScript("pnpx.js"), parent)

        when:
        def data = cli.executableData(["--version"])

        then:
        data.executable == "pnpx"
        data.args == ["--version"]
        data.workingDir == null
        data.environmentVariables.isEmpty()
        data.path.isEmpty()
        !data.ignoreExitValue

    }

}