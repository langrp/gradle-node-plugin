/*
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
 */

apply<MavenPublishPlugin>()

val sourceSets = the<SourceSetContainer>()
val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets["main"].allSource)
    classifier = "sources"
}

val javadocJar by tasks.registering(Jar::class) {
    from(project.tasks.named<Javadoc>("javadoc"))
    classifier = "javadoc"
}

project.artifacts {
    add("archives", sourcesJar)
    add("archives", javadocJar)
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Publishing

afterEvaluate {
    tasks.named<Jar>("jar") {
        manifest {
            attributes["Implementation-Version"] = project. version
            attributes["Created-By"] = "JDK ${System.getProperty("java.version")} (${System.getProperty("java.specification.vendor")})"
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Vendor-Id"] = project.group
            attributes["Implementation-Vendor"] = "Petr Langr"
            attributes["Implementation-URL"] = "https://github.com/langrp"
            attributes["Automatic-Module-Name"] = project.name.replace("-", ".")
        }
    }

    configure<PublishingExtension> {

        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
                artifact(sourcesJar.get())
                artifact(javadocJar.get())

                with(pom) {
                    name.set(project.name)
                    description.set(project.description)
                    url.set("https://github.com/langrp")

                    licenses {
                        license {
                            name.set("The MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("langrp")
                            name.set("Petr Langr")
                            organizationUrl.set("https://github.com/langrp")
                        }
                    }
                    scm {
                        url.set("https://github.com/langrp/${rootProject.name}")
                        connection.set("scm:git:git://github.com/langrp/${rootProject.name}.git")
                        developerConnection.set("scm:git:git://github.com/langrp/${rootProject.name}.git")
                    }
                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("https://github.com/langrp/${rootProject.name}/issues")
                    }

                }

            }
        }

    }
}

