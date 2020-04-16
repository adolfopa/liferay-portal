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

package com.liferay.roles.admin.internal.role;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.UserGroupRole;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.permission.GroupPermissionUtil;
import com.liferay.portal.kernel.service.permission.OrganizationPermissionUtil;
import com.liferay.portal.kernel.service.permission.RolePermissionUtil;
import com.liferay.portal.kernel.service.permission.UserGroupRolePermissionUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.roles.admin.role.RoleFilter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Adolfo Pérez
 */
@Component(service = RoleFilter.class)
public class RoleFilterImpl implements RoleFilter {

	@Override
	public List<Role> filterGroupRoles(
			PermissionChecker permissionChecker, long groupId, List<Role> roles)
		throws PortalException {

		List<Role> filteredGroupRoles = ListUtil.copy(roles);

		Iterator<Role> itr = filteredGroupRoles.iterator();

		while (itr.hasNext()) {
			Role groupRole = itr.next();

			String roleName = groupRole.getName();

			if (roleName.equals(RoleConstants.ORGANIZATION_USER) ||
				roleName.equals(RoleConstants.SITE_MEMBER)) {

				itr.remove();
			}
		}

		if (permissionChecker.isCompanyAdmin() ||
			permissionChecker.isGroupOwner(groupId)) {

			return filteredGroupRoles;
		}

		Group group = GroupLocalServiceUtil.getGroup(groupId);

		if (!GroupPermissionUtil.contains(
				permissionChecker, group, ActionKeys.ASSIGN_USER_ROLES) &&
			!OrganizationPermissionUtil.contains(
				permissionChecker, group.getOrganizationId(),
				ActionKeys.ASSIGN_USER_ROLES)) {

			return Collections.emptyList();
		}

		itr = filteredGroupRoles.iterator();

		while (itr.hasNext()) {
			Role groupRole = itr.next();

			String roleName = groupRole.getName();

			if (roleName.equals(RoleConstants.ORGANIZATION_ADMINISTRATOR) ||
				roleName.equals(RoleConstants.ORGANIZATION_OWNER) ||
				roleName.equals(RoleConstants.SITE_ADMINISTRATOR) ||
				roleName.equals(RoleConstants.SITE_OWNER) ||
				!RolePermissionUtil.contains(
					permissionChecker, groupId, groupRole.getRoleId(),
					ActionKeys.ASSIGN_MEMBERS)) {

				itr.remove();
			}
		}

		return filteredGroupRoles;
	}

	@Override
	public List<Role> filterRoles(
		PermissionChecker permissionChecker, List<Role> roles) {

		List<Role> filteredRoles = ListUtil.copy(roles);

		Iterator<Role> itr = filteredRoles.iterator();

		while (itr.hasNext()) {
			Role role = itr.next();

			String roleName = role.getName();

			if (roleName.equals(RoleConstants.GUEST) ||
				roleName.equals(RoleConstants.ORGANIZATION_USER) ||
				roleName.equals(RoleConstants.OWNER) ||
				roleName.equals(RoleConstants.SITE_MEMBER) ||
				roleName.equals(RoleConstants.USER)) {

				itr.remove();
			}
		}

		if (permissionChecker.isCompanyAdmin()) {
			return filteredRoles;
		}

		itr = filteredRoles.iterator();

		while (itr.hasNext()) {
			Role role = itr.next();

			if (!RolePermissionUtil.contains(
					permissionChecker, role.getRoleId(),
					ActionKeys.ASSIGN_MEMBERS)) {

				itr.remove();
			}
		}

		return filteredRoles;
	}

	@Override
	public List<UserGroupRole> filterUserGroupRoles(
			PermissionChecker permissionChecker,
			List<UserGroupRole> userGroupRoles)
		throws PortalException {

		List<UserGroupRole> filteredUserGroupRoles = ListUtil.copy(
			userGroupRoles);

		Iterator<UserGroupRole> itr = filteredUserGroupRoles.iterator();

		while (itr.hasNext()) {
			UserGroupRole userGroupRole = itr.next();

			Role role = userGroupRole.getRole();

			String roleName = role.getName();

			if (roleName.equals(RoleConstants.ORGANIZATION_USER) ||
				roleName.equals(RoleConstants.SITE_MEMBER)) {

				itr.remove();
			}
		}

		if (permissionChecker.isCompanyAdmin()) {
			return filteredUserGroupRoles;
		}

		itr = filteredUserGroupRoles.iterator();

		while (itr.hasNext()) {
			UserGroupRole userGroupRole = itr.next();

			if (!UserGroupRolePermissionUtil.contains(
					permissionChecker, userGroupRole.getGroupId(),
					userGroupRole.getRoleId())) {

				itr.remove();
			}
		}

		return filteredUserGroupRoles;
	}

}