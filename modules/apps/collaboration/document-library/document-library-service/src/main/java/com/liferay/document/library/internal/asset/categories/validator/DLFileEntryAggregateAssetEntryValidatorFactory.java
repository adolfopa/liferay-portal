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

package com.liferay.document.library.internal.asset.categories.validator;

import com.liferay.asset.kernel.validator.AggregateAssetEntryValidator;
import com.liferay.asset.kernel.validator.AggregateAssetEntryValidatorFactory;
import com.liferay.asset.kernel.validator.AssetEntryValidator;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portlet.asset.validator.AssetEntryValidatorRegistry;

import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 * @author Roberto Díaz
 */
@Component(
	immediate = true,
	property = {
		"model.class.name=com.liferay.document.library.kernel.model.DLFileEntry",
		"model.class.name=com.liferay.portal.kernel.repository.model.FileEntry"
	},
	service = AggregateAssetEntryValidatorFactory.class
)
public class DLFileEntryAggregateAssetEntryValidatorFactory
	implements AggregateAssetEntryValidatorFactory {

	@Activate
	@Modified
	public void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, AssetEntryValidator.class, "model.class.name");
	}

	@Deactivate
	public void deactivate() {
		_serviceTrackerMap.close();
	}

	@Override
	public AggregateAssetEntryValidator getAggregateAssetEntryValidator(
		long groupId, String className, long classPK, long classTypePK,
		long[] categoryIds, String[] tagNames) {

		DLFileEntry dlFileEntry = _dlFileEntryLocalService.fetchDLFileEntry(
			classPK);

		if ((dlFileEntry == null) ||
			(dlFileEntry.getRepositoryId() != groupId)) {

			return null;
		}

		List<AssetEntryValidator> assetEntryValidators =
			_assetEntryValidatorRegistry.getAssetEntryValidators(className);

		return new AggregateAssetEntryValidator(
			assetEntryValidators.toArray(
				new AssetEntryValidator[assetEntryValidators.size()]));
	}

	@Reference(unbind = "-")
	private AssetEntryValidatorRegistry _assetEntryValidatorRegistry;

	@Reference(unbind = "-")
	private DLFileEntryLocalService _dlFileEntryLocalService;

	private ServiceTrackerMap<String, List<AssetEntryValidator>>
		_serviceTrackerMap;

}