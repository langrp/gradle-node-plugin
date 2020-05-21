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

package com.palawan.gradle.dsl

import com.palawan.gradle.AbstractProjectTest
/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
class NodeExtensionTest extends AbstractProjectTest {

    def "Get"() {

        when:
        def node = NodeExtension.get(project)

        then:
        node != null

    }

    def "Validate default values"() {

        when:
        def node = new NodeExtension(project)

        then:
        node.getNodeManager() != null
        node.getPackagerManager() != null
        !node.getDownload()
        node.getCommand() == "node"
        node.getVersion() == "12.16.3"
        node.getWorkingDir() == testProjectDir.resolve(".gradle/nodejs")
        node.getUrl() == "https://nodejs.org/dist"

    }

    def "Validate value update"() {

        when:
        def node = new NodeExtension(project)
        node.setDownload(true)
        then:
        node.getDownload()

        when:
        node.setCommand("nodejs")
        then:
        node.getCommand() == "nodejs"

        when:
        node.setVersion("14.2.0")
        then:
        node.getVersion() == "14.2.0"

        when:
        node.setWorkingDir(testProjectDir.resolve(".gradle/node").toFile())
        then:
        node.getWorkingDir() == testProjectDir.resolve(".gradle/node")

        when:
        node.setUrl("https://company.org/dist")
        then:
        node.getUrl() == "https://company.org/dist"

    }

    def "Npm"() {

        when:
        def node = new NodeExtension(project)
        node.npm{ }

        then:
        node.getPackagerManager().getPackager().isPresent()
        node.getPackagerManager().getPackager().map{ p -> p.getNpmPackage() }.orElseThrow() == "npm"

    }

    def "Pnpm"() {

        when:
        def node = new NodeExtension(project)
        node.pnpm{ }

        then:
        node.getPackagerManager().getPackager().isPresent()
        node.getPackagerManager().getPackager().map{ p -> p.getNpmPackage() }.orElseThrow() == "pnpm"

    }

    def "Cnpm"() {

        when:
        def node = new NodeExtension(project)
        node.cnpm{ }

        then:
        node.getPackagerManager().getPackager().isPresent()
        node.getPackagerManager().getPackager().map{ p -> p.getNpmPackage() }.orElseThrow() == "cnpm"

    }

    def "Yarn"() {

        when:
        def node = new NodeExtension(project)
        node.yarn{ }

        then:
        node.getPackagerManager().getPackager().isPresent()
        node.getPackagerManager().getPackager().map{ p -> p.getNpmPackage() }.orElseThrow() == "yarn"

    }

    def "Custom"() {

        when:
        def node = new NodeExtension(project)
        node.custom{ p -> p.npmPackage = "custom-npm" }

        then:
        node.getPackagerManager().getPackager().isPresent()
        node.getPackagerManager().getPackager().map{ p -> p.getNpmPackage() }.orElseThrow() == "custom-npm"

    }
}
