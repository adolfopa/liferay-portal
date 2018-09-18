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

package com.liferay.announcements.web.internal.upgrade.v1_1_0;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.model.PortletConstants;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.StringBundler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Roberto Díaz
 */
public class UpgradePortletPreferences extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		upgradePortletPreferences();
	}

	protected String getPortletId() {
		return "com_liferay_announcements_web_portlet_AnnouncementsPortlet";
	}

	protected void updatePortletPreferences(
			long portletPreferencesId, String preferences)
		throws Exception {

		PreparedStatement ps = null;

		try {
			ps = connection.prepareStatement(
				"update PortletPreferences set preferences = ? where " +
					"portletPreferencesId = ?");

			ps.setString(1, preferences);
			ps.setLong(2, portletPreferencesId);

			ps.executeUpdate();
		}
		finally {
			DataAccess.cleanUp(ps);
		}
	}

	protected void upgradePortletPreferences() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			StringBundler sb1 = new StringBundler(10);

			sb1.append("select PP1.portletPreferencesId, PP1.preferences ");
			sb1.append("from PortletPreferences as PP1 inner join ");
			sb1.append("PortletPreferences as PP2 on PP1.companyId = ");
			sb1.append("PP2.ownerId where PP1.portletId = '");
			sb1.append(getPortletId());
			sb1.append("' AND PP2.portletId = '");
			sb1.append(getPortletId());
			sb1.append("' AND PP1.ownerType = ");
			sb1.append(PortletKeys.PREFS_OWNER_TYPE_COMPANY);
			sb1.append(";");

			try (PreparedStatement ps1 = connection.prepareStatement(
					sb1.toString());
				ResultSet rs1 = ps1.executeQuery()) {

				while (rs1.next()) {
					String preferences = rs1.getString("preferences");

					if (preferences.equals(
							PortletConstants.DEFAULT_PREFERENCES)) {

						continue;
					}

					long portletPreferencesId = rs1.getLong(
						"portletPreferencesId");

					updatePortletPreferences(
						portletPreferencesId,
						PortletConstants.DEFAULT_PREFERENCES);
				}
			}
		}
	}

}