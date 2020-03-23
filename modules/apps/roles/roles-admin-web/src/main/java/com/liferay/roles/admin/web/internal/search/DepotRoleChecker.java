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

package com.liferay.roles.admin.web.internal.search;

import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.util.ArrayUtil;

import javax.portlet.PortletResponse;

/**
 * @author Alicia García
 */
public class DepotRoleChecker extends RoleChecker {

	public DepotRoleChecker(PortletResponse portletResponse) {
		super(portletResponse);
	}

	@Override
	public boolean isDisabled(Object obj) {
		Role role = (Role)obj;

		if (ArrayUtil.contains(
				RoleConstants.SYSTEM_ASSET_LIBRARY_ROLES, role.getName())) {

			return true;
		}

		return super.isDisabled(obj);
	}

}