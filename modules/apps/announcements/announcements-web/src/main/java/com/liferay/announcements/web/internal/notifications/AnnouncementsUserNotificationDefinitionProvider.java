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

package com.liferay.announcements.web.internal.notifications;

import com.liferay.announcements.constants.AnnouncementsPortletKeys;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.notifications.UserNotificationDefinition;
import com.liferay.portal.kernel.notifications.UserNotificationDeliveryType;

import com.liferay.portal.kernel.notifications.UserNotificationManagerUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Roberto Díaz
 */
@Component(
	immediate = true,
	service = AnnouncementsUserNotificationDefinitionProvider.class
)
public class AnnouncementsUserNotificationDefinitionProvider {

	@Activate
	public void activate() {
		for (String type : TYPES) {
			UserNotificationDefinition userNotificationDefinition =
				new UserNotificationDefinition(
					AnnouncementsPortletKeys.ANNOUNCEMENTS, 0, type,
					type);

			userNotificationDefinition.addUserNotificationDeliveryType(
				new UserNotificationDeliveryType(
					"email", UserNotificationDeliveryConstants.TYPE_EMAIL,
					false, true));
			userNotificationDefinition.addUserNotificationDeliveryType(
				new UserNotificationDeliveryType(
					"website", UserNotificationDeliveryConstants.TYPE_WEBSITE,
					true, false));

			UserNotificationManagerUtil.addUserNotificationDefinition(
				AnnouncementsPortletKeys.ANNOUNCEMENTS,
				userNotificationDefinition);
		}
	}

	@Deactivate
	public void deactivate() {
		UserNotificationManagerUtil.deleteUserNotificationDefinitions(
			AnnouncementsPortletKeys.ANNOUNCEMENTS);
	}

	public static final String[] TYPES = PropsUtil.getArray(
		PropsKeys.ANNOUNCEMENTS_ENTRY_TYPES);

}