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

package com.liferay.info.internal.display.contributor;

import com.liferay.info.display.contributor.InfoDisplayContributor;
import com.liferay.info.display.contributor.InfoDisplayContributorTracker;
import com.liferay.info.internal.util.MappingServiceTrackerCustomizer;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Jürgen Kappler
 */
@Component(immediate = true, service = InfoDisplayContributorTracker.class)
public class InfoDisplayContributorTrackerImpl
	implements InfoDisplayContributorTracker {

	@Override
	public InfoDisplayContributor getInfoDisplayContributor(String className) {
		return _infoDisplayContributorServiceTrackerMap.getService(className);
	}

	@Override
	public InfoDisplayContributor getInfoDisplayContributorByURLSeparator(
		String urlSeparator) {

		return _infoDisplayContributorByURLSeparator.get(urlSeparator);
	}

	@Override
	public List<InfoDisplayContributor> getInfoDisplayContributors() {
		return new ArrayList<>(
			_infoDisplayContributorServiceTrackerMap.values());
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_infoDisplayContributorServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, InfoDisplayContributor.class, "model.class.name",
				new MappingServiceTrackerCustomizer<>(
					_infoDisplayContributorByURLSeparator,
					InfoDisplayContributor::getInfoURLSeparator,
					bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_infoDisplayContributorServiceTrackerMap.close();
	}

	private static ServiceTrackerMap<String, InfoDisplayContributor>
		_infoDisplayContributorServiceTrackerMap;

	private final Map<String, InfoDisplayContributor>
		_infoDisplayContributorByURLSeparator = new ConcurrentHashMap<>();

}