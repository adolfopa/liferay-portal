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
import com.liferay.asset.kernel.validator.AssetEntryValidator;
import com.liferay.asset.kernel.validator.AssetEntryValidatorLocator;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.portlet.asset.validator.AssetEntryValidatorRegistry;

import java.util.List;

import org.osgi.service.component.annotations.Component;
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
	service = AssetEntryValidatorLocator.class
)
public class DLFileEntryAssetEntryValidatorLocator
	implements AssetEntryValidatorLocator {

	@Override
	public AssetEntryValidator getAssetEntryValidator() {
		List<AssetEntryValidator> assetEntryValidators =
			_assetEntryValidatorRegistry.getAssetEntryValidators(
				DLFileEntryConstants.getClassName());

		return new DLFileEntryAggregateAssetEntryValidator(
			assetEntryValidators.toArray(
				new AssetEntryValidator[assetEntryValidators.size()]));
	}

	@Reference(unbind = "-")
	private AssetEntryValidatorRegistry _assetEntryValidatorRegistry;

	@Reference(unbind = "-")
	private DLFileEntryLocalService _dlFileEntryLocalService;

	private class DLFileEntryAggregateAssetEntryValidator
		extends AggregateAssetEntryValidator {

		public DLFileEntryAggregateAssetEntryValidator(
			AssetEntryValidator... assetEntryValidators) {

			super(assetEntryValidators);
		}

		@Override
		protected boolean skipValidation(
			long groupId, String className, long classPK, long classTypePK,
			long[] categoryIds, String[] entryNames) {

			DLFileEntry dlFileEntry = _dlFileEntryLocalService.fetchDLFileEntry(
				classPK);

			if ((dlFileEntry == null) ||
				(dlFileEntry.getRepositoryId() != groupId)) {

				return true;
			}

			return false;
		}

	}

}