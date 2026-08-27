/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil.HTTPAuthorization;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author Kenji Heigel
 */
public class JSONArrayBodyURLReader extends BaseBodyURLReader<JSONArray> {

	public static JSONArray read(
			boolean checkCache, HTTPAuthorization httpAuthorization,
			int maxRetries, String postContent, int retryPeriod, int timeout,
			String url)
		throws IOException {

		return _jsonArrayBodyURLReader.doRead(
			checkCache, true, httpAuthorization, null, maxRetries, postContent,
			retryPeriod, timeout, url);
	}

	public static void setInstance(
		JSONArrayBodyURLReader jsonArrayBodyURLReader) {

		_jsonArrayBodyURLReader = jsonArrayBodyURLReader;
	}

	@Override
	protected boolean isTruncationFatal() {
		return true;
	}

	@Override
	protected JSONArray parse(String content) throws IOException {
		try {
			return new JSONArray(content);
		}
		catch (JSONException jsonException) {
			throw new IOException(
				"Unable to create a JSON array from the response body",
				jsonException);
		}
	}

	private static volatile JSONArrayBodyURLReader _jsonArrayBodyURLReader =
		new JSONArrayBodyURLReader();

}