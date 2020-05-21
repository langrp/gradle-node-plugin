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

package com.palawan.gradle.tasks

import com.palawan.gradle.AbstractFuncTest
import com.palawan.gradle.NodePlugin
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Stepwise

import java.nio.file.Files

/**
 *
 * Tests to be implemented
 * - download node directly from packager task
 * - use non-default packager in no download mode
 * - use non-default packager in no download mode and execute default packager
 * - node install test - input/output files
 * - execution task ignore exit value
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
@Stepwise
class SystemNodeFuncTest extends AbstractFuncTest {

	def "No setup tasks"() {

		given:
		buildScript("")

		when:
		def result = run("tasks")

		then:
		result.task(":tasks").outcome == TaskOutcome.SUCCESS
		result.output =~ /nodeInstall - Install node packages using chosen packager\nnodeSetup - Prepares specific version of NodeJS./

	}

	def "node & npm version"() {

		given:
		buildScript("""
			node {
				download = false
			}
			
			def script = System.properties["script"] ? System.properties["script"] : "app.js"
			
			task npxVersion(type: NpxTask) {
				command = "-version"
			}
			
			task npmVersion(type: NpmTask) {
				command = "-version"
			}
			
			task nodeVersion(type: NodeTask) {
				args = ['--version']
			}
			
			task helloWorld(type: NodeTask) {
				args = [script]
				outputs.upToDateWhen { true }
			}
			
		""")

		and:
		writeFile("app.js", """
			console.log("Hello World")
		""")
		writeFile("version.js", """
			console.log(`\${process.version}`);
		""")

		when:
		def result = run("nodeVersion")

		then:
		result.task(":${NodePlugin.NODE_SETUP_TASK_NAME}") == null // did not run
		result.task(":nodeVersion").outcome == TaskOutcome.SUCCESS
		result.output =~ /\nv${NodePlugin.LTS_VERSION}\n/

		when:
		def result2 = run('npmVersion')

		then:
		result2.task(":${NodePlugin.NODE_SETUP_TASK_NAME}") == null // did not run
		result2.task(":npmVersion").outcome == TaskOutcome.SUCCESS
		result2.output =~ /\n6.14.4\n/

		when:
		def result3 = run('npxVersion')

		then:
		result3.task(":${NodePlugin.NODE_SETUP_TASK_NAME}") == null // did not run
		result3.task(":npxVersion").outcome == TaskOutcome.SUCCESS
		result3.output =~ /\n6.14.4\n/

		when:
		def result4 = run("helloWorld")

		then:
		result4.task(":helloWorld").outcome == TaskOutcome.SUCCESS
		result4.output =~ /Hello World/

		when:
		def result5 = run("helloWorld")

		then:
		result5.task(":helloWorld").outcome == TaskOutcome.UP_TO_DATE

		when:
		def result6 = run("helloWorld", "-Dscript=version.js")

		then:
		result6.task(":helloWorld").outcome == TaskOutcome.SUCCESS
		result6.output =~ /\nv${NodePlugin.LTS_VERSION}\n/

	}

	def "Execution parameter detection"() {

		given:
		writeFile("print-env.js", """
			const nodeHome = process.env.NODE_HOME;
			if (nodeHome) {
				console.log(`NODE_HOME=\${nodeHome}`);
			} else {
				console.log('Missing NODE_HOME');
			}
		""")

		and:
		buildScript("""

			def nodeHomeVar = System.properties["nodeHome"] ? System.properties["nodeHome"] : "${AbstractFuncTest.nodeJsDir.toString()}"

			task failure(type: NodeTask) {
				ignoreExitValue = true
				args = ["unknown.js"]
				outputs.upToDateWhen { true }
			}
			
			task nodeHome(type: NodeTask) {
				args = ["print-env.js"]
				environment = ['NODE_HOME': nodeHomeVar]
				outputs.upToDateWhen { true }
			}
		""")

		when:
		def result1 = run("failure")

		then:
		result1.task(":failure").outcome == TaskOutcome.SUCCESS
		result1.output =~ /Error: Cannot find module '${testProjectDir.resolve("unknown.js")}'/

		when:
		def result2 = run("failure")

		then:
		result2.task(":failure").outcome == TaskOutcome.UP_TO_DATE

		when:
		def result3 = run("nodeHome")

		then:
		result3.task(":nodeHome").outcome == TaskOutcome.SUCCESS
		result3.output =~ "NODE_HOME=${AbstractFuncTest.nodeJsDir.toString()}"

		when:
		def result4 = run("nodeHome")

		then:
		result4.task(":nodeHome").outcome == TaskOutcome.UP_TO_DATE

		when:
		def result5 = run("nodeHome", "-DnodeHome=${AbstractFuncTest.nodeJsDir.toString()}")

		then:
		result5.task(":nodeHome").outcome == TaskOutcome.UP_TO_DATE

		when:
		def result6 = run("nodeHome", "-DnodeHome=~/.nodejs")

		then:
		result6.task(":nodeHome").outcome == TaskOutcome.SUCCESS
		result6.output =~ "NODE_HOME=~/.nodejs"

	}

	// NPM

	def "npm input_outputs detection"() {

		given:
		writeFile("create-cwd.js", """
			var fs = require('fs');
			fs.writeFile('print-cwd.js', 'console.log(process.cwd())', 'utf8', function (err) {
			  if (err) throw err;
			  console.log('Saved!');
			});
		""")
		writeFile("package.json", """
			{
			  "name": "sample-node-project",
			  "version": "1.0.0",
			  "description": "Sample Node.js Project",
			  "main": "app.js",
			  "scripts": {
			  	"create-cwd": "node create-cwd.js",
			    "print-cwd": "node print-cwd.js",
			    "test": "echo \\"Error: no test specified\\" && exit 1"
			  }
			}
		""")

		and:
		Files.createDirectories(testProjectDir.resolve("subproject"))
		writeFile("subproject/print-cwd.js", "console.log(process.cwd())")
		writeFile("subproject/package.json", """
			{
			  "name": "subproject",
			  "scripts": {
			    "print-cwd": "node print-cwd.js"
			  }
			}
		""")

		and:
		buildScript("""

			task printSubProjectDir(type: NpmTask) {
				command = "run"
				args = ["print-cwd", "--prefix", "subproject"]
				inputs.file("subproject/print-cwd.js")
				outputs.upToDateWhen { true }
			}
			
			task printSubWorkingDir(type: NpmTask) {
				command = "run"
				args = ["print-cwd"]
				workingDir = file("subproject")
				inputs.file("subproject/print-cwd.js")
				outputs.upToDateWhen { true }
			}
			
			task createCwd(type: NpmTask) {
				command = "run"
				args = ["create-cwd"]
				inputs.file("create-cwd.js")
				outputs.file("create-cwd.js")
			}
			
			task printMainProjectDir(type: NpmTask) {
				command = "run"
				args = ["print-cwd"]
				inputs.file("print-cwd.js")
				outputs.upToDateWhen { true }
			}
			
			task nodeTest(type: NpmTask) {
				command = "run"
				args = ["test"]
			}

		""")

		when:
		def result1 = run("printSubProjectDir")

		then:
		result1.task(":printSubProjectDir").outcome == TaskOutcome.SUCCESS
		result1.output =~ testProjectDir.resolve("subproject").toString()

		when:
		def result2 = run("printSubProjectDir")

		then:
		result2.task(":printSubProjectDir").outcome == TaskOutcome.UP_TO_DATE

		when:
		def result3 = run("printSubWorkingDir")

		then:
		result3.task(":printSubWorkingDir").outcome == TaskOutcome.SUCCESS
		result3.output =~ testProjectDir.resolve("subproject").toString()

		when:
		def result4 = run("printSubWorkingDir")

		then:
		result4.task(":printSubWorkingDir").outcome == TaskOutcome.UP_TO_DATE

		when:
		def result5 = runAndFail("printMainProjectDir")

		then:
		result5.task(":printMainProjectDir").outcome == TaskOutcome.FAILED

		when:
		def result6 = run("createCwd")

		then:
		result6.task(":createCwd").outcome == TaskOutcome.SUCCESS

		when:
		def result7 = run("createCwd")

		then:
		result7.task(":createCwd").outcome == TaskOutcome.UP_TO_DATE

		when:
		def result8 = run("printMainProjectDir")

		then:
		result8.task(":printMainProjectDir").outcome == TaskOutcome.SUCCESS
		result8.output =~ testProjectDir.toString()

		when:
		def result9 = runAndFail("nodeTest")

		then:
		result9.task(":nodeTest").outcome == TaskOutcome.FAILED

	}

	// NodeInstall
	// Sample app from https://github.com/mochajs/mocha-examples/tree/master/packages/babel
	def "nodeInstall IO detection"() {

		given:
		buildScript("""

			task nodeCompile(type: NpmTask) {
				dependsOn(nodeInstall)
				command = "run"
				args = ["compile"]
				inputs.files(fileTree("src"))
				outputs.dir("lib")
			}

			task nodeTest(type: NpmTask) {
				dependsOn(nodeCompile)
				command = "run"
				args = ["test"]
				inputs.dir("lib")
				outputs.files(fileTree("mochawesome-report"))
			}

		""")

		and:
		Files.createDirectories(testProjectDir.resolve("src"))
		Files.createDirectories(testProjectDir.resolve("test"))
		writeFile("src/index.js", "export { default as add } from './add';")
		writeFile("src/add.js", """
			export default (a, b) => {
			  return a + b;
			}
		""")
		writeFile("test/index.spec.js", """
			import { equal } from "assert";
			import { add } from "../src";
			
			describe('Babel usage suite', () => {
			  it('should add numbers correctly', () => {
				equal(add(2, 3), 5);
			  });
			});
		""")
		writeFile("babel.config.js", """
		module.exports = (api) => {
		  // Cache configuration is a required option
		  api.cache(false);
		
		  const presets = [
			[
			  "@babel/preset-env", 
			  { 
				useBuiltIns: false
			  }
			]
		  ];
		
		  return { presets };
		};
		""")
		writeFile("package.json", """
			{
			  "name": "example-babel-app",
			  "version": "1.0.0",
			  "description": "Babel example",
			  "main": "lib/index.js",
			  "scripts": {
				"compile": "babel src -d lib",
				"prepublish": "npm run compile",
				"test": "mocha --require @babel/register --reporter mochawesome"
			  },
			  "directories": {
				"lib": "./src",
				"test": "./test"
			  },
			  "devDependencies": {
				"@babel/cli": "^7.8.4",
				"@babel/core": "^7.9.0",
				"@babel/preset-env": "^7.3.4",
				"@babel/register": "^7.9.0",
				"mocha": "^7.1.1",
				"mochawesome": "^6.1.1"
			  },
			  "engines": {
				"node": ">=10.0.0"
			  },
			  "license": "ISC"
			}
		""")

		when:
		def result1 = run("nodeInstall")

		then:
		result1.task(":nodeSetup") == null
		result1.task(":nodeInstall").outcome == TaskOutcome.SUCCESS
		Files.exists(testProjectDir.resolve("package-lock.json"))

		when:
		def result2 = run("nodeInstall")

		then:
		result2.task(":nodeSetup") == null
		result2.task(":nodeInstall").outcome == TaskOutcome.SUCCESS

		when:
		def result3 = run("nodeInstall")

		then:
		result3.task(":nodeSetup") == null
		result3.task(":nodeInstall").outcome == TaskOutcome.UP_TO_DATE

		when:
		def result4 = run("nodeTest")

		then:
		result4.task(":nodeInstall").outcome == TaskOutcome.UP_TO_DATE
		result4.task(":nodeCompile").outcome == TaskOutcome.SUCCESS
		result4.task(":nodeTest").outcome == TaskOutcome.SUCCESS
		result4.output =~ "should add numbers correctly"

		when:
		def result5 = run("nodeTest")

		then:
		result5.task(":nodeInstall").outcome == TaskOutcome.UP_TO_DATE
		result5.task(":nodeCompile").outcome == TaskOutcome.UP_TO_DATE
		result5.task(":nodeTest").outcome == TaskOutcome.UP_TO_DATE

	}

	// Npx
	def "npx input/output detection"() {

		given:
		buildScript("""
		
			task cowsay(type: NpxTask) {
				command = "cowsay"
				args = ["Hello World"]
				outputs.upToDateWhen { true }
			}
		
		""")

		when:
		def result1 = run("cowsay")

		then:
		result1.task(":cowsay").outcome == TaskOutcome.SUCCESS
		result1.output =~ '< Hello World >'

		when:
		def result2 = run("cowsay")

		then:
		result2.task(":cowsay").outcome == TaskOutcome.UP_TO_DATE

	}

	// Unknown packagers
	def "Unknown packager yarn"() {

		given:
		buildScript("""
			task yarnTask(type: YarnTask) {
			
			}
		""")

		when:
		def result = runAndFail("yarnTask")

		then:
		result.task("yarnTask") == null

	}

	def "Missing packager yarn"() {

		given:
		buildScript("""
			node {
				yarn { }
			}
			
			task npmVersion(type: NpmTask) {
				command = "-version"
			}
			
			task yarnVersion(type: YarnTask) {
				command = "-version"
			}
		""")

		when:
		def result1 = runAndFail("yarnVersion")

		then:
		result1.task(":yarnVersion").outcome == TaskOutcome.FAILED

		when:
		def result2 = run("npmVersion")

		then:
		result2.task(":npmVersion").outcome == TaskOutcome.SUCCESS
		result2.output =~ NodePlugin.LTS_NPM_VERSION

	}

	def "Missing packager pnpm"() {

		given:
		buildScript("""
			node {
				pnpm { }
			}
			
			task npmVersion(type: NpmTask) {
				command = "-version"
			}
			
			task pnpxVersion(type: PnpxTask) {
				command = "--version"
			}
		""")

		when:
		def result1 = runAndFail("pnpxVersion")

		then:
		result1.task(":pnpxVersion").outcome == TaskOutcome.FAILED

		when:
		def result2 = run("npmVersion")

		then:
		result2.task(":npmVersion").outcome == TaskOutcome.SUCCESS
		result2.output =~ NodePlugin.LTS_NPM_VERSION

	}

	def "Multiple packagers"() {

		given:
		buildScript("""
			node {
				yarn {}
				npm {}
			}
		""")

		when:
		def result = runAndFail("yarnTask")

		then:
		result.task("yarnTask") == null
		result.output =~ "Multiple packagers defined. Please configure single packager!"

	}

}
