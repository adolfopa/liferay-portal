/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.monitor;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.RandomTestUtil;
import com.liferay.jenkins.results.parser.UrlReader;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Brittney Nguyen
 */
public class ExternalStatusMonitorTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testExecuteComponentStatus() throws Exception {
		_testExecuteComponentStatus(
			"degraded_performance", MonitorResult.Status.WARN);
		_testExecuteComponentStatus(
			"major_outage", MonitorResult.Status.CRITICAL);
		_testExecuteComponentStatus(
			"partial_outage", MonitorResult.Status.CRITICAL);
		_testExecuteComponentStatus(
			"under_maintenance", MonitorResult.Status.WARN);
	}

	@Test
	public void testExecuteDuplicateComponent() throws Exception {
		_testExecuteDuplicateComponent(
			_COMPONENT_NAME_1 + ": degraded_performance",
			MonitorResult.Status.WARN,
			_newStatusPageJSON(
				_newComponentJSONObject(
					_COMPONENT_NAME_1, "degraded_performance"),
				_newComponentJSONObject(_COMPONENT_NAME_1, "under_maintenance"),
				_newComponentJSONObject(_COMPONENT_NAME_2, "operational")));
		_testExecuteDuplicateComponent(
			_COMPONENT_NAME_1 + ": major_outage", MonitorResult.Status.CRITICAL,
			_newStatusPageJSON(
				_newComponentJSONObject(_COMPONENT_NAME_1, "major_outage"),
				_newComponentJSONObject(_COMPONENT_NAME_1, "operational"),
				_newComponentJSONObject(_COMPONENT_NAME_2, "operational")));
		_testExecuteDuplicateComponent(
			_COMPONENT_NAME_1 + ": major_outage", MonitorResult.Status.CRITICAL,
			_newStatusPageJSON(
				_newComponentJSONObject(_COMPONENT_NAME_1, "operational"),
				_newComponentJSONObject(_COMPONENT_NAME_1, "major_outage"),
				_newComponentJSONObject(_COMPONENT_NAME_2, "operational")));
	}

	@Test
	public void testExecuteInvalidJSON() throws Exception {
		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(RandomTestUtil.randomString(), _URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			"Unable to read status page " + _URL, monitorResult.getMessage());
		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());
	}

	@Test
	public void testExecuteMalformedComponent() throws Exception {
		_testExecuteMalformedComponent(
			JenkinsResultsParserUtil.combine(
				_COMPONENT_NAME_1, ": major_outage, ", _COMPONENT_NAME_2,
				": missing"),
			_newStatusPageJSON(
				_newComponentJSONObject(_COMPONENT_NAME_1, "major_outage"),
				_newComponentJSONObject(_COMPONENT_NAME_2, null)));

		JSONObject statusPageJSONObject = new JSONObject(
		).put(
			"components",
			new JSONArray(
			).put(
				RandomTestUtil.randomString()
			).put(
				_newComponentJSONObject(_COMPONENT_NAME_1, "major_outage")
			).put(
				_newComponentJSONObject(_COMPONENT_NAME_2, "operational")
			)
		);

		_testExecuteMalformedComponent(
			_COMPONENT_NAME_1 + ": major_outage",
			statusPageJSONObject.toString());
	}

	@Test
	public void testExecuteMostSevere() throws Exception {
		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(
			_newStatusPageJSON(
				_newComponentJSONObject(_COMPONENT_NAME_2, "major_outage"),
				_newComponentJSONObject(
					_COMPONENT_NAME_1, "degraded_performance")),
			_URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			JenkinsResultsParserUtil.combine(
				"Status page ", _URL, " reports ", _COMPONENT_NAME_1,
				": degraded_performance, ", _COMPONENT_NAME_2,
				": major_outage"),
			monitorResult.getMessage());
		testEquals(MonitorResult.Status.CRITICAL, monitorResult.getStatus());
	}

	@Test
	public void testExecuteNoComponents() throws Exception {
		_testExecuteNoComponents(String.valueOf(new JSONObject()));

		JSONObject statusPageJSONObject = new JSONObject(
		).put(
			"components", RandomTestUtil.randomString()
		);

		_testExecuteNoComponents(statusPageJSONObject.toString());
	}

	@Test
	public void testExecuteOK() throws Exception {
		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(
			_newStatusPageJSON(
				_newComponentJSONObject(_COMPONENT_NAME_1, "operational"),
				_newComponentJSONObject(_COMPONENT_NAME_2, "operational"),
				_newComponentJSONObject(
					RandomTestUtil.randomString(), "major_outage")),
			_URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			JenkinsResultsParserUtil.combine("Status page ", _URL, " is OK"),
			monitorResult.getMessage());
		testEquals(MonitorResult.Status.OK, monitorResult.getStatus());

		verifyUrlReaderRead(false, 0, 13500, urlReader);
	}

	@Test
	public void testExecuteReadFailure() throws Exception {
		UrlReader urlReader = mockUrlReader();

		setUrlReaderException(new IOException(), _URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			"Unable to read status page " + _URL, monitorResult.getMessage());
		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());

		verifyUrlReaderRead(false, 0, 2, 13500, urlReader);
	}

	@Test
	public void testExecuteRetry() throws Exception {
		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutputAfterException(
			new IOException(),
			_newStatusPageJSON(
				_newComponentJSONObject(_COMPONENT_NAME_1, "operational"),
				_newComponentJSONObject(_COMPONENT_NAME_2, "operational")),
			_URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			JenkinsResultsParserUtil.combine("Status page ", _URL, " is OK"),
			monitorResult.getMessage());
		testEquals(MonitorResult.Status.OK, monitorResult.getStatus());

		verifyUrlReaderRead(false, 0, 2, 13500, urlReader);
	}

	@Test
	public void testExecuteUnknownComponentStatus() throws Exception {
		_testExecuteUnknownComponentStatus(
			_newComponentJSONObject(
				_COMPONENT_NAME_2.toUpperCase(), "operational"),
			"missing");

		String componentStatusString = RandomTestUtil.randomString();

		_testExecuteUnknownComponentStatus(
			_newComponentJSONObject(_COMPONENT_NAME_2, componentStatusString),
			componentStatusString);
	}

	@Test
	public void testExecuteUnknownComponentStatusWithOutage() throws Exception {
		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(
			_newStatusPageJSON(
				_newComponentJSONObject(_COMPONENT_NAME_1, "major_outage")),
			_URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			JenkinsResultsParserUtil.combine(
				"Status page ", _URL, " reports ", _COMPONENT_NAME_1,
				": major_outage, ", _COMPONENT_NAME_2, ": missing"),
			monitorResult.getMessage());
		testEquals(MonitorResult.Status.CRITICAL, monitorResult.getStatus());
	}

	@Test
	public void testExternalStatusMonitor() {
		_testExternalStatusMonitorInvalidProperty(
			"monitor[a].parameter[components]", " ");
		_testExternalStatusMonitorInvalidProperty(
			"monitor[a].parameter[components]",
			JenkinsResultsParserUtil.combine(
				_COMPONENT_NAME_1, ",,", _COMPONENT_NAME_2));
		_testExternalStatusMonitorInvalidProperty(
			"monitor[a].parameter[url]",
			"file:///" + RandomTestUtil.randomString());
		_testExternalStatusMonitorInvalidProperty(
			"monitor[a].parameter[url]",
			"http://" + RandomTestUtil.randomString());
		_testExternalStatusMonitorInvalidProperty(
			"monitor[a].parameter[url]", RandomTestUtil.randomString());

		_testExternalStatusMonitorMissingProperty(
			"monitor[a].parameter[components]");
		_testExternalStatusMonitorMissingProperty("monitor[a].parameter[url]");
	}

	private MonitorResult _execute() {
		ExternalStatusMonitor externalStatusMonitor = _newExternalStatusMonitor(
			_newMonitorProperties());

		MonitorResult monitorResult = externalStatusMonitor.execute();

		Map<String, String> metrics = monitorResult.getMetrics();

		Assert.assertTrue(metrics.isEmpty());

		return monitorResult;
	}

	private JSONObject _newComponentJSONObject(String name, String status) {
		return new JSONObject(
		).put(
			"name", name
		).put(
			"status", status
		);
	}

	private ExternalStatusMonitor _newExternalStatusMonitor(
		Properties monitorProperties) {

		List<MonitorConfig> monitorConfigs =
			MonitorConfigLoader.getMonitorConfigs(monitorProperties);

		return new ExternalStatusMonitor(monitorConfigs.get(0));
	}

	private Properties _newMonitorProperties() {
		Properties monitorProperties = new Properties();

		monitorProperties.setProperty(
			"monitor[a].parameter[components]",
			JenkinsResultsParserUtil.combine(
				_COMPONENT_NAME_2, ",", _COMPONENT_NAME_1));
		monitorProperties.setProperty("monitor[a].parameter[url]", _URL);
		monitorProperties.setProperty("monitor[a].type", "external-status");

		return monitorProperties;
	}

	private String _newStatusPageJSON(JSONObject... componentJSONObjects) {
		JSONArray componentsJSONArray = new JSONArray();

		for (JSONObject componentJSONObject : componentJSONObjects) {
			componentsJSONArray.put(componentJSONObject);
		}

		JSONObject statusPageJSONObject = new JSONObject(
		).put(
			"components", componentsJSONArray
		);

		return statusPageJSONObject.toString();
	}

	private void _testExecuteComponentStatus(
			String componentStatusString, MonitorResult.Status status)
		throws Exception {

		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(
			_newStatusPageJSON(
				_newComponentJSONObject(
					_COMPONENT_NAME_1, componentStatusString),
				_newComponentJSONObject(_COMPONENT_NAME_2, "operational")),
			_URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			JenkinsResultsParserUtil.combine(
				"Status page ", _URL, " reports ", _COMPONENT_NAME_1, ": ",
				componentStatusString),
			monitorResult.getMessage());
		testEquals(status, monitorResult.getStatus());
	}

	private void _testExecuteDuplicateComponent(
			String componentMessages, MonitorResult.Status status,
			String statusPageJSON)
		throws Exception {

		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(statusPageJSON, _URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			JenkinsResultsParserUtil.combine(
				"Status page ", _URL, " reports ", componentMessages),
			monitorResult.getMessage());
		testEquals(status, monitorResult.getStatus());
	}

	private void _testExecuteMalformedComponent(
			String componentMessages, String statusPageJSON)
		throws Exception {

		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(statusPageJSON, _URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			JenkinsResultsParserUtil.combine(
				"Status page ", _URL, " reports ", componentMessages),
			monitorResult.getMessage());
		testEquals(MonitorResult.Status.CRITICAL, monitorResult.getStatus());
	}

	private void _testExecuteNoComponents(String statusPageJSON)
		throws Exception {

		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(statusPageJSON, _URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			"Unable to determine the component statuses from status page " +
				_URL,
			monitorResult.getMessage());
		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());
	}

	private void _testExecuteUnknownComponentStatus(
			JSONObject componentJSONObject, String componentStatusString)
		throws Exception {

		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(
			_newStatusPageJSON(
				_newComponentJSONObject(_COMPONENT_NAME_1, "operational"),
				componentJSONObject),
			_URL, urlReader);

		MonitorResult monitorResult = _execute();

		testEquals(
			JenkinsResultsParserUtil.combine(
				"Status page ", _URL, " reports ", _COMPONENT_NAME_2, ": ",
				componentStatusString),
			monitorResult.getMessage());
		testEquals(MonitorResult.Status.UNKNOWN, monitorResult.getStatus());
	}

	private void _testExternalStatusMonitorExpectedIllegalArgumentException(
		Properties monitorProperties) {

		try {
			_newExternalStatusMonitor(monitorProperties);

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
		}
	}

	private void _testExternalStatusMonitorInvalidProperty(
		String name, String value) {

		Properties monitorProperties = _newMonitorProperties();

		monitorProperties.setProperty(name, value);

		_testExternalStatusMonitorExpectedIllegalArgumentException(
			monitorProperties);
	}

	private void _testExternalStatusMonitorMissingProperty(String name) {
		Properties monitorProperties = _newMonitorProperties();

		monitorProperties.remove(name);

		_testExternalStatusMonitorExpectedIllegalArgumentException(
			monitorProperties);
	}

	private static final String _COMPONENT_NAME_1 =
		"1" + RandomTestUtil.randomString();

	private static final String _COMPONENT_NAME_2 =
		"2" + RandomTestUtil.randomString();

	private static final String _URL =
		"https://" + RandomTestUtil.randomString();

}