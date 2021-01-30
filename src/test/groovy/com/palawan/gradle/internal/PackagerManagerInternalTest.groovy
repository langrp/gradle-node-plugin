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

package com.palawan.gradle.internal

import com.palawan.gradle.AbstractProjectTest
/**
 *
 * @author petr.langr* @since 1.0.0
 */
class PackagerManagerInternalTest extends AbstractProjectTest {

    PackagerManagerInternal manager

    void setup() {

        manager = new PackagerManagerInternal(project, testProjectDir.resolve(".gradle"))

    }

    def "Npm"() {

        when:
        manager.npm{ }

        then:
        def packager = manager.packager.get()
        packager.name == "npm"
        packager.command == "npm"
        packager.cli.get().name == "npx"

    }

    def "Pnpm"() {

        when:
        manager.pnpm{ }

        then:
        def packager = manager.packager.get()
        packager.name == "pnpm"
        packager.command == "pnpm"
        packager.cli.get().name == "pnpx"

    }

    def "Cnpm"() {

        when:
        manager.cnpm{ }

        then:
        def packager = manager.packager.get()
        packager.name == "cnpm"
        packager.command == "cnpm"
        packager.cli.isEmpty()

    }

    def "Yarn"() {

        when:
        manager.yarn{ }

        then:
        def packager = manager.packager.get()
        packager.name == "yarn"
        packager.command == "yarn"
        packager.cli.isEmpty()

    }

    def "Custom"() {

        when:
        manager.custom {
            it.command = "npm"
            it.npmPackage = "npm"
            it.version = "1.0.0"
            it.cli { c ->
                c.command = "npx"
            }
        }

        then:
        def packager = manager.packager.get()
        packager.name == "custom"
        packager.command == "npm"
        packager.version == "1.0.0"
        packager.cli.get().command == "npx"

    }

    def "Multiple packagers"() {

        given:
        manager.npm {}

        when:
        manager.pnpm {}

        then:
        thrown(NodeException.class)

    }

    def "AfterEvaluate"() {

        given:
        manager.npm{}

        when:
        manager.afterEvaluate(project, nodeExtension)

        then:
        manager.packager.isPresent()

    }

    def "AfterEvaluate no packager"() {

        when:
        manager.afterEvaluate(project, nodeExtension)

        then:
        manager.packager.isEmpty()

    }

}
