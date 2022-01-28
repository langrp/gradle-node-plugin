
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

/**
 * execute node command as
 * `./gradlew :node --script npm-cli.js`
 */
tasks.register<com.palawan.gradle.tasks.NodeTask>("node")

/**
 * execute npm command as
 * `./gradlew :npm --cmd install --args="@angular/cli"`
 */
tasks.register<com.palawan.gradle.tasks.DefaultPackagerTask>("npm")

/**
 * execute npx command as
 * `./gradlew :npx --cmd cowsay --args="hello"`
 */
tasks.register<com.palawan.gradle.tasks.DefaultPackagerCliTask>("npx")

/**
 * execute pnpm command as
 * `./gradlew :pnpm --cmd install --args="@angular/cli"`
 */
tasks.register<com.palawan.gradle.tasks.PackagerTask>("pnpm")

/**
 * execute pnpx command as
 * `./gradlew :pnpx --cmd cowsay --args="hello"`
 */
tasks.register<com.palawan.gradle.tasks.PackagerCliTask>("pnpx")
