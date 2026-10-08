/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Kenji Heigel
 */
public class BaseURLReaderTest extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testToInputStream() throws Exception {
		mockURLReaders();

		setURLReaderOutput(_STANDARD_OUT, _URL);

		try (InputStream inputStream = JenkinsResultsParserUtil.toInputStream(
				_URL, false)) {

			Assert.assertEquals(
				_STANDARD_OUT,
				JenkinsResultsParserUtil.readInputStream(inputStream));
		}
	}

	@Test
	public void testToJSONArray() throws Exception {
		mockURLReaders();

		JSONArray jsonArray = new JSONArray();

		jsonArray.put("first");
		jsonArray.put("second");

		setURLReaderOutput(String.valueOf(jsonArray), _URL);

		JSONArray readJSONArray = JenkinsResultsParserUtil.toJSONArray(
			_URL, false, _MAX_RETRIES, null, 0, 0);

		Assert.assertEquals("first", readJSONArray.getString(0));
		Assert.assertEquals(2, readJSONArray.length());

		verifyURLReaderAttemptsCount(1, _URL);
	}

	@Test
	public void testToJSONArrayWhenResponseIsMalformed() throws Exception {
		mockURLReaders();

		setURLReaderOutput("not json at all", _URL);

		try {
			JenkinsResultsParserUtil.toJSONArray(
				_URL, false, _MAX_RETRIES, null, 0, 0);

			Assert.fail();
		}
		catch (IOException ioException) {
			Assert.assertEquals(
				"Unable to create a JSON array from the response body",
				ioException.getMessage());
		}

		verifyURLReaderAttemptsCount(_MAX_RETRIES + 1, _URL);
	}

	@Test
	public void testToJSONObject() throws Exception {
		mockURLReaders();

		JSONObject jsonObject = new JSONObject();

		jsonObject.put("id", 7800);

		setURLReaderOutput(String.valueOf(jsonObject), _URL);

		JSONObject readJSONObject = JenkinsResultsParserUtil.toJSONObject(
			_URL, false, _MAX_RETRIES, 0, 0);

		Assert.assertEquals(7800, readJSONObject.getInt("id"));

		verifyURLReaderAttemptsCount(1, _URL);
	}

	@Test
	public void testToJSONObjectWhenResponseCodeIs404() throws Exception {
		mockURLReaders();

		setURLReaderException(new FileNotFoundException(_URL), _URL);

		try {
			JenkinsResultsParserUtil.toJSONObject(
				_URL, false, _MAX_RETRIES, 0, 0);

			Assert.fail();
		}
		catch (FileNotFoundException fileNotFoundException) {
			Assert.assertEquals(_URL, fileNotFoundException.getMessage());
		}

		verifyURLReaderAttemptsCount(1, _URL);
	}

	@Test
	public void testToJSONObjectWhenResponseIsMalformed() throws Exception {
		mockURLReaders();

		setURLReaderOutput("not json at all", _URL);

		try {
			JenkinsResultsParserUtil.toJSONObject(
				_URL, false, _MAX_RETRIES, 0, 0);

			Assert.fail();
		}
		catch (IOException ioException) {
			Assert.assertEquals(
				"Unable to create a JSON object from the response body",
				ioException.getMessage());

			Throwable throwable = ioException.getCause();

			Assert.assertTrue(throwable instanceof JSONException);
		}

		verifyURLReaderAttemptsCount(_MAX_RETRIES + 1, _URL);
	}

	@Test
	public void testToJSONObjectWhenURLIsFileAndAuthorizationIsClientCredentials()
		throws Exception {

		JenkinsMasterTestUtil.getJenkinsCohortProperties("test-9", 1);

		mockURLReaders();

		JSONObject jsonObject = new JSONObject();

		jsonObject.put("id", 7800);

		String url = "file:/tmp/" + RandomTestUtil.randomString() + ".json";

		setURLReaderOutput(String.valueOf(jsonObject), url);

		JSONObject readJSONObject = JenkinsResultsParserUtil.toJSONObject(
			url,
			new JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				new URL("https://test.liferay.com/o/oauth2/token")));

		Assert.assertEquals(7800, readJSONObject.getInt("id"));

		verifyURLReaderAttemptsCount(0, "/o/oauth2/token");
	}

	@Test
	public void testToString() throws Exception {
		mockURLReaders();

		setURLReaderOutput(_STANDARD_OUT, _URL);

		Assert.assertEquals(
			_STANDARD_OUT, JenkinsResultsParserUtil.toString(_URL, false));
	}

	@Test
	public void testToStringWhenConnectionTimesOut() throws Exception {
		mockURLReaders();

		setURLReaderException(
			new SocketTimeoutException("Read timed out"), _URL);

		try {
			JenkinsResultsParserUtil.toString(_URL, false, _MAX_RETRIES, 0, 0);

			Assert.fail();
		}
		catch (SocketTimeoutException socketTimeoutException) {
		}

		verifyURLReaderAttemptsCount(_MAX_RETRIES + 1, _URL);
	}

	@Test
	public void testToStringWhenResponseBodyIsEmpty() throws Exception {
		mockURLReaders();

		setURLReaderOutput("", _URL);

		try {
			JenkinsResultsParserUtil.toString(
				_URL, false, _MAX_RETRIES, 0, 0, true);

			Assert.fail();
		}
		catch (IOException ioException) {
			String message = ioException.getMessage();

			Assert.assertTrue(
				message, message.startsWith("Unable to read a response body"));
		}

		verifyURLReaderAttemptsCount(_MAX_RETRIES + 1, _URL);
	}

	@Test
	public void testToStringWhenResponseBodyIsEmptyAndNotExpected()
		throws Exception {

		mockURLReaders();

		setURLReaderOutput("", _URL);

		Assert.assertEquals(
			"",
			JenkinsResultsParserUtil.toString(
				_URL, false, _MAX_RETRIES, 0, 0, false));

		verifyURLReaderAttemptsCount(1, _URL);
	}

	@Test
	public void testToStringWhenResponseCodeIs403AndURLIsGitHubAPI()
		throws Exception {

		Properties buildProperties = new Properties();

		buildProperties.setProperty(
			"github.access.token", RandomTestUtil.randomString());

		JenkinsResultsParserUtil.setBuildProperties(buildProperties);

		mockURLReaders();

		String url = "https://api.github.com/" + RandomTestUtil.randomString();

		setURLReaderResponseCode(403, url);

		try {
			JenkinsResultsParserUtil.toString(url, false, 0, 0, 0);

			Assert.fail();
		}
		catch (GitHubSecondaryRateLimitRuntimeException
					gitHubSecondaryRateLimitRuntimeException) {
		}

		verifyURLReaderAttemptsCount(1, url);
	}

	@Test
	public void testToStringWhenResponseCodeIs403AndURLIsGitHubAPIWithRetries()
		throws Exception {

		Properties buildProperties = new Properties();

		buildProperties.setProperty(
			"github.access.token", RandomTestUtil.randomString());

		JenkinsResultsParserUtil.setBuildProperties(buildProperties);

		mockURLReaders();

		String url = "https://api.github.com/" + RandomTestUtil.randomString();

		setURLReaderResponseCode(403, url);

		try {
			JenkinsResultsParserUtil.toString(url, false, 3, 5, 0);

			Assert.fail();
		}
		catch (GitHubSecondaryRateLimitRuntimeException
					gitHubSecondaryRateLimitRuntimeException) {
		}

		verifyURLReaderSleepDurations(Arrays.asList(5000L, 25000L, 60000L));
	}

	@Test
	public void testToStringWhenResponseCodeIs404() throws Exception {
		mockURLReaders();

		setURLReaderException(new FileNotFoundException(_URL), _URL);

		try {
			JenkinsResultsParserUtil.toString(_URL, false, _MAX_RETRIES, 0, 0);

			Assert.fail();
		}
		catch (FileNotFoundException fileNotFoundException) {
		}

		verifyURLReaderAttemptsCount(1, _URL);
	}

	@Test
	public void testToStringWhenResponseCodeIs422() throws Exception {
		mockURLReaders();

		setURLReaderResponseCode(422, _URL);

		try {
			JenkinsResultsParserUtil.toString(_URL, false, _MAX_RETRIES, 0, 0);

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			Throwable throwable = runtimeException.getCause();

			Assert.assertTrue(throwable instanceof IOException);
		}

		verifyURLReaderAttemptsCount(1, _URL);
	}

	@Test
	public void testToStringWhenResponseCodeIsRetryable() throws Exception {
		_testToStringWhenResponseCodeIsRetryable(403);
		_testToStringWhenResponseCodeIsRetryable(408);
		_testToStringWhenResponseCodeIsRetryable(429);
		_testToStringWhenResponseCodeIsRetryable(500);
	}

	@Test
	public void testToStringWhenResponseCodeIsTerminal() throws Exception {
		mockURLReaders();

		setURLReaderResponseCode(400, _URL);

		try {
			JenkinsResultsParserUtil.toString(_URL, false, _MAX_RETRIES, 0, 0);

			Assert.fail();
		}
		catch (IOException ioException) {
		}

		verifyURLReaderAttemptsCount(1, _URL);
	}

	@Test
	public void testToStringWhenResponseNeverArrives() throws Exception {
		List<HttpURLConnection> httpURLConnections = new ArrayList<>();

		mockURLReaders();

		for (BaseURLReader<?> baseURLReader : getBaseURLReaders()) {
			Mockito.doAnswer(
				invocation -> {
					HttpURLConnection httpURLConnection = Mockito.mock(
						HttpURLConnection.class);

					Mockito.doThrow(
						new SocketTimeoutException("Read timed out")
					).when(
						httpURLConnection
					).getInputStream();

					httpURLConnections.add(httpURLConnection);

					return httpURLConnection;
				}
			).when(
				baseURLReader
			).openURLConnection(
				Mockito.any(), Mockito.anyBoolean(), Mockito.any(),
				Mockito.any(), Mockito.anyBoolean(), Mockito.anyInt(),
				Mockito.argThat(
					readURL -> (readURL != null) && readURL.contains(_URL))
			);
		}

		try {
			JenkinsResultsParserUtil.toString(_URL, false, _MAX_RETRIES, 0, 0);

			Assert.fail();
		}
		catch (SocketTimeoutException socketTimeoutException) {
		}

		verifyURLReaderAttemptsCount(_MAX_RETRIES + 1, _URL);

		for (HttpURLConnection httpURLConnection : httpURLConnections) {
			Mockito.verify(
				httpURLConnection, Mockito.never()
			).getResponseCode();
		}
	}

	private void _testToStringWhenResponseCodeIsRetryable(int responseCode)
		throws Exception {

		mockURLReaders();

		setURLReaderResponseCode(responseCode, _URL);

		try {
			JenkinsResultsParserUtil.toString(_URL, false, _MAX_RETRIES, 0, 0);

			Assert.fail();
		}
		catch (IOException ioException) {
		}

		verifyURLReaderAttemptsCount(_MAX_RETRIES + 1, _URL);
	}

	private static final int _MAX_RETRIES = 2;

	private static final String _STANDARD_OUT = "Hello, World!\n";

	private static final String _URL = "http://test.liferay.com";

}