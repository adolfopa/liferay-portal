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

package com.liferay.depot.internal.security.permission;

import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.group.membership.GroupMembershipCustomizer;
import com.liferay.portal.kernel.service.UserGroupRoleLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "group.class.name=com.liferay.depot.model.DepotEntry",
	service = GroupMembershipCustomizer.class
)
public class DepotGroupMembershipCustomizer
	implements GroupMembershipCustomizer {

	@Override
	public Boolean isGroupAdmin(User user, Group group) throws PortalException {
		if (group.getType() != GroupConstants.TYPE_DEPOT) {
			return false;
		}

		if (_userGroupRoleLocalService.hasUserGroupRole(
				user.getUserId(), group.getGroupId(),
				DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR, true) ||
			_userGroupRoleLocalService.hasUserGroupRole(
				user.getUserId(), group.getGroupId(),
				DepotRolesConstants.ASSET_LIBRARY_OWNER, true)) {

			return true;
		}

		return false;
	}

	@Override
	public Boolean isGroupContentReviewer(User user, Group group) {
		return null;
	}

	@Override
	public Boolean isGroupMember(User user, Group group)
		throws PortalException {

		if (group.getType() != GroupConstants.TYPE_DEPOT) {
			return null;
		}

		if (_userGroupRoleLocalService.hasUserGroupRole(
				user.getUserId(), group.getGroupId(),
				DepotRolesConstants.ASSET_LIBRARY_MEMBER, true)) {

			return true;
		}

		return null;
	}

	@Override
	public Boolean isGroupOwner(User user, Group group) throws PortalException {
		if (group.getType() != GroupConstants.TYPE_DEPOT) {
			return null;
		}

		if (_userGroupRoleLocalService.hasUserGroupRole(
				user.getUserId(), group.getGroupId(),
				DepotRolesConstants.ASSET_LIBRARY_OWNER, true)) {

			return true;
		}

		return false;
	}

	@Reference
	private UserGroupRoleLocalService _userGroupRoleLocalService;

}