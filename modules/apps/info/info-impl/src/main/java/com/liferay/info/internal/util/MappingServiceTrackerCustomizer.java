/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.info.internal.util;

import java.util.Map;
import java.util.function.Function;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Adolfo Pérez
 */
public class MappingServiceTrackerCustomizer<T>
	implements ServiceTrackerCustomizer<T, T> {

	public MappingServiceTrackerCustomizer(
		Map<String, T> map, Function<T, String> function,
		BundleContext bundleContext) {

		_map = map;
		_function = function;
		_bundleContext = bundleContext;
	}

	@Override
	public T addingService(ServiceReference<T> serviceReference) {
		T service = _bundleContext.getService(serviceReference);

		_map.put(_function.apply(service), service);

		return service;
	}

	@Override
	public void modifiedService(
		ServiceReference<T> serviceReference, T service) {

		removedService(serviceReference, service);

		addingService(serviceReference);
	}

	@Override
	public void removedService(
		ServiceReference<T> serviceReference, T service) {

		_map.remove(_function.apply(service));
	}

	private final BundleContext _bundleContext;
	private final Function<T, String> _function;
	private final Map<String, T> _map;

}