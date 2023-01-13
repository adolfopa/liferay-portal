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
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.upgrade.UpgradeException;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Adolfo Pérez
 */
public class UpgradeBasicDocumentDLFileEntryType extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		DLFileEntryTypeData dlFileEntryTypeData = _getDLFileEntryTypeData();

		if (dlFileEntryTypeData == null) {
			throw new UpgradeException(
				"Could not find basic document dl file entry type");
		}

		runSQL(
			"delete from DLFileEntryType where fileEntryTypeId = '" +
				DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT +
					"'");

		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select companyId from Company");
			PreparedStatement preparedStatement2 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					StringBundler.concat(
						"insert into DLFileEntryType (uuid_, fileEntryTypeId, ",
						"groupId, companyId, createDate, modifiedDate, ",
						"dataDefinitionId, fileEntryTypeKey, name, ",
						"lastPublishDate) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ",
						"?)"));
			PreparedStatement preparedStatement3 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update DLFileEntry set fileEntryTypeId = ? where " +
						"companyId = ? and fileEntryTypeId = ?");
			PreparedStatement preparedStatement4 =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update DLFileVersion set fileEntryTypeId = ? where " +
						"companyId = ? and fileEntryTypeId = ?");
			ResultSet resultSet = preparedStatement1.executeQuery()) {

			Map<Long, Long> fileEntryTypeIds = new HashMap<>();

			while (resultSet.next()) {
				long companyId = resultSet.getLong("companyId");

				long fileEntryTypeId = increment();

				fileEntryTypeIds.put(companyId, fileEntryTypeId);

				preparedStatement2.setString(
					1, String.valueOf(UUID.randomUUID()));
				preparedStatement2.setLong(2, fileEntryTypeId);
				preparedStatement2.setLong(
					3, GroupConstants.DEFAULT_LIVE_GROUP_ID);
				preparedStatement2.setLong(4, companyId);
				preparedStatement2.setDate(
					5, dlFileEntryTypeData.getCreateDate());
				preparedStatement2.setDate(
					6, dlFileEntryTypeData.getModifiedDate());
				preparedStatement2.setLong(7, _DEFAULT_DDM_STRUCTURE_ID);
				preparedStatement2.setString(
					8,
					DLFileEntryTypeConstants.
						FILE_ENTRY_TYPE_KEY_BASIC_DOCUMENT);
				preparedStatement2.setString(9, dlFileEntryTypeData.getName());
				preparedStatement2.setDate(
					10, dlFileEntryTypeData.getLastPublishedDate());

				preparedStatement2.addBatch();
			}

			preparedStatement2.executeBatch();

			for (Map.Entry<Long, Long> entry : fileEntryTypeIds.entrySet()) {
				preparedStatement3.setLong(1, entry.getValue());
				preparedStatement3.setLong(2, entry.getKey());
				preparedStatement3.setLong(
					3,
					DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT);

				preparedStatement3.addBatch();

				preparedStatement4.setLong(1, entry.getValue());
				preparedStatement4.setLong(2, entry.getKey());
				preparedStatement4.setLong(
					3,
					DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT);

				preparedStatement4.addBatch();
			}

			preparedStatement3.executeBatch();
			preparedStatement4.executeBatch();
		}
	}

	private DLFileEntryTypeData _getDLFileEntryTypeData() throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select createDate, lastPublishDate, modifiedDate, name from " +
					"DLFileEntryType where fileEntryTypeId = ?")) {

			preparedStatement.setLong(
				1, DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT);

			ResultSet resultSet = preparedStatement.executeQuery();

			if (!resultSet.next()) {
				return null;
			}

			return new DLFileEntryTypeData(
				resultSet.getDate("createDate"),
				resultSet.getDate("lastPublishDate"),
				resultSet.getDate("modifiedDate"), resultSet.getString("name"));
		}
	}

	private static final long _DEFAULT_DDM_STRUCTURE_ID = 0;

	private static final class DLFileEntryTypeData {

		public Date getCreateDate() {
			return _createDate;
		}

		public Date getLastPublishedDate() {
			return _lastPublishedDate;
		}

		public Date getModifiedDate() {
			return _modifiedDate;
		}

		public String getName() {
			return _name;
		}

		private DLFileEntryTypeData(
			Date createDate, Date lastPublishedDate, Date modifiedDate,
			String name) {

			_createDate = createDate;
			_lastPublishedDate = lastPublishedDate;
			_modifiedDate = modifiedDate;
			_name = name;
		}

		private final Date _createDate;
		private final Date _lastPublishedDate;
		private final Date _modifiedDate;
		private final String _name;

	}

}