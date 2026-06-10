/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.upgrade.v1_0_0;

import com.liferay.depot.model.DepotEntry;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.site.cms.site.initializer.util.CMSDefaultPermissionUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Marco Leo
 */
public class CMSDefaultPermissionsUpgradeProcess extends UpgradeProcess {

	public CMSDefaultPermissionsUpgradeProcess(
		FilterFactory<Predicate> filterFactory,
		GroupLocalService groupLocalService,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryFolderLocalService objectEntryFolderLocalService) {

		_filterFactory = filterFactory;
		_groupLocalService = groupLocalService;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryFolderLocalService = objectEntryFolderLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		int count = _objectEntryFolderLocalService.getObjectEntryFoldersCount();

		Set<Long> repairedGroupIds = new HashSet<>();

		for (int start = 0; start < count; start += _BATCH_SIZE) {
			List<ObjectEntryFolder> objectEntryFolders =
				_objectEntryFolderLocalService.getObjectEntryFolders(
					start, start + _BATCH_SIZE);

			for (ObjectEntryFolder objectEntryFolder : objectEntryFolders) {
				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.
						fetchObjectDefinitionByExternalReferenceCode(
							"L_CMS_DEFAULT_PERMISSION",
							objectEntryFolder.getCompanyId());

				if (objectDefinition == null) {
					continue;
				}

				if (repairedGroupIds.add(objectEntryFolder.getGroupId())) {
					_updateCMSDefaultPermissionExternalReferenceCode(
						objectEntryFolder.getGroupId());
				}

				CMSDefaultPermissionUtil.ensureCMSDefaultPermissions(
					objectEntryFolder, _filterFactory);
			}
		}
	}

	private void _updateCMSDefaultPermissionExternalReferenceCode(long groupId)
		throws Exception {

		Group group = _groupLocalService.fetchGroup(groupId);

		if (group == null) {
			return;
		}

		ObjectEntry objectEntry =
			CMSDefaultPermissionUtil.fetchObjectEntryByDepotGroupId(
				group.getCompanyId(), group.getCreatorUserId(),
				group.getGroupId(), DepotEntry.class.getName(), _filterFactory);

		if (objectEntry == null) {
			return;
		}

		CMSDefaultPermissionUtil.updateClassExternalReferenceCode(
			objectEntry, group.getExternalReferenceCode(),
			group.getCreatorUserId());
	}

	private static final int _BATCH_SIZE = 100;

	private final FilterFactory<Predicate> _filterFactory;
	private final GroupLocalService _groupLocalService;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryFolderLocalService _objectEntryFolderLocalService;

}