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
public abstract class AggregateAssetEntryValidator
	implements AssetEntryValidator {

	public AggregateAssetEntryValidator(
		AssetEntryValidator... assetEntryValidators) {

		_assetEntryValidators = assetEntryValidators;
	}

	@Override
	public void validate(
			long groupId, String className, long classPK, long classTypePK,
			long[] categoryIds, String[] entryNames)
		throws PortalException {

		if (skipValidation(
				groupId, className, classPK, classTypePK, categoryIds,
				entryNames)) {

			return;
		}

		for (AssetEntryValidator assetEntryValidator : _assetEntryValidators) {
			assetEntryValidator.validate(
				groupId, className, classPK, classTypePK, categoryIds,
				entryNames);
		}
	}

	@Override
	public void validate(
			long groupId, String className, long classTypePK,
			long[] categoryIds, String[] entryNames)
		throws PortalException {

		if (skipValidation(
				groupId, className, 0L, classTypePK, categoryIds, entryNames)) {

			return;
		}

		for (AssetEntryValidator assetEntryValidator : _assetEntryValidators) {
			assetEntryValidator.validate(
				groupId, className, classTypePK, categoryIds, entryNames);
		}
	}

	protected abstract boolean skipValidation(
		long groupId, String className, long classPK, long classTypePK,
		long[] categoryIds, String[] entryNames);

	private final AssetEntryValidator[] _assetEntryValidators;

}