/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.upgrade.v7_4_x;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Validator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Adolfo Pérez
 */
public class LayoutLayoutSetPrototypeLayoutExternalReferenceCodeUpgradeProcess
	extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				StringBundler.concat(
					"select Layout1.ctCollectionId, ",
					"Layout1.externalReferenceCode, Layout1.groupId, ",
					"Layout1.layoutSetPrototypeLayoutERC, Layout1.plid from ",
					"Layout Layout1 where Layout1.externalReferenceCode != ",
					"Layout1.layoutSetPrototypeLayoutERC and ",
					"Layout1.layoutSetPrototypeLayoutERC is not null and not ",
					"exists (select 1 from Layout Layout2 where ",
					"Layout2.ctCollectionId = Layout1.ctCollectionId and ",
					"Layout2.externalReferenceCode = ",
					"Layout1.layoutSetPrototypeLayoutERC and Layout2.groupId ",
					"= Layout1.groupId) order by Layout1.plid"));
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update Layout set externalReferenceCode = ? where " +
						"ctCollectionId = ? and externalReferenceCode = ? " +
							"and plid = ?");
			ResultSet resultSet = preparedStatement1.executeQuery()) {

			Set<String> keys = new HashSet<>();

			while (resultSet.next()) {
				String layoutSetPrototypeLayoutERC = resultSet.getString(
					"layoutSetPrototypeLayoutERC");

				if (Validator.isNull(layoutSetPrototypeLayoutERC)) {
					continue;
				}

				long ctCollectionId = resultSet.getLong("ctCollectionId");

				if (!keys.add(
						StringBundler.concat(
							ctCollectionId, StringPool.POUND,
							resultSet.getLong("groupId"), StringPool.POUND,
							layoutSetPrototypeLayoutERC))) {

					continue;
				}

				preparedStatement2.setString(1, layoutSetPrototypeLayoutERC);
				preparedStatement2.setLong(2, ctCollectionId);
				preparedStatement2.setString(
					3, resultSet.getString("externalReferenceCode"));
				preparedStatement2.setLong(4, resultSet.getLong("plid"));

				preparedStatement2.addBatch();
			}

			preparedStatement2.executeBatch();
		}
	}

}