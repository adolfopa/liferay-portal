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

package com.liferay.portal.upgrade.v7_4_x;

import com.liferay.document.library.kernel.model.DLFileEntryTypeConstants;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Adolfo Pérez
 */
public class UpgradeDLFolder extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select companyId, fileEntryTypeId from DLFileEntryType " +
					"where groupId = ? and fileEntryTypeKey = ?");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update DLFolder set defaultFileEntryTypeId = ? where " +
						"companyId = ? and restrictionType = ?")) {

			preparedStatement1.setLong(1, GroupConstants.DEFAULT_LIVE_GROUP_ID);
			preparedStatement1.setString(
				2, DLFileEntryTypeConstants.FILE_ENTRY_TYPE_KEY_BASIC_DOCUMENT);

			try (ResultSet resultSet = preparedStatement1.executeQuery()) {
				while (resultSet.next()) {
					preparedStatement2.setLong(
						1, resultSet.getLong("fileEntryTypeId"));
					preparedStatement2.setLong(
						2, resultSet.getLong("companyId"));
					preparedStatement2.setInt(
						3,
						DLFolderConstants.
							RESTRICTION_TYPE_FILE_ENTRY_TYPES_AND_WORKFLOW);

					preparedStatement2.addBatch();
				}

				preparedStatement2.executeBatch();
			}
		}
	}

}