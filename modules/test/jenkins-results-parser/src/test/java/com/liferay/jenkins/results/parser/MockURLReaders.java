/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Calum Ragan
 */
public class MockURLReaders {

	public MockURLReaders(
		BodyURLReader<JSONArray> jsonArrayURLReader,
		BodyURLReader<JSONObject> jsonObjectURLReader,
		StreamURLReader streamURLReader, BodyURLReader<String> textURLReader) {

		_streamURLReader = streamURLReader;

		_urlReaders = Arrays.asList(
			jsonArrayURLReader, jsonObjectURLReader, streamURLReader,
			textURLReader);
	}

	public StreamURLReader getStreamURLReader() {
		return _streamURLReader;
	}

	public List<BaseURLReader<?>> getURLReaders() {
		return _urlReaders;
	}

	private final StreamURLReader _streamURLReader;
	private final List<BaseURLReader<?>> _urlReaders;

}