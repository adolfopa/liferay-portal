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

package com.liferay.portlet.asset.validator;

import com.liferay.asset.kernel.validator.AssetEntryValidatorLocator;
import com.liferay.portal.kernel.spring.osgi.OSGiBeanProperties;
import com.liferay.registry.collections.ServiceTrackerCollections;
import com.liferay.registry.collections.ServiceTrackerMap;

/**
 * @author Adolfo Pérez
 */
@OSGiBeanProperties(service = AssetEntryValidatorLocatorRegistry.class)
public class AssetEntryValidatorLocatorRegistry {

	public void afterPropertiesSet() {
		_serviceTrackerMap = ServiceTrackerCollections.openSingleValueMap(
			AssetEntryValidatorLocator.class, "model.class.name");
	}

	public void destroy() {
		_serviceTrackerMap.close();
	}

	public AssetEntryValidatorLocator getAssetEntryValidatorLocator(
		String className) {

		AssetEntryValidatorLocator assetEntryValidatorLocator =
			_serviceTrackerMap.getService(className);

		if (assetEntryValidatorLocator == null) {
			return null;
		}

		return assetEntryValidatorLocator;
	}

	private ServiceTrackerMap<String, AssetEntryValidatorLocator>
		_serviceTrackerMap;

}