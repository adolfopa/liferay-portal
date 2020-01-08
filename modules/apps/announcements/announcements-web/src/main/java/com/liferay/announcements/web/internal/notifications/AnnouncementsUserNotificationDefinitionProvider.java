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
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Locale;
import java.util.ResourceBundle;

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

	public static final String[] TYPES = PropsUtil.getArray(
		PropsKeys.ANNOUNCEMENTS_ENTRY_TYPES);

	@Activate
	protected void activate() {
		for (String type : TYPES) {
			UserNotificationDefinition userNotificationDefinition =
				new AnnouncementsUserNotificationDefinition(type);

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
	protected void deactivate() {
		UserNotificationManagerUtil.deleteUserNotificationDefinitions(
			AnnouncementsPortletKeys.ANNOUNCEMENTS);
	}

	private class AnnouncementsUserNotificationDefinition
		extends UserNotificationDefinition {

		public AnnouncementsUserNotificationDefinition(String type) {
			super(AnnouncementsPortletKeys.ANNOUNCEMENTS, 0, type, type);

			_description = type;
		}

		@Override
		public String getDescription(Locale locale) {
			ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
				locale, AnnouncementsUserNotificationDefinitionProvider.class);

			String notificationType = ResourceBundleUtil.getString(
				resourceBundle, getNotificationType());

			String description = ResourceBundleUtil.getString(
				resourceBundle,
				"receive-a-notification-when-someone-adds-a-new-announcement-" +
					"for-x-distribution-scope",
				notificationType);

			if (Validator.isNotNull(description)) {
				return description;
			}

			return _description;
		}

		private final String _description;

	}

}