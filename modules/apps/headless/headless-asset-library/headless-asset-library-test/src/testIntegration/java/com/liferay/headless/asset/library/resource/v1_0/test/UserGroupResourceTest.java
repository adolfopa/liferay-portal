/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.asset.library.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;

import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.headless.asset.library.client.dto.v1_0.UserGroup;
import com.liferay.headless.asset.library.client.resource.v1_0.AssetLibraryResource;
import com.liferay.headless.asset.library.dto.v1_0.AssetLibrary;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserGroupTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.util.PropsValues;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Roberto Díaz
 */
@FeatureFlags("LPD-17564")
@RunWith(Arquillian.class)
public class UserGroupResourceTest extends BaseUserGroupResourceTestCase {

	@Override
	protected UserGroup testPostAssetLibraryByExternalReferenceCodeUserGroup_addUserGroup(
		UserGroup userGroup) throws Exception {

		com.liferay.portal.kernel.model.UserGroup userGroup1 =
			_userGroupLocalService.addUserGroup(
				userGroup.getExternalReferenceCode(),
				TestPropsValues.getUserId(), TestPropsValues.getCompanyId(),
				userGroup.getName(), RandomTestUtil.randomString(),
				ServiceContextTestUtil.getServiceContext());

		userGroup.setId(userGroup1.getUserGroupId());

		userGroupResource.postAssetLibraryUserGroup(
			testDepotEntry.getGroupId(), userGroup);

		return userGroup;

	}

	@Override
	public void testPostAssetLibraryUserGroup() throws Exception {
		super.testPostAssetLibraryUserGroup();
	}

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject
	private UserGroupLocalService _userGroupLocalService;
}