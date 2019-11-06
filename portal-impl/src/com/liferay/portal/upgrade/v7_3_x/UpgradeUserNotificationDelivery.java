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

package com.liferay.portal.upgrade.v7_3_x;

import com.liferay.announcements.kernel.model.AnnouncementsEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.upgrade.v7_3_x.util.UserNotificationDeliveryTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Roberto Díaz
 */
public class UpgradeUserNotificationDelivery extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		upgradeTable();

		migrateAnnouncementsDeliveriesToUserNotificationDelivery();
	}

	protected void migrateAnnouncementsDeliveriesToUserNotificationDelivery()
		throws Exception {

		try (PreparedStatement ps = connection.prepareStatement(
				"SELECT * FROM AnnouncementsDelivery announcementsDelivery " +
					"ORDER BY announcementsDelivery.deliveryId ASC");
			PreparedStatement updatePS =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection.prepareStatement(
						SQLTransformer.transform(
							StringBundler.concat(
								"insert into UserNotificationDelivery (",
								"userNotificationDeliveryId, companyId, ",
								"userId, portletId, classNameId, ",
								"notificationType, deliveryType, deliver) ",
								"values (?, ?, ?, ?, ?, ?, ?, ?)"))))) {

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				long companyId = rs.getLong("companyId");
				long userId = rs.getLong("userId");
				String type = rs.getString("type_");

				if (rs.getBoolean("email")) {
					_migrateAnnouncementsDeliveryToUserNotificationDelivery(
						updatePS, companyId, userId, type,
						UserNotificationDeliveryConstants.TYPE_EMAIL);
				}
				else if (rs.getBoolean("website")) {
					_migrateAnnouncementsDeliveryToUserNotificationDelivery(
						updatePS, companyId, userId, type,
						UserNotificationDeliveryConstants.TYPE_WEBSITE);
				}
			}

			updatePS.executeBatch();
		}
	}

	protected void upgradeTable() throws Exception {
		if (!hasColumnType(
				"UserNotificationDelivery", "notificationType",
				"VARCHAR(75) null")) {

			alter(
				UserNotificationDeliveryTable.class,
				new AlterColumnType("notificationType", "VARCHAR(75) null"));
		}
	}

	private void _migrateAnnouncementsDeliveryToUserNotificationDelivery(
			PreparedStatement ps, long companyId, long userId, String type,
			int notificationType)
		throws SQLException {

		ps.setLong(1, increment(AnnouncementsEntry.class.getName()));
		ps.setLong(2, companyId);
		ps.setLong(3, userId);
		ps.setString(4, _ANNOUNCEMENTS_PORTLET_ID);
		ps.setLong(5, PortalUtil.getClassNameId(AnnouncementsEntry.class));
		ps.setString(6, type);
		ps.setInt(7, notificationType);
		ps.setBoolean(8, true);

		ps.addBatch();
	}

	private static final String _ANNOUNCEMENTS_PORTLET_ID =
		"com_liferay_announcements_web_portlet_AnnouncementsPortlet";

}