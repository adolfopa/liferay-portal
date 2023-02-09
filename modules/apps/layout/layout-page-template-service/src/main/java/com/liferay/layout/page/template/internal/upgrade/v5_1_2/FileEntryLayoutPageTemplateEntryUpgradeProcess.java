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

package com.liferay.layout.page.template.internal.upgrade.v5_1_2;

import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;

/**
 * @author Adolfo Pérez
 */
public class FileEntryLayoutPageTemplateEntryUpgradeProcess
	extends UpgradeProcess {

	public FileEntryLayoutPageTemplateEntryUpgradeProcess(
		ClassNameLocalService classNameLocalService,
		CompanyLocalService companyLocalService,
		DLFileEntryTypeLocalService dlFileEntryTypeLocalService) {

		_classNameLocalService = classNameLocalService;
		_companyLocalService = companyLocalService;
		_dlFileEntryTypeLocalService = dlFileEntryTypeLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		long classNameId = _classNameLocalService.getClassNameId(
			FileEntry.class);

		_companyLocalService.forEachCompanyId(
			companyId -> {
				try (PreparedStatement preparedStatement =
						connection.prepareStatement(
							"update LayoutPageTemplateEntry set classTypeId " +
								"= ? where companyId = ? and classNameId = ? " +
									"and classTypeId = 0")) {

					DLFileEntryType basicDocumentDLFileEntryType =
						_dlFileEntryTypeLocalService.
							getBasicDocumentDLFileEntryType(companyId);

					preparedStatement.setLong(
						1, basicDocumentDLFileEntryType.getFileEntryTypeId());

					preparedStatement.setLong(2, companyId);
					preparedStatement.setLong(3, classNameId);

					preparedStatement.execute();
				}
			});
	}

	private final ClassNameLocalService _classNameLocalService;
	private final CompanyLocalService _companyLocalService;
	private final DLFileEntryTypeLocalService _dlFileEntryTypeLocalService;

}