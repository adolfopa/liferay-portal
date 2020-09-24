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

package com.liferay.depot.internal.upgrade.v1_2_0;

import com.liferay.depot.internal.upgrade.v1_2_0.util.DepotEntryGroupRelTable;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;

import java.sql.PreparedStatement;

/**
 * @author Alejandro Tardín
 */
public class UpgradeDepotEntryGroupRel extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		alter(
			DepotEntryGroupRelTable.class,
			new AlterTableAddColumn("groupId", "LONG"),
			new AlterTableAddColumn("createDate", "DATE null"),
			new AlterTableAddColumn("modifiedDate", "DATE null"),
			new AlterTableAddColumn("uuid_", "VARCHAR(75) null"));

		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			runSQL("update DepotEntryGroupRel set groupId = toGroupId");

			try (PreparedStatement ps = connection.prepareStatement(
					"update DepotEntryGroupRel set uuid_ = ?")) {

				ps.setString(1, PortalUUIDUtil.generate());

				ps.executeUpdate();
			}
		}
	}

}