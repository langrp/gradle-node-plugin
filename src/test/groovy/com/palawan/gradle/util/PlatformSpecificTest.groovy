package com.palawan.gradle.util

import org.gradle.api.GradleException
import spock.lang.Specification

import java.nio.file.Paths

class PlatformSpecificTest extends Specification {

	def "GetOsName"(String osNameProp, String expected) {
		given:
		def subject = getSubject(Map.of(PlatformSpecific.OS_NAME, osNameProp))

		when:
		def osName = subject.getOsName()

		then:
		osName == expected

		where:
		osNameProp 		| expected
		'Windows 7 ' 	| 'win'
		'Windows 8'		| 'win'
		'Windows NT'	| 'win'
		'Windows 98'	| 'win'
		'Windows XP'	| 'win'
		'Windows 2000'	| 'win'
		'Windows ME'	| 'win'
		'Windows 2003'	| 'win'
		'Windows 8.1'	| 'win'
		'Windows 10'	| 'win'
		'Linux'			| 'linux'
		'SunOS'			| 'sunos'
		'FreeBSD'		| 'linux'
		'Mac OS X'		| 'darwin'
	}

	def "GetOsName unknown"() {
		given:
		def subject = getSubject(Map.of(PlatformSpecific.OS_NAME, 'random'))

		when:
		subject.getOsName()

		then:
		def e = thrown(IllegalStateException)
		e.message == 'Unsupported OS random'
	}

	def "GetOsArch"(String osArchProp, String expected) {
		given:
		def subject = getSubject(Map.of(PlatformSpecific.OS_ARCH, osArchProp))

		when:
		def osArch = subject.getOsArch()

		then:
		osArch == expected

		where:
		osArchProp 		| expected
		'x86'			| 'x86'
		'i386'			| 'x86'
		'ppc'			| 'x86'
		'sparc'			| 'x86'
		'amd64'			| 'x64'
		'x86_64'		| 'x64'
	}

	def "GetOsArch arm"(String osArchProp, String uname, String expected) {
		given:
		def executor = Mock(ProcessExecutor)
		def subject = getSubject(Map.of(PlatformSpecific.OS_ARCH, osArchProp), executor)
		executor.execute("uname", "-m") >> uname

		when:
		def osArch = subject.getOsArch()

		then:
		osArch == expected

		where:
		osArchProp 		| uname 	| expected
		'arm'			| 'arm64' 	| 'arm64'
		'aarch'			| 'armv8l' 	| 'arm64'
	}

	def "GetOsArch failure"(String osArchProp, Exception exception) {
		given:
		def executor = Mock(ProcessExecutor)
		def subject = getSubject(Map.of(PlatformSpecific.OS_ARCH, osArchProp), executor)
		executor.execute("uname", "-m") >> { throw exception }

		when:
		subject.getOsArch()

		then:
		thrown(GradleException)

		where:
		osArchProp 		| exception
		'arm'			| new InterruptedException()
		'aarch'			| new IOException()
	}

	def "IsWindows"(String osName, boolean expected) {
		given:
		def subject = getSubject(Map.of(PlatformSpecific.OS_NAME, osName))

		when:
		def isWindows = subject.isWindows()

		then:
		isWindows == expected

		where:
		osName			| expected
		'Windows 10' 	| true
		'Linux'			| false
		'Mac OS X'		| false
	}

	def "GetCommand"(String osName, String command, String expected) {
		given:
		def subject = getSubject(Map.of(PlatformSpecific.OS_NAME, osName))

		when:
		def result = subject.getCommand(command)

		then:
		result == expected

		where:
		osName			| command 	| expected
		'Windows 10' 	| 'yarn'	| 'yarn.cmd'
		'Linux'			| 'npx'		| 'npx'
		'Mac OS X'		| 'npm'		| 'npm'
	}

	def "GetExecutable"(String osName, String exec, String expected) {
		given:
		def subject = getSubject(Map.of(PlatformSpecific.OS_NAME, osName))

		when:
		def result = subject.getExecutable(exec)

		then:
		result == expected

		where:
		osName			| exec 		| expected
		'Windows 10' 	| 'node'	| 'node.exe'
		'Linux'			| 'npx'		| 'npx'
		'Mac OS X'		| 'npm'		| 'npm'
	}

	def "GetBinPath"(String osName, String path, String expected) {
		given:
		def subject = getSubject(Map.of(PlatformSpecific.OS_NAME, osName))

		when:
		def result = subject.getBinPath(Paths.get(path))

		then:
		result == Paths.get(expected)

		where:
		osName			| path 			| expected
		'Windows 10' 	| './nodejs'	| './nodejs'
		'Linux'			| './nodejs'	| './nodejs/bin'
		'Mac OS X'		| './nodejs'	| './nodejs/bin'
	}

	private getSubject(Map<String, String> properties) {
		ProcessExecutor executor = Mock()
		getSubject(properties, executor)
	}

	private static getSubject(Map<String, String> properties, ProcessExecutor processExecutor) {
		PlatformSpecific.getInstance(createProperties(properties), processExecutor)
	}

	private static Properties createProperties(Map<String, String> properties) {
		def props = new Properties()
		props.putAll(properties)
		props
	}
}
