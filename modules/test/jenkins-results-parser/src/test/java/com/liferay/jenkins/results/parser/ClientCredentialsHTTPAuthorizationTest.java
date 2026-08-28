/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.net.URL;

import java.util.Date;

import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Calum Ragan
 */
public class ClientCredentialsHTTPAuthorizationTest
	extends com.liferay.jenkins.results.parser.Test {

	@Before
	public void setUpBuildProperties() {
		JenkinsMasterTestUtil.getJenkinsCohortProperties("test-9", 1);
	}

	@Test
	public void testInvalidateToken() throws Exception {
		MockURLReaders mockURLReaders = _mockTokenRequestURLReader();

		JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization
			clientCredentialsHTTPAuthorization =
				_newClientCredentialsHTTPAuthorization();

		String authorization = clientCredentialsHTTPAuthorization.toString();

		clientCredentialsHTTPAuthorization.invalidateToken(authorization);

		Assert.assertNotEquals(
			authorization, clientCredentialsHTTPAuthorization.toString());

		_verifyTokenRequestCount(2, mockURLReaders);
	}

	@Test
	public void testInvalidateTokenWhenAuthorizationIsStale() throws Exception {
		MockURLReaders mockURLReaders = _mockTokenRequestURLReader();

		JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization
			clientCredentialsHTTPAuthorization =
				_newClientCredentialsHTTPAuthorization();

		String authorization = clientCredentialsHTTPAuthorization.toString();

		clientCredentialsHTTPAuthorization.invalidateToken(authorization);

		String newAuthorization = clientCredentialsHTTPAuthorization.toString();

		clientCredentialsHTTPAuthorization.invalidateToken(authorization);

		Assert.assertEquals(
			newAuthorization, clientCredentialsHTTPAuthorization.toString());

		_verifyTokenRequestCount(2, mockURLReaders);
	}

	@Test
	public void testToStringCachesToken() throws Exception {
		MockURLReaders mockURLReaders = _mockTokenRequestURLReader();

		JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization
			clientCredentialsHTTPAuthorization =
				_newClientCredentialsHTTPAuthorization();

		String authorization = clientCredentialsHTTPAuthorization.toString();

		Assert.assertEquals(
			authorization, clientCredentialsHTTPAuthorization.toString());

		_verifyTokenRequestCount(1, mockURLReaders);
	}

	@Test
	public void testToStringRefreshesExpiredToken() throws Exception {
		MockURLReaders mockURLReaders = _mockTokenRequestURLReader();

		JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization
			clientCredentialsHTTPAuthorization =
				_newClientCredentialsHTTPAuthorization();

		String authorization = clientCredentialsHTTPAuthorization.toString();

		ReflectionTestUtil.setFieldValue(
			clientCredentialsHTTPAuthorization, "_tokenExpirationDate",
			new Date(System.currentTimeMillis() - 1000));

		Assert.assertNotEquals(
			authorization, clientCredentialsHTTPAuthorization.toString());

		_verifyTokenRequestCount(2, mockURLReaders);
	}

	private MockURLReaders _mockTokenRequestURLReader() throws Exception {
		MockURLReaders mockURLReaders = mockURLReaders();

		for (BaseURLReader<?> urlReader : mockURLReaders.getURLReaders()) {
			Mockito.doAnswer(
				invocation -> mockURLConnection(
					200,
					String.valueOf(
						new JSONObject(
						).put(
							"access_token", RandomTestUtil.randomString()
						).put(
							"expires_in", 600
						).put(
							"token_type", "Bearer"
						)))
			).when(
				urlReader
			).openURLConnection(
				Mockito.any(), Mockito.anyBoolean(), Mockito.any(),
				Mockito.any(), Mockito.anyBoolean(), Mockito.anyInt(),
				Mockito.contains("/o/oauth2/token")
			);
		}

		return mockURLReaders;
	}

	private JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization
			_newClientCredentialsHTTPAuthorization()
		throws Exception {

		return new JenkinsResultsParserUtil.ClientCredentialsHTTPAuthorization(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			new URL(
				"https://" + RandomTestUtil.randomString() +
					".liferay.com/o/oauth2/token"));
	}

	private void _verifyTokenRequestCount(
		int expectedCount, MockURLReaders mockURLReaders) {

		verifyURLReaderAttemptCount(
			expectedCount, mockURLReaders, "/o/oauth2/token");
	}

}