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

package com.liferay.roles.admin.role;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.UserGroupRole;
import com.liferay.portal.kernel.security.permission.PermissionChecker;

import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Adolfo Pérez
 */
public class RoleFilterUtil {

	public static List<Role> filterGroupRoles(
			PermissionChecker permissionChecker, long groupId, List<Role> roles)
		throws PortalException {

		return _getRoleFilter().filterGroupRoles(
			permissionChecker, groupId, roles);
	}

	public static List<Role> filterRoles(
		PermissionChecker permissionChecker, List<Role> roles) {

		return _getRoleFilter().filterRoles(permissionChecker, roles);
	}

	public static List<UserGroupRole> filterUserGroupRoles(
			PermissionChecker permissionChecker,
			List<UserGroupRole> userGroupRoles)
		throws PortalException {

		return _getRoleFilter().filterUserGroupRoles(
			permissionChecker, userGroupRoles);
	}

	private static RoleFilter _getRoleFilter() {
		RoleFilter roleFilter = _serviceTracker.getService();

		if (roleFilter == null) {
			throw new NullPointerException("Role Filter is null");
		}

		return roleFilter;
	}

	private static final ServiceTracker<RoleFilter, RoleFilter> _serviceTracker;

	static {
		Bundle bundle = FrameworkUtil.getBundle(RoleFilterUtil.class);

		ServiceTracker<RoleFilter, RoleFilter> serviceTracker =
			new ServiceTracker<>(
				bundle.getBundleContext(), RoleFilter.class, null);

		serviceTracker.open();

		_serviceTracker = serviceTracker;
	}

}