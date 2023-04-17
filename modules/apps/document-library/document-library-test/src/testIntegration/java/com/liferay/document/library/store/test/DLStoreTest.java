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

package com.liferay.document.library.store.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.store.DLStoreRequest;
import com.liferay.document.library.kernel.store.DLStoreUtil;
import com.liferay.document.library.kernel.store.Store;
import com.liferay.document.library.kernel.store.StoreArea;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Adolfo Pérez
 */
@RunWith(Arquillian.class)
public class DLStoreTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testAddFile() throws Exception {
		String fileName = StringUtil.randomString();

		_addFile(fileName, Store.VERSION_DEFAULT);

		Assert.assertTrue(
			DLStoreUtil.hasFile(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName, Store.VERSION_DEFAULT));

		StoreArea.withStoreArea(
			StoreArea.LIVE,
			() -> _assertHasStoreFile(fileName, Store.VERSION_DEFAULT));
	}

	@Test
	public void testDeleteDirectory() throws Exception {
		String fileName = StringUtil.randomString();

		_addFile(fileName, Store.VERSION_DEFAULT);

		DLStoreUtil.deleteDirectory(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			StringPool.BLANK);

		Assert.assertFalse(
			DLStoreUtil.hasFile(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName, Store.VERSION_DEFAULT));

		StoreArea.withStoreArea(
			StoreArea.EVICTED,
			() -> _assertHasStoreFile(fileName, Store.VERSION_DEFAULT));
	}

	@Test
	public void testDeleteFile() throws Exception {
		String fileName = StringUtil.randomString();

		_addFile(fileName, Store.VERSION_DEFAULT);

		DLStoreUtil.deleteFile(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			fileName, Store.VERSION_DEFAULT);

		Assert.assertFalse(
			DLStoreUtil.hasFile(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName, Store.VERSION_DEFAULT));

		StoreArea.withStoreArea(
			StoreArea.EVICTED,
			() -> _assertHasStoreFile(fileName, Store.VERSION_DEFAULT));
	}

	@Test
	public void testDeleteFileWithMultipleVersions() throws Exception {
		String fileName = StringUtil.randomString();

		_addFile(fileName, Store.VERSION_DEFAULT);
		_addFile(fileName, "2.0");

		DLStoreUtil.deleteFile(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			fileName);

		Assert.assertFalse(
			DLStoreUtil.hasFile(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName, Store.VERSION_DEFAULT));
		Assert.assertFalse(
			DLStoreUtil.hasFile(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName, "2.0"));

		StoreArea.withStoreArea(
			StoreArea.EVICTED,
			() -> {
				_assertHasStoreFile(fileName, Store.VERSION_DEFAULT);
				_assertHasStoreFile(fileName, "2.0");
			});
	}

	@Test
	public void testUpdateFile() throws Exception {
		String fileName = StringUtil.randomString();

		_addFile(fileName, Store.VERSION_DEFAULT);

		Company company = _companyLocalService.getCompany(
			TestPropsValues.getCompanyId());

		long newRepositoryId = company.getGroupId();

		DLStoreUtil.updateFile(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			newRepositoryId, fileName);

		Assert.assertFalse(
			DLStoreUtil.hasFile(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName, Store.VERSION_DEFAULT));
		Assert.assertTrue(
			DLStoreUtil.hasFile(
				TestPropsValues.getCompanyId(), newRepositoryId, fileName,
				Store.VERSION_DEFAULT));

		StoreArea.withStoreArea(
			StoreArea.EVICTED,
			() -> _assertHasStoreFile(fileName, Store.VERSION_DEFAULT));
	}

	@Test
	public void testUpdateFileVersion() throws Exception {
		String fileName = StringUtil.randomString();

		_addFile(fileName, Store.VERSION_DEFAULT);

		DLStoreUtil.updateFileVersion(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			fileName, Store.VERSION_DEFAULT, "2.0");

		Assert.assertFalse(
			DLStoreUtil.hasFile(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName, Store.VERSION_DEFAULT));
		Assert.assertTrue(
			DLStoreUtil.hasFile(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName, "2.0"));

		StoreArea.withStoreArea(
			StoreArea.EVICTED,
			() -> _assertHasStoreFile(fileName, Store.VERSION_DEFAULT));
	}

	private void _addFile(String fileName, String version) throws Exception {
		DLStoreUtil.addFile(
			DLStoreRequest.builder(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName
			).versionLabel(
				version
			).build(),
			new byte[0]);
	}

	private void _assertHasStoreFile(String fileName, String version)
		throws Exception {

		Store store = ReflectionTestUtil.getFieldValue(
			DLStoreUtil.getStore(), "_store");

		Assert.assertTrue(
			store.hasFile(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				fileName, version));
	}

	@Inject
	private CompanyLocalService _companyLocalService;

}