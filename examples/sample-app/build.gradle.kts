
plugins {
	id("com.palawanframe.node")
}

node {
	download = true
	workingDir = rootProject.file(".gradle/nodejs")

	custom {
		command = "pnpm"
		workingDir = rootProject.file(".gradle/pnpm")
		npmPackage = "pnpm"
		inputFiles = listOf("package.json", "pnpm-lock.yaml")
		outputFiles = listOf("pnpm-lock.yaml")
		outputDirectories = listOf("node_modules")
		cli {
			command = "pnpx"
		}
	}
}

tasks.named<com.palawan.gradle.tasks.NodeInstallTask>("nodeInstall") {
	command = "install"
}

tasks.register<com.palawan.gradle.tasks.NodeTask>("nodeVersion") {
	args = listOf("--version")
}

tasks.register<com.palawan.gradle.tasks.DefaultPackagerTask>("npmVersion") {
	command = "-version"
}

tasks.register<com.palawan.gradle.tasks.DefaultPackagerCliTask>("npxVersion") {
	command = "-version"
}

tasks.register<com.palawan.gradle.tasks.PackagerTask>("customVersion") {
	command = "--version"
}
