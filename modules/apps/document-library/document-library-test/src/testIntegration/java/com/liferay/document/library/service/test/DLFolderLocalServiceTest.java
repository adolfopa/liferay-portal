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

package com.liferay.document.library.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Michael C. Han
 */
@RunWith(Arquillian.class)
public class DLFolderLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testDeleteAllByGroup() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId());

		Folder parentFolder = _dlAppService.addFolder(
			_group.getGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			serviceContext);

		_dlAppService.addFolder(
			_group.getGroupId(), parentFolder.getFolderId(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			serviceContext);

		Assert.assertEquals(
			2,
			_dlFolderLocalService.getRepositoryFoldersCount(
				_group.getGroupId()));

		_dlFolderLocalService.deleteAllByGroup(_group.getGroupId());

		Assert.assertEquals(
			0,
			_dlFolderLocalService.getRepositoryFoldersCount(
				_group.getGroupId()));
	}

	@Test
	public void testGetFolderNested() throws Exception {
		String path = "/folder1/folder2/folder3";

		Assert.assertEquals(
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			_fetchFolderIdByPath(path));

		long folderId = _addFolders(path);

		Assert.assertEquals(folderId, _fetchFolderIdByPath(path));

		DLFolder dlFolder = _dlFolderLocalService.fetchFolder(folderId);

		Assert.assertEquals("folder3", dlFolder.getName());
	}

	@Test
	public void testGetFolderRootFolder() throws Exception {
		String name = RandomTestUtil.randomString();

		Assert.assertNull(
			_dlFolderLocalService.fetchFolder(
				_group.getGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
				name));

		_addFolders(String.format("/%s", name));

		Assert.assertNotNull(
			_dlFolderLocalService.fetchFolder(
				_group.getGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
				name));
	}

	@Test
	public void testGetNoAssetEntries() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId());

		Folder folder = _dlAppService.addFolder(
			_group.getGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			serviceContext);

		DLFolder dlFolder = _dlFolderLocalService.getDLFolder(
			folder.getFolderId());

		AssetEntry assetEntry = _assetEntryLocalService.fetchEntry(
			DLFolder.class.getName(), dlFolder.getFolderId());

		Assert.assertNotNull(assetEntry);

		List<DLFolder> noAssetDLFolders =
			_dlFolderLocalService.getNoAssetFolders();

		_assetEntryLocalService.deleteAssetEntry(assetEntry);

		List<DLFolder> dlFolders = new ArrayList<>(
			_dlFolderLocalService.getNoAssetFolders());

		dlFolders.removeAll(noAssetDLFolders);

		Assert.assertEquals(dlFolders.toString(), 1, dlFolders.size());
		Assert.assertEquals(dlFolder, dlFolders.get(0));
	}

	private long _addFolders(String path) throws Exception {
		long parentFolderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;

		for (String name : StringUtil.split(path, CharPool.FORWARD_SLASH)) {
			DLFolder dlFolder = _dlFolderLocalService.addFolder(
				TestPropsValues.getUserId(), _group.getGroupId(),
				_group.getGroupId(), false, parentFolderId, name,
				RandomTestUtil.randomString(), false,
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

			parentFolderId = dlFolder.getFolderId();
		}

		return parentFolderId;
	}

	private long _fetchFolderIdByPath(String path) {
		long parentFolderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;

		for (String name : StringUtil.split(path, CharPool.FORWARD_SLASH)) {
			DLFolder dlFolder = _dlFolderLocalService.fetchFolder(
				_group.getGroupId(), parentFolderId, name);

			if (dlFolder == null) {
				return DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
			}

			parentFolderId = dlFolder.getFolderId();
		}

		return parentFolderId;
	}

	@Inject
	private AssetEntryLocalService _assetEntryLocalService;

	@Inject
	private DLAppService _dlAppService;

	@Inject
	private DLFolderLocalService _dlFolderLocalService;

	@DeleteAfterTestRun
	private Group _group;

}