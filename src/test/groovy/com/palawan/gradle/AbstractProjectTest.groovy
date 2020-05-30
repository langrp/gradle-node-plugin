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

package com.palawan.gradle

import com.palawan.gradle.dsl.NodeExtension
import com.palawan.gradle.util.PlatformSpecific
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
class AbstractProjectTest extends Specification {

    Path testProjectDir
    ProjectInternal project
    NodeExtension nodeExtension
    PlatformSpecific platformSpecific


    void setup() {
        testProjectDir = Files.createTempDirectory("junit")
        platformSpecific = Mock()
        project = (ProjectInternal) ProjectBuilder.builder()
                .withProjectDir(testProjectDir.toFile())
                .build()
        nodeExtension = project.getExtensions().create(
                NodePlugin.EXTENSION_NAME,
                NodeExtension.class,
                project,
                platformSpecific)

    }

    void cleanup() {
        if (testProjectDir != null) {
            testProjectDir.toFile().deleteDir()
        }
    }

    void mockLinux() {
        platformSpecific.getOsName() >> "linux"
        platformSpecific.getOsArch() >> "x64"
        platformSpecific.isWindows() >> false

        platformSpecific.getExecutable(!null) >> { args -> args[0]}
        platformSpecific.getCommand(!null) >> { args -> args[0]}
    }

    void mockWindows() {
        platformSpecific.getOsName() >> "win"
        platformSpecific.getOsArch() >> "x64"
        platformSpecific.isWindows() >> true
        platformSpecific.getExecutable(!null) >> { args -> args[0] + ".exe" }
        platformSpecific.getCommand(!null) >> { args -> args[0] + ".cmd" }
    }

    static void set(Class<?> type, Object target, String fieldName, Object value) {
        Field field = getField(type, fieldName)

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.setAccessible(true)
        field.set(target, value)
    }

    static Object get(Object target, String fieldName) {
        Field field = getField(target.getClass(), fieldName)
        field.setAccessible(true)
        return field.get(target)
    }

    private static Field getField(Class<?> type, String fieldName) {
        while(type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getName() == fieldName) {
                    return field
                }
            }
            type = type.getSuperclass()
        }
        throw new NoSuchFieldException(fieldName)
    }

}
