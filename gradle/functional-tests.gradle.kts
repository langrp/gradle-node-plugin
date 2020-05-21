/*
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
 */
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.internal.tasks.DefaultGroovySourceSet

apply<JacocoPlugin>()
apply<GroovyPlugin>()

configure<SourceSetContainer> {
	val main by getting
	create("functionalTest") {
		val groovySource = DslObject(this).getConvention().getPlugins().get("groovy")
		val groovy = (groovySource as DefaultGroovySourceSet).groovy
		groovy.srcDir("src/funcTest/groovy")
		resources.srcDir("src/funcTest/resources")
		compileClasspath += main.output + configurations["testRuntime"]
		runtimeClasspath += output + compileClasspath
	}
}


configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeClasspath"].extendsFrom(configurations["testRuntimeClasspath"])

val functionalSources = the<SourceSetContainer>()["functionalTest"]
val functionalTest = task<Test>("functionalTest") {
	description = "Runs the functional tests."
	group = "verification"

	testClassesDirs = functionalSources.output.classesDirs
	classpath = functionalSources.runtimeClasspath
	shouldRunAfter("test")
}

configure<JacocoPluginExtension> { toolVersion = "0.8.4" }

val jacocoTestReport = project.tasks.named<JacocoReport>("jacocoTestReport") {
	executionData(functionalTest)
	reports {
		xml.isEnabled = false
		csv.isEnabled = false
		html.destination = file("${buildDir}/reports/jacocoHtml")
	}
}

val mainSources = the<SourceSetContainer>()["main"]
val jacocoTestCoverageVerification = project.tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
	executionData(functionalTest)
	violationRules {
		rule {
			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = java.math.BigDecimal.ONE
			}
		}
	}
	classDirectories.setFrom(
			mainSources.output.asFileTree.matching {
				// exclude main()
				exclude("com/palawan/gradle/util/DoubleChecked.class")
			}
	)
}

// Tasks order
project.tasks.named("test") {
	finalizedBy(jacocoTestReport)
}

project.tasks.named("check") {
	dependsOn(functionalTest)
	dependsOn(jacocoTestCoverageVerification)
}
