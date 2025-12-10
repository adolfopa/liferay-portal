/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.journal.web.internal.util;

import com.liferay.data.engine.rest.dto.v2_0.DataDefinition;
import com.liferay.data.engine.rest.resource.v2_0.DataDefinitionResource;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.portal.kernel.service.UserLocalService;

/**
 * @author Adolfo Pérez
 */
public class ImportDataDefinitionHelper {

	public ImportDataDefinitionHelper(
		DataDefinitionResource.Factory dataDefinitionResourceFactory,
		DDMStructureLocalService ddmStructureLocalService,
		UserLocalService userLocalService) {

		_dataDefinitionResourceFactory = dataDefinitionResourceFactory;
		_ddmStructureLocalService = ddmStructureLocalService;
		_userLocalService = userLocalService;
	}

	public void importAndOverride(
			long userId, long dataDefinitionId, String json)
		throws Exception {

		DataDefinitionResource.Builder dataDefinitionResourcedBuilder =
			_dataDefinitionResourceFactory.create();

		DataDefinitionResource dataDefinitionResource =
			dataDefinitionResourcedBuilder.user(
				_userLocalService.getUser(userId)
			).build();

		DataDefinition dataDefinition = DataDefinition.toDTO(json);

		DDMStructure ddmStructure = _ddmStructureLocalService.getDDMStructure(
			dataDefinitionId);

		DataDefinitionUtil.updateDataDefinitionFields(
			dataDefinition, ddmStructure);

		dataDefinition.setExternalReferenceCode(
			ddmStructure::getExternalReferenceCode);

		dataDefinitionResource.putDataDefinition(
			dataDefinitionId, dataDefinition);
	}

	private final DataDefinitionResource.Factory _dataDefinitionResourceFactory;
	private final DDMStructureLocalService _ddmStructureLocalService;
	private final UserLocalService _userLocalService;

}