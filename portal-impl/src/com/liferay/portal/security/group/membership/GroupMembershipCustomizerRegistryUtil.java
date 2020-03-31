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

package com.liferay.portal.security.group.membership;

import com.liferay.portal.kernel.security.group.membership.GroupMembershipCustomizer;
import com.liferay.registry.collections.ServiceTrackerCollections;
import com.liferay.registry.collections.ServiceTrackerMap;

/**
 * @author Adolfo Pérez
 */
public class GroupMembershipCustomizerRegistryUtil {

	public static GroupMembershipCustomizer getGroupMembershipCustomizer(
		String className) {

		return _serviceTrackerMap.getService(className);
	}

	private static final ServiceTrackerMap<String, GroupMembershipCustomizer>
		_serviceTrackerMap = ServiceTrackerCollections.openSingleValueMap(
			GroupMembershipCustomizer.class, "group.class.name");

}