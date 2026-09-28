/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.BatchEngineTaskOperation;
import com.liferay.batch.engine.constants.BatchEngineImportTaskConstants;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.service.BatchEngineImportTaskLocalService;
import com.liferay.petra.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Adolfo Pérez
 */
@RunWith(Arquillian.class)
public class BatchEngineImportTaskLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testUpdateBatchEngineImportTask() throws Exception {
		_testUpdateBatchEngineImportTask(
			BatchEngineTaskExecuteStatus.COMPLETED);
		_testUpdateBatchEngineImportTask(BatchEngineTaskExecuteStatus.FAILED);
	}

	private BatchEngineImportTask _addBatchEngineImportTask(
			String emailAddress, String password)
		throws Exception {

		BatchEngineImportTask batchEngineImportTask =
			_batchEngineImportTaskLocalService.addBatchEngineImportTask(
				null, TestPropsValues.getCompanyId(),
				TestPropsValues.getUserId(), 10, null,
				"com.liferay.headless.admin.user.dto.v1_0.UserAccount",
				_getContent(emailAddress, password), "JSON",
				BatchEngineTaskExecuteStatus.INITIAL.name(), null,
				BatchEngineImportTaskConstants.IMPORT_STRATEGY_ON_ERROR_FAIL,
				BatchEngineTaskOperation.CREATE.name(), new HashMap<>(), null);

		_batchEngineImportTasks.add(batchEngineImportTask);

		return batchEngineImportTask;
	}

	private byte[] _getContent(String emailAddress, String password)
		throws Exception {

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(
				unsyncByteArrayOutputStream)) {

			zipOutputStream.putNextEntry(new ZipEntry("import.json"));

			String json = JSONUtil.putAll(
				JSONUtil.put(
					"emailAddress", emailAddress
				).put(
					"password", password
				)
			).toString();

			zipOutputStream.write(json.getBytes(StandardCharsets.UTF_8));

			zipOutputStream.closeEntry();
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private String _readContent(BatchEngineImportTask batchEngineImportTask)
		throws Exception {

		try (ZipInputStream zipInputStream = new ZipInputStream(
				_batchEngineImportTaskLocalService.openContentInputStream(
					batchEngineImportTask.getBatchEngineImportTaskId()))) {

			ZipEntry zipEntry = zipInputStream.getNextEntry();

			Assert.assertEquals("import.json", zipEntry.getName());

			return StringUtil.read(zipInputStream);
		}
	}

	private void _testUpdateBatchEngineImportTask(
			BatchEngineTaskExecuteStatus batchEngineTaskExecuteStatus)
		throws Exception {

		String emailAddress = RandomTestUtil.randomString() + "@liferay.com";
		String password = RandomTestUtil.randomString();

		BatchEngineImportTask batchEngineImportTask = _addBatchEngineImportTask(
			emailAddress, password);

		batchEngineImportTask = _updateExecuteStatus(
			batchEngineImportTask, BatchEngineTaskExecuteStatus.STARTED);

		String content = _readContent(batchEngineImportTask);

		Assert.assertTrue(content, content.contains(password));

		batchEngineImportTask = _updateExecuteStatus(
			batchEngineImportTask, batchEngineTaskExecuteStatus);

		content = _readContent(batchEngineImportTask);

		Assert.assertTrue(content, content.contains(emailAddress));
		Assert.assertFalse(content, content.contains(password));
	}

	private BatchEngineImportTask _updateExecuteStatus(
		BatchEngineImportTask batchEngineImportTask,
		BatchEngineTaskExecuteStatus batchEngineTaskExecuteStatus) {

		batchEngineImportTask.setExecuteStatus(
			batchEngineTaskExecuteStatus.name());

		return _batchEngineImportTaskLocalService.updateBatchEngineImportTask(
			batchEngineImportTask);
	}

	@Inject
	private BatchEngineImportTaskLocalService
		_batchEngineImportTaskLocalService;

	@DeleteAfterTestRun
	private final List<BatchEngineImportTask> _batchEngineImportTasks =
		new ArrayList<>();

}