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

package com.liferay.asset.kernel.validator;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Roberto Díaz
 */
public class AggregateAssetEntryValidator {

	public AggregateAssetEntryValidator(
		AssetEntryValidator...assetEntryValidators) {

		_assetEntryValidators = assetEntryValidators;
	}

	public void validate(
			long groupId, String className, long classPK, long classTypePK,
			long[] categoryIds, String[] entryNames)
		throws PortalException {

		for (AssetEntryValidator assetEntryValidator : _assetEntryValidators) {
			assetEntryValidator.validate(
				groupId, className, classPK, classTypePK, categoryIds,
				entryNames);
		}
	}

	private final AssetEntryValidator[] _assetEntryValidators;

}