# Gradle Node Plugin

[![Build Status](https://github.com/langrp/gradle-node-plugin/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/langrp/gradle-node-plugin/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/langrp/gradle-node-plugin.svg)](https://opensource.org/licenses/MIT)
![Version](https://img.shields.io/badge/Version-0.2.1-orange.svg)

This plugin enables you to run build of your frontend using NodeJs technologies via gradle build system.
The plugin supports multiple node-js packager managers:
* npm
* yarn
* pnpm
* cnpm

To start using the plugin add this into your `build.gradle` file.
```groovy
plugins {
    id "com.palawanframe.node" version "0.2.1"
}
```

By default, the plugin tries to execute NodeJs available on PATH. One may configure the plugin to download preferred
version of NodeJs and execute build using downloaded version.

## Configuring Plugin
There are two major ways how plugin can be used:
1. With NodeJs available on PATH
2. With downloaded version of NodeJs or package manager

Below are configuration parameters and its default values.
```groovy
node {
    // Use downloaded NodeJs
    download = false
    // NodeJs version to be downloaded (if download=true)
    version = "16.13.2"
    // Node execution command (to support system aliases)
    command = "node"
    // Directory where nodejs will be downloaded
    workingDir = file(".gradle/nodejs")
    // URL to nodejs repository
    url = "https://nodejs.org/dist"
    
    // Below are different package managers that can be used. Only one can be configured.
    // Default uses NPM package manager available with NodeJs installation
    
    // Configures NPM packager for dependencies
    npm {
        // Npm version to be downloaded. If not defined uses latest
        version = "8.1.2"
        // NPM execution command
        command = "npm"
        // NPX execution command
        cliCommand = "npx"
        // Directory where NPM will be downloaded
        workingDir = file(".gradle/npm")
    }
    
    // Configures Yarn packager for dependencies
    yarn {
        version = "1.22.17"
        command = "yarn"
        workingDir = file(".gradle/yarn")
    }

	// Configures PNPM packager for dependencies
    pnpm {
        version = "6.27.1"
        command = "pnpm"
        cliCommand = "pnpx"
        workingDir = file(".gradle/pnpm")
    }

	// Configures CNPM packager for dependencies
    cnpm {
        version = "7.1.0"
        command = "cnpm"
        workingDir = file(".gradle/cnpm")
    }

	// Configures Custom packager for dependencies
    custom {
        version = "1.0.0"
        command = "myNpm"
        workingDir = file(".gradle/custom")
        // Package manager name needs to be defined in order to download it from repository
        npmPackage = "myNpm"
        
        // Define input/outputs to realize task only when needed
        inputFiles = [ "package.json" ]
        outputFiles = [ "package-lock.json" ]
        outputDirectories = [ "node_modules" ]

        cli {
            command = "myNpx"
        }
    }
}
```

## Gradle Tasks

