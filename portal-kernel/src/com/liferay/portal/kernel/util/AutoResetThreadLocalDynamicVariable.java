/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.kernel.util;

/**
 * @author Adolfo Pérez
 */
public class AutoResetThreadLocalDynamicVariable<T>
	implements DynamicVariable<T> {

	public AutoResetThreadLocalDynamicVariable(
		java.util.function.Supplier<T> supplier) {

		_threadLocal = new AutoResetThreadLocal<>(null, supplier);
	}

	@Override
	public T getValue() {
		return _threadLocal.get();
	}

	@Override
	public <S, E extends Exception> S withValue(
			T value, CheckedCallable<S, E> callable)
		throws E {

		T oldValue = _threadLocal.get();

		try {
			_threadLocal.set(value);

			return callable.call();
		}
		finally {
			_threadLocal.set(oldValue);
		}
	}

	@Override
	public <E extends Exception> void withValue(
			T value, CheckedRunnable<E> runnable)
		throws E {

		T oldValue = _threadLocal.get();

		try {
			_threadLocal.set(value);

			runnable.run();
		}
		finally {
			_threadLocal.set(oldValue);
		}
	}

	private final AutoResetThreadLocal<T> _threadLocal;

}