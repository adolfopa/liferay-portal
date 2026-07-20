/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.tools.rest.builder.internal.freemarker.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.tools.rest.builder.internal.yaml.config.ConfigYAML;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Adolfo Pérez
 */
public class OpenAPIUtilTest {

	@Test
	public void testFormatPlural() {
		_testFormatPlural(StringPool.BLANK, StringPool.BLANK);
		_testFormatPlural("123", "123");
		_testFormatPlural("batches", "batch");
		_testFormatPlural("boxes", "box");
		_testFormatPlural("boys", "boy");
		_testFormatPlural("buses", "bus");
		_testFormatPlural("cars", "car");
		_testFormatPlural("cars10", "car10");
		_testFormatPlural("categories", "category");
		_testFormatPlural("categories1", "category1");
		_testFormatPlural("days", "day");
		_testFormatPlural("dishes", "dish");
		_testFormatPlural("guys", "guy");
		_testFormatPlural("keys", "key");
		_testFormatPlural("quizes", "quiz");
		_testFormatPlural(null, null);
	}

	@Test
	public void testFormatSingular() {
		_testFormatSingular(1, "clas", "class");
		_testFormatSingular(1, "statu1", "status1");
		_testFormatSingular(6, "class", "class");
		_testFormatSingular(6, "statu", "status");
		_testFormatSingular(15, StringPool.BLANK, StringPool.BLANK);
		_testFormatSingular(15, "123", "123");
		_testFormatSingular(15, "base", "bases");
		_testFormatSingular(15, "box", "boxes");
		_testFormatSingular(15, "bus", "buses");
		_testFormatSingular(15, "car", "car");
		_testFormatSingular(15, "car", "cars");
		_testFormatSingular(15, "car10", "cars10");
		_testFormatSingular(15, "category", "categories");
		_testFormatSingular(15, "category1", "categories1");
		_testFormatSingular(15, "clause", "clauses");
		_testFormatSingular(15, "key", "keys");
		_testFormatSingular(15, "status", "status");
		_testFormatSingular(15, null, null);
	}

	private void _testFormatPlural(String expected, String s) {
		Assert.assertEquals(expected, OpenAPIUtil.formatPlural(s));
	}

	private void _testFormatSingular(
		int compatibilityVersion, String expected, String s) {

		ConfigYAML configYAML = new ConfigYAML();

		configYAML.setCompatibilityVersion(compatibilityVersion);

		Assert.assertEquals(
			expected, OpenAPIUtil.formatSingular(configYAML, s));
	}

}