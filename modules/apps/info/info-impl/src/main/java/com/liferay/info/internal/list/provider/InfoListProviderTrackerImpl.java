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

package com.liferay.info.internal.list.provider;

import com.liferay.info.list.provider.InfoListProvider;
import com.liferay.info.list.provider.InfoListProviderTracker;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Eudaldo Alonso
 */
@Component(immediate = true, service = InfoListProviderTracker.class)
public class InfoListProviderTrackerImpl implements InfoListProviderTracker {

	@Override
	public InfoListProvider getInfoListProvider(String className) {
		if (Validator.isNull(className)) {
			return null;
		}

		List<InfoListProvider> infoListProviders =
			_itemClassNameInfoListProvidersServiceTrackerMap.getService(
				className);

		return infoListProviders.get(0);
	}

	@Override
	public List<InfoListProvider> getInfoListProviders() {
		List<InfoListProvider> infoListProviders = new ArrayList<>();

		for (List<InfoListProvider> infoListProviderList :
				_itemClassNameInfoListProvidersServiceTrackerMap.values()) {

			infoListProviders.addAll(infoListProviderList);
		}

		return infoListProviders;
	}

	@Override
	public List<InfoListProvider> getInfoListProviders(Class<?> itemClass) {
		return Collections.unmodifiableList(
			_itemClassNameInfoListProvidersServiceTrackerMap.getService(
				itemClass.getName()));
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_itemClassNameInfoListProvidersServiceTrackerMap =
			ServiceTrackerMapFactory.openMultiValueMap(
				bundleContext, InfoListProvider.class, "model.class.name");
	}

	@Deactivate
	protected void deactivate() {
		_itemClassNameInfoListProvidersServiceTrackerMap.close();
	}

	private ServiceTrackerMap<String, List<InfoListProvider>>
		_itemClassNameInfoListProvidersServiceTrackerMap;

}