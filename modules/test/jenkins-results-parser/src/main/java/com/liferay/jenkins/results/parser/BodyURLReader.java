/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil.HTTPAuthorization;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil.HttpRequestMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Kenji Heigel
 */
public class BodyURLReader<T> extends BaseURLReader<T> {

	public static BodyURLReader<JSONArray> newJSONArrayURLReader() {
		return new BodyURLReader<>(BodyURLReader::_toJSONArray, true);
	}

	public static BodyURLReader<JSONObject> newJSONObjectURLReader() {
		return new BodyURLReader<>(BodyURLReader::_toJSONObject, true);
	}

	public static BodyURLReader<String> newTextURLReader() {
		return new BodyURLReader<>(content -> content, false);
	}

	public static JSONArray readJSONArray(
			boolean checkCache, HTTPAuthorization httpAuthorization,
			int maxRetries, String postContent, int retryPeriod, int timeout,
			String url)
		throws IOException {

		return _jsonArrayURLReader.read(
			checkCache, true, httpAuthorization, null, maxRetries, postContent,
			retryPeriod, timeout, url);
	}

	public static JSONObject readJSONObject(
			boolean checkCache, HTTPAuthorization httpAuthorization,
			HttpRequestMethod httpRequestMethod, int maxRetries,
			String postContent, int retryPeriod, int timeout, String url)
		throws IOException {

		return _jsonObjectURLReader.read(
			checkCache, true, httpAuthorization, httpRequestMethod, maxRetries,
			postContent, retryPeriod, timeout, url);
	}

	public static String readText(
			boolean checkCache, boolean expectResponse,
			HTTPAuthorization httpAuthorization,
			HttpRequestMethod httpRequestMethod, int maxRetries,
			String postContent, int retryPeriod, int timeout, String url)
		throws IOException {

		return _textURLReader.read(
			checkCache, expectResponse, httpAuthorization, httpRequestMethod,
			maxRetries, postContent, retryPeriod, timeout, url);
	}

	public static void setJSONArrayInstance(URLReader<JSONArray> urlReader) {
		_jsonArrayURLReader = urlReader;
	}

	public static void setJSONObjectInstance(URLReader<JSONObject> urlReader) {
		_jsonObjectURLReader = urlReader;
	}

	public static void setTextInstance(URLReader<String> urlReader) {
		_textURLReader = urlReader;
	}

	public interface Parser<T> {

		public T parse(String content) throws IOException;

	}

	@Override
	protected T handleCachedFile(File cachedFile) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(
				new FileReader(cachedFile))) {

			return _parseBody(_readBody(bufferedReader), cachedFile.toString());
		}
	}

	@Override
	protected T handleResponse(
			String cacheFileKey, boolean expectResponse,
			URLConnection urlConnection)
		throws IOException {

		String content = null;

		try (InputStream inputStream = urlConnection.getInputStream();

			BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream))) {

			content = _readBody(bufferedReader);
		}

		if (expectResponse && JenkinsResultsParserUtil.isNullOrEmpty(content)) {
			throw new IOException(
				"Unable to read a response body from " +
					urlConnection.getURL());
		}

		if (cacheFileKey != null) {
			JenkinsResultsParserUtil.saveToCacheFile(cacheFileKey, content);
		}

		return _parseBody(content, String.valueOf(urlConnection.getURL()));
	}

	private static JSONArray _toJSONArray(String content) throws IOException {
		try {
			return new JSONArray(content);
		}
		catch (JSONException jsonException) {
			throw new IOException(
				"Unable to create a JSON array from the response body",
				jsonException);
		}
	}

	private static JSONObject _toJSONObject(String content) throws IOException {
		try {
			return new JSONObject(content);
		}
		catch (JSONException jsonException) {
			throw new IOException(
				"Unable to create a JSON object from the response body",
				jsonException);
		}
	}

	private BodyURLReader(Parser<T> parser, boolean truncationFatal) {
		_parser = parser;
		_truncationFatal = truncationFatal;
	}

	private boolean _isTruncated(String content) {
		String trimmedContent = content.trim();

		return trimmedContent.endsWith("was truncated due to its size.");
	}

	private T _parseBody(String content, String source) throws IOException {
		if (_truncationFatal && _isTruncated(content)) {
			throw new TruncatedResponseException(source);
		}

		return _parser.parse(content);
	}

	private String _readBody(BufferedReader bufferedReader) throws IOException {
		StringBuilder sb = new StringBuilder();

		String line = bufferedReader.readLine();

		while (line != null) {
			sb.append(line);
			sb.append("\n");

			line = bufferedReader.readLine();
		}

		return sb.toString();
	}

	private static volatile URLReader<JSONArray> _jsonArrayURLReader =
		newJSONArrayURLReader();
	private static volatile URLReader<JSONObject> _jsonObjectURLReader =
		newJSONObjectURLReader();
	private static volatile URLReader<String> _textURLReader =
		newTextURLReader();

	private final Parser<T> _parser;
	private final boolean _truncationFatal;

}