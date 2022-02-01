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

package com.liferay.depot.internal.upgrade.v2_1_0;

import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Adolfo Pérez
 */
public class DepotEntryGroupRelUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			try (PreparedStatement preparedStatement1 =
					connection.prepareStatement(
						"select depotEntryGroupRelId, depotEntryId from " +
							"DepotEntryGroupRel");
				PreparedStatement preparedStatement2 =
					AutoBatchPreparedStatementUtil.autoBatch(
						connection.prepareStatement(
							"update DepotEntryGroupRel set groupId = ? where " +
								"depotEntryGroupRelId = ?"));
				ResultSet resultSet1 = preparedStatement1.executeQuery()) {

				while (resultSet1.next()) {
					try (PreparedStatement preparedStatement3 =
							connection.prepareStatement(
								"select groupId from DepotEntry where " +
									"depotEntryId = ?")) {

						preparedStatement3.setLong(1, resultSet1.getLong(2));

						ResultSet resultSet2 =
							preparedStatement3.executeQuery();

						if (resultSet2.next()) {
							preparedStatement2.setLong(
								1, resultSet2.getLong(1));
							preparedStatement2.setLong(
								2, resultSet1.getLong(1));

							preparedStatement2.addBatch();
						}
					}
				}

				preparedStatement2.executeBatch();
			}
		}
	}

}