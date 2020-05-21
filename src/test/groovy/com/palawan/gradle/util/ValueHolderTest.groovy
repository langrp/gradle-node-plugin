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

package com.palawan.gradle.util

import spock.lang.Specification
/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
class ValueHolderTest extends Specification {

    def "Racy"() {

        given:
        def holder = ValueHolder.racy{ "Hello World" }

        when:
        def value = holder.get()

        then:
        value == "Hello World"

    }

    def "DoubleChecked"() {

        given:
        def holder = ValueHolder.doubleChecked{ "Hello World" }

        when:
        def value = holder.get()

        then:
        value == "Hello World"

    }

    //https://stackoverflow.com/questions/45821212/test-the-double-check-locking-mechanism-in-singleton-for-100-code-coverage

}
