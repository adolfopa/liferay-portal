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

import com.liferay.asset.kernel.validator.AssetEntryValidator;
import com.liferay.asset.kernel.validator.AssetEntryValidatorLocator;
import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.exception.PortalException;
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
			return DefaultAssetEntryValidator::new;
		}

		return assetEntryValidatorLocator;
	}

	@BeanReference(type = AssetEntryValidatorRegistry.class)
	protected AssetEntryValidatorRegistry assetEntryValidatorRegistry;

	private ServiceTrackerMap<String, AssetEntryValidatorLocator>
		_serviceTrackerMap;

	private class DefaultAssetEntryValidator implements AssetEntryValidator {

		@Override
		public void validate(
				long groupId, String className, long classTypePK,
				long[] categoryIds, String[] tagNames)
			throws PortalException {

			for (AssetEntryValidator assetEntryValidator :
				assetEntryValidatorRegistry.getAssetEntryValidators(
					className)) {

				assetEntryValidator.validate(
					groupId, className, classTypePK, categoryIds, tagNames);
			}
		}

	}

}