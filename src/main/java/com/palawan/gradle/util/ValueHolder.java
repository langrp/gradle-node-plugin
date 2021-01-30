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

package com.palawan.gradle.util;

import java.util.function.Supplier;

/**
 * Lazy loaded value holder
 *
 * @author petr.langr
 * @since 1.0.0
 */
public interface ValueHolder<T> extends Supplier<T> {

	/**
	 * Creates lazy loaded value holder with racing creation.
	 * @param supplier Value supplier triggered on first lazy load
	 * @param <T> Value type
	 * @return Value holder which initiating can cause race condition
	 */
	static <T> ValueHolder<T> racy(Supplier<T> supplier) {
		return new RacyHolder<>(supplier);
	}

	/**
	 * Thread safe (double checked) value holder. Use this to prevent multiple
	 * threads initiating value.
	 * @param supplier Value supplier triggered on first lazy load
	 * @param <T> Value type
	 * @return Thread safe lazy value holder
	 */
	static <T> ValueHolder<T> doubleChecked(Supplier<T> supplier) {
		return new DoubleChecked<>(supplier);
	}

}

class RacyHolder<T> implements ValueHolder<T>  {

	private T value;
	private final Supplier<T> supplier;

	public RacyHolder(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	/**
	 * Gets a result.
	 *
	 * @return a result
	 */
	@Override
	public T get() {
		T local = value;
		if (local == null) {
			local = value = supplier.get();
		}
		return local;
	}
}

class DoubleChecked<T> implements ValueHolder<T> {

	private T value;
	private final Supplier<T> supplier;

	public DoubleChecked(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	/**
	 * Gets a result.
	 *
	 * @return a result
	 */
	@Override
	public T get() {
		T local = value;
		if (local == null) {
			synchronized (this) {
				local = value;
				if (local == null) {
					local = value = supplier.get();
				}
			}
		}
		return local;
	}
}
