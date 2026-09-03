/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.auto.tagger.opennlp.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cristina González
 * @author Alejandro Tardín
 */
@RunWith(Arquillian.class)
public class JournalArticleOpenNLPDocumentAssetAutoTaggerTest
	extends BaseOpenNLPDocumentAssetAutoTaggerTestCase {

	@Test
	public void testAutoTagsAnAssetReusingNameFinders() throws Exception {
		testWithOpenNLPDocumentAssetAutoTagProviderEnabled(
			getClassName(),
			() -> {
				getAssetEntry(getTaggableText());

				try (LogCapture logCapture =
						LoggerTestUtil.configureLog4JLogger(
							"opennlp.tools.util.XmlUtil",
							LoggerTestUtil.WARN)) {

					AssetEntry assetEntry = getAssetEntry(getTaggableText());

					Assert.assertTrue(
						ArrayUtil.isNotEmpty(assetEntry.getTagNames()));

					List<LogEntry> logEntries = logCapture.getLogEntries();

					Assert.assertEquals(
						logEntries.toString(), 0, logEntries.size());
				}
			});
	}

	@Override
	protected AssetEntry getAssetEntry(String text) throws Exception {
		JournalArticle journalArticle = JournalTestUtil.addArticle(
			group.getGroupId(), RandomTestUtil.randomString(), text);

		return assetEntryLocalService.fetchEntry(
			JournalArticle.class.getName(),
			journalArticle.getResourcePrimKey());
	}

	@Override
	protected String getClassName() {
		return JournalArticle.class.getName();
	}

}