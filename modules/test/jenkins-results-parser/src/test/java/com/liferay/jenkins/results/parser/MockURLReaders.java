/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.util.Arrays;
import java.util.List;

/**
 * @author Calum Ragan
 */
public class MockURLReaders {

	public MockURLReaders(
		JSONArrayBodyURLReader jsonArrayBodyURLReader,
		JSONObjectBodyURLReader jsonObjectBodyURLReader,
		StreamURLReader streamURLReader, TextBodyURLReader textBodyURLReader) {

		_streamURLReader = streamURLReader;

		_urlReaders = Arrays.asList(
			jsonArrayBodyURLReader, jsonObjectBodyURLReader, streamURLReader,
			textBodyURLReader);
	}

	public StreamURLReader getStreamURLReader() {
		return _streamURLReader;
	}

	public List<URLReader<?>> getURLReaders() {
		return _urlReaders;
	}

	private final StreamURLReader _streamURLReader;
	private final List<URLReader<?>> _urlReaders;

}