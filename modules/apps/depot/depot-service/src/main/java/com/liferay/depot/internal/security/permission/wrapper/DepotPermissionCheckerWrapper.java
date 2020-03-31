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

package com.liferay.depot.internal.security.permission.wrapper;

import com.liferay.depot.model.DepotEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.wrapper.PermissionCheckerWrapper;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Cristina González
 */
public class DepotPermissionCheckerWrapper extends PermissionCheckerWrapper {

	public DepotPermissionCheckerWrapper(
		PermissionChecker permissionChecker,
		ModelResourcePermission<DepotEntry> depotEntryModelResourcePermission,
		GroupLocalService groupLocalService) {

		super(permissionChecker);

		_depotEntryModelResourcePermission = depotEntryModelResourcePermission;
		_groupLocalService = groupLocalService;
	}

	@Override
	public boolean hasPermission(
		Group group, String name, long primKey, String actionId) {

		return _hasPermission(
			name, primKey, actionId,
			() -> super.hasPermission(group, name, primKey, actionId));
	}

	@Override
	public boolean hasPermission(
		long groupId, String name, long primKey, String actionId) {

		return _hasPermission(
			name, primKey, actionId,
			() -> super.hasPermission(groupId, name, primKey, actionId));
	}

	private Group _getDepotGroup(String name, long primKey) {
		try {
			if (!StringUtil.equals(name, Group.class.getName())) {
				return null;
			}

			Group group = _groupLocalService.getGroup(primKey);

			if (group.getType() == GroupConstants.TYPE_DEPOT) {
				return group;
			}

			return null;
		}
		catch (Exception exception) {
			_log.error(exception, exception);

			return null;
		}
	}

	private boolean _hasPermission(
		String name, long primKey, String actionId,
		Supplier<Boolean> hasPermissionSupplier) {

		try {
			Group depotGroup = _getDepotGroup(name, primKey);

			if (depotGroup == null) {
				return hasPermissionSupplier.get();
			}

			if (!_supportedActionIds.contains(actionId)) {
				return false;
			}

			return _depotEntryModelResourcePermission.contains(
				this, depotGroup.getClassPK(), actionId);
		}
		catch (PortalException portalException) {
			_log.error(portalException, portalException);

			return false;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DepotPermissionCheckerWrapper.class);

	private static final Set<String> _supportedActionIds = new HashSet<>(
		Arrays.asList(
			ActionKeys.ASSIGN_MEMBERS, ActionKeys.ASSIGN_USER_ROLES,
			ActionKeys.DELETE, ActionKeys.UPDATE, ActionKeys.VIEW,
			ActionKeys.VIEW_MEMBERS, ActionKeys.VIEW_SITE_ADMINISTRATION));

	private final ModelResourcePermission<DepotEntry>
		_depotEntryModelResourcePermission;
	private final GroupLocalService _groupLocalService;

}