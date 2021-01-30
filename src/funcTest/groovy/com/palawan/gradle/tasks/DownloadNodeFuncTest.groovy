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

package com.palawan.gradle.tasks

import com.palawan.gradle.AbstractFuncTest
import org.gradle.testkit.runner.TaskOutcome

/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
class DownloadNodeFuncTest extends AbstractFuncTest {

    def "Download NodeJS"() {

        given:
        buildScript("""
            node {
                download = true
                version = "14.3.0"
                workingDir = file("build/nodejs")
            }
            
            task nodeVersion(type: NodeTask) {
                args = ["--version"]
            }
            
            task npmVersion(type: NpmTask) {
				command = "-version"
			}
            
        """)

        when:
        def result1 = run("nodeVersion")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":nodeVersion").outcome == TaskOutcome.SUCCESS
        result1.output =~ "v14.3.0"

        when:
        def result2 = run("npmVersion")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":npmSetup") == null
        result2.task(":npmVersion").outcome == TaskOutcome.SUCCESS
        result2.output =~ "6.14.5"

        when:
        buildScript("""
            node {
                download = true
                version = "14.3.0"
                workingDir = file("tools/nodejs")
            }
            
            task nodeVersion(type: NodeTask) {
                args = ["--version"]
            }
            
            task npmVersion(type: NpmTask) {
				command = "-version"
			}
            
        """)
        def result3 = run("nodeVersion")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result3.task(":nodeVersion").outcome == TaskOutcome.SUCCESS
        result3.output =~ "v14.3.0"

        when:
        def result4 = run("npmVersion")

        then:
        result4.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result4.task(":npmSetup") == null
        result4.task(":npmVersion").outcome == TaskOutcome.SUCCESS
        result4.output =~ "6.14.5"

    }

    def "Download npm"() {

        given:
        buildScript("""
            node {
                download = true
                workingDir = file("build/nodejs")

                npm {
                    version = System.properties["npm_version"] ? System.properties["npm_version"] : "6.13.7"
                    workingDir = file("build/npm")
                }
            }
            
            task npmVersion(type: NpmTask) {
				command = "-version"
			}
			
			task npxVersion(type: NpxTask) {
			    command = "-version"
			}
            
        """)

        when:
        def result1 = run("npmVersion")

        then:
        result1.task(":nodeSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":npmSetup").outcome == TaskOutcome.SUCCESS
        result1.task(":npmVersion").outcome == TaskOutcome.SUCCESS
        result1.output =~ "6.13.7"

        when:
        def result2 = run("npxVersion")

        then:
        result2.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":npmSetup").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":npxVersion").outcome == TaskOutcome.SUCCESS
        result2.output =~ "6.13.7"

        when:
        def result3 = run("npmVersion", "-Dnpm_version=6.14.15")

        then:
        result3.task(":nodeSetup").outcome == TaskOutcome.UP_TO_DATE
        result3.task(":npmSetup").outcome == TaskOutcome.SUCCESS
        result3.task(":npmVersion").outcome == TaskOutcome.SUCCESS
        result3.output =~ "6.14.15"

    }

}
