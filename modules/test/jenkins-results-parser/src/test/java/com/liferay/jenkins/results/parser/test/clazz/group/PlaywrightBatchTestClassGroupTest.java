/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.test.clazz.group;

import com.liferay.jenkins.results.parser.AntUtil;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.NotificationUtil;
import com.liferay.jenkins.results.parser.PortalGitWorkingDirectory;
import com.liferay.jenkins.results.parser.RandomTestUtil;
import com.liferay.jenkins.results.parser.ReflectionTestUtil;
import com.liferay.jenkins.results.parser.Shell;
import com.liferay.jenkins.results.parser.test.clazz.PlaywrightTestClassMethod;
import com.liferay.jenkins.results.parser.test.clazz.TestClass;
import com.liferay.jenkins.results.parser.test.clazz.TestClassMethod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Calum Ragan
 */
public class PlaywrightBatchTestClassGroupTest
	extends com.liferay.jenkins.results.parser.Test {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		setShellCommandOutput(
			"git remote -v", mockShell(),
			"upstream\tgit@github.com:liferay/liferay-portal.git (fetch)\n" +
				"upstream\tgit@github.com:liferay/liferay-portal.git (push)\n");

		_workingDirectory = temporaryFolder.newFolder();

		File playwrightDir = new File(
			_workingDirectory, "modules/test/playwright");

		_writeProject(
			playwrightDir, "tests/multiple-database-types-project",
			"multiple-database-types-project",
			"tests/multiple-database-types-project",
			"database.types=\\\n    db2,\\\n    mysql,\\\n    oracle");
		_writeProject(
			playwrightDir, "tests/mysql-project", "mysql-project",
			"tests/mysql-project", "database.types=mysql");
		_writeProject(
			playwrightDir, "tests/no-database-types-project",
			"no-database-types-project", "tests/no-database-types-project",
			"analytics.cloud.enabled=false");
		_writeProject(
			playwrightDir, "tests/no-test-properties-project",
			"no-test-properties-project", "tests/no-test-properties-project",
			null);
		_writeProject(
			playwrightDir, "tests/shared-spec-project", "shared-spec-project",
			"tests/shared", null);
		_writeProject(
			playwrightDir, "tests/unknown-database-type-project",
			"unknown-database-type-project",
			"tests/unknown-database-type-project",
			"database.types=mysq,postgresql");

		JenkinsResultsParserUtil.write(
			new File(playwrightDir, "tests/shared/test.properties"),
			"database.types=mysql,postgresql");
	}

	@Test
	public void testIsDatabaseTypeSupported() throws Exception {
		_testIsDatabaseTypeSupportedDefaultProjects();
		_testIsDatabaseTypeSupportedIncludesWildcard();
		_testIsDatabaseTypeSupportedMultipleDatabaseTypes();
		_testIsDatabaseTypeSupportedNoDatabaseType();
		_testIsDatabaseTypeSupportedSuffixedBatchName();
		_testIsDatabaseTypeSupportedUnknownDatabaseType();
	}

	@Test
	public void testLoadPlaywrightJSONObjects() throws Exception {
		JSONObject reportJSONObject = new JSONObject(
		).put(
			"config",
			new JSONObject(
			).put(
				"rootDir", RandomTestUtil.randomString()
			)
		);

		String reportJSON = reportJSONObject.toString();

		_testLoadPlaywrightJSONObjects(1, false, reportJSONObject, reportJSON);
		_testLoadPlaywrightJSONObjects(
			2, false, reportJSONObject, "", reportJSON);
		_testLoadPlaywrightJSONObjects(
			2, false, reportJSONObject, RandomTestUtil.randomString(),
			reportJSON);
		_testLoadPlaywrightJSONObjects(
			2, false, reportJSONObject, null, reportJSON);

		_testLoadPlaywrightJSONObjects(2, true, new JSONObject(), null, null);
	}

	@Test
	public void testParsePlaywrightJSONObjectsDescribeBlocks() {
		String projectName = RandomTestUtil.randomString();
		File rootDir = new File(RandomTestUtil.randomString());
		String specFilePath = RandomTestUtil.randomString();
		String specTitle = RandomTestUtil.randomString();
		String suiteTitle1 = RandomTestUtil.randomString();
		String suiteTitle2 = RandomTestUtil.randomString();

		Map<String, Map<File, TestClass>> testClassesMaps =
			_parsePlaywrightJSONObjects(
				rootDir,
				new JSONArray(
				).put(
					new JSONObject(
					).put(
						"suites",
						new JSONArray(
						).put(
							_newSuiteJSONObject(
								specFilePath,
								new JSONArray(
								).put(
									_newSpecJSONObject(
										RandomTestUtil.randomString(),
										projectName, specFilePath, specTitle)
								),
								suiteTitle1)
						).put(
							_newSuiteJSONObject(
								specFilePath,
								new JSONArray(
								).put(
									_newSpecJSONObject(
										RandomTestUtil.randomString(),
										projectName, specFilePath, specTitle)
								),
								suiteTitle2)
						)
					)
				));

		Map<File, TestClass> testClassesMap = testClassesMaps.get(projectName);

		Assert.assertEquals(
			Arrays.asList(
				suiteTitle1 + " › " + specTitle,
				suiteTitle2 + " › " + specTitle),
			_getTestNames(testClassesMap.get(new File(rootDir, specFilePath))));
	}

	@Test
	public void testParsePlaywrightJSONObjectsRepeated() {
		String projectName = RandomTestUtil.randomString();
		String specFilePath = RandomTestUtil.randomString();
		String specTitle1 = RandomTestUtil.randomString();
		String specTitle2 = RandomTestUtil.randomString();

		JSONArray suitesJSONArray = new JSONArray(
		).put(
			_newSuiteJSONObject(
				specFilePath,
				new JSONArray(
				).put(
					_newSpecJSONObject(
						RandomTestUtil.randomString(), projectName,
						specFilePath, specTitle1)
				).put(
					_newSpecJSONObject(
						RandomTestUtil.randomString(), projectName,
						specFilePath, specTitle2)
				),
				specFilePath)
		);

		File rootDir = new File(RandomTestUtil.randomString());

		_parsePlaywrightJSONObjects(rootDir, suitesJSONArray);

		Map<String, Map<File, TestClass>> testClassesMaps =
			_parsePlaywrightJSONObjects(rootDir, suitesJSONArray);

		Map<File, TestClass> testClassesMap = testClassesMaps.get(projectName);

		Assert.assertEquals(
			Arrays.asList(specTitle1, specTitle2),
			_getTestNames(testClassesMap.get(new File(rootDir, specFilePath))));
	}

	@Test
	public void testParsePlaywrightJSONObjectsSharedSpec() {
		String projectName1 = RandomTestUtil.randomString();
		String projectName2 = RandomTestUtil.randomString();
		File rootDir = new File(RandomTestUtil.randomString());
		String specFilePath = RandomTestUtil.randomString();
		String specTitle1 = RandomTestUtil.randomString();
		String specTitle2 = RandomTestUtil.randomString();

		Map<String, Map<File, TestClass>> testClassesMaps =
			_parsePlaywrightJSONObjects(
				rootDir,
				new JSONArray(
				).put(
					_newSuiteJSONObject(
						specFilePath,
						new JSONArray(
						).put(
							_newSpecJSONObject(
								"skip", projectName1, specFilePath, specTitle1)
						).put(
							_newSpecJSONObject(
								RandomTestUtil.randomString(), projectName1,
								specFilePath, specTitle2)
						).put(
							_newSpecJSONObject(
								RandomTestUtil.randomString(), projectName2,
								specFilePath, specTitle1)
						),
						specFilePath)
				));

		Map<File, TestClass> testClassesMap1 = testClassesMaps.get(
			projectName1);

		File specFile = new File(rootDir, specFilePath);

		TestClass testClass = testClassesMap1.get(specFile);

		Assert.assertEquals(
			Arrays.asList(specTitle1, specTitle2), _getTestNames(testClass));

		Map<File, TestClass> testClassesMap2 = testClassesMaps.get(
			projectName2);

		Assert.assertSame(testClass, testClassesMap2.get(specFile));

		List<TestClassMethod> testClassMethods =
			testClass.getTestClassMethods();

		TestClassMethod testClassMethod = testClassMethods.get(0);

		Assert.assertTrue(testClassMethod.isIgnored());
	}

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private List<String> _getTestNames(TestClass testClass) {
		List<String> testNames = new ArrayList<>();

		for (TestClassMethod testClassMethod :
				testClass.getTestClassMethods()) {

			PlaywrightTestClassMethod playwrightTestClassMethod =
				(PlaywrightTestClassMethod)testClassMethod;

			testNames.add(playwrightTestClassMethod.getTestName());
		}

		return testNames;
	}

	private PlaywrightBatchTestClassGroup _newPlaywrightBatchTestClassGroup(
		String batchName, Properties jobProperties) {

		jobProperties.setProperty("test.relevant.changes", "false");

		return new PlaywrightBatchTestClassGroup(
			batchName,
			BatchTestClassGroupTestUtil.getPortalTestClassJob(
				jobProperties, Collections.emptyList(), _workingDirectory)) {

			@Override
			protected void setTestClasses() {
			}

		};
	}

	private JSONObject _newSpecJSONObject(
		String annotationType, String projectName, String specFilePath,
		String title) {

		return new JSONObject(
		).put(
			"file", specFilePath
		).put(
			"tests",
			new JSONArray(
			).put(
				new JSONObject(
				).put(
					"annotations",
					new JSONArray(
					).put(
						new JSONObject(
						).put(
							"type", annotationType
						)
					)
				).put(
					"projectName", projectName
				)
			)
		).put(
			"title", title
		);
	}

	private JSONObject _newSuiteJSONObject(
		String specFilePath, JSONArray specsJSONArray, String title) {

		return new JSONObject(
		).put(
			"file", specFilePath
		).put(
			"specs", specsJSONArray
		).put(
			"title", title
		);
	}

	private Map<String, Map<File, TestClass>> _parsePlaywrightJSONObjects(
		File rootDir, JSONArray suitesJSONArray) {

		Map<String, Map<File, TestClass>> testClassesMaps = new HashMap<>();

		PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
			Mockito.mock(PlaywrightBatchTestClassGroup.class);

		Mockito.doReturn(
			Mockito.mock(PortalGitWorkingDirectory.class)
		).when(
			playwrightBatchTestClassGroup
		).getPortalGitWorkingDirectory();

		ReflectionTestUtil.invoke(
			playwrightBatchTestClassGroup, "_parsePlaywrightJSONObjects",
			new Class<?>[] {File.class, JSONArray.class, Map.class}, rootDir,
			suitesJSONArray, testClassesMaps);

		return testClassesMaps;
	}

	private void _testIsDatabaseTypeSupportedDefaultProjects() {
		mockEnvironment(
			Collections.singletonMap(
				"PLAYWRIGHT_PROJECT_NAME",
				"multiple-database-types-project,mysql-project," +
					"no-database-types-project," +
						"no-test-properties-project,shared-spec-project"));

		PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
			_newPlaywrightBatchTestClassGroup(
				"playwright-js-smoke-tomcat101-postgresql163",
				new Properties());

		testEquals(
			false,
			playwrightBatchTestClassGroup.isDatabaseTypeSupported(
				"mysql-project"));
		testEquals(
			true,
			playwrightBatchTestClassGroup.isDatabaseTypeSupported(
				"no-database-types-project"));
		testEquals(
			true,
			playwrightBatchTestClassGroup.isDatabaseTypeSupported(
				"no-test-properties-project"));
		testEquals(
			true,
			playwrightBatchTestClassGroup.isDatabaseTypeSupported(
				"shared-spec-project"));

		PlaywrightBatchTestClassGroup db2PlaywrightBatchTestClassGroup =
			_newPlaywrightBatchTestClassGroup(
				"playwright-js-smoke-tomcat101-db2111", new Properties());

		testEquals(
			false,
			db2PlaywrightBatchTestClassGroup.isDatabaseTypeSupported(
				"shared-spec-project"));
	}

	private void _testIsDatabaseTypeSupportedIncludesWildcard() {
		Properties jobProperties = new Properties();

		jobProperties.setProperty(
			"playwright.projects.includes[playwright-js-upgrade-tomcat101-*]",
			"mysql-project,no-database-types-project");

		PlaywrightBatchTestClassGroup db2PlaywrightBatchTestClassGroup =
			_newPlaywrightBatchTestClassGroup(
				"playwright-js-upgrade-tomcat101-db2111", jobProperties);

		testEquals(
			false,
			db2PlaywrightBatchTestClassGroup.isDatabaseTypeSupported(
				"mysql-project"));
		testEquals(
			true,
			db2PlaywrightBatchTestClassGroup.isDatabaseTypeSupported(
				"no-database-types-project"));

		PlaywrightBatchTestClassGroup mySQLPlaywrightBatchTestClassGroup =
			_newPlaywrightBatchTestClassGroup(
				"playwright-js-upgrade-tomcat101-mysql84", jobProperties);

		testEquals(
			true,
			mySQLPlaywrightBatchTestClassGroup.isDatabaseTypeSupported(
				"mysql-project"));
	}

	private void _testIsDatabaseTypeSupportedMultipleDatabaseTypes() {
		Properties jobProperties = new Properties();

		jobProperties.setProperty(
			"playwright.projects.includes[playwright-js-upgrade-tomcat101-*]",
			"multiple-database-types-project");

		for (String batchName :
				new String[] {
					"playwright-js-upgrade-tomcat101-db2111",
					"playwright-js-upgrade-tomcat101-oracle193"
				}) {

			PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
				_newPlaywrightBatchTestClassGroup(batchName, jobProperties);

			testEquals(
				true,
				playwrightBatchTestClassGroup.isDatabaseTypeSupported(
					"multiple-database-types-project"));
		}

		PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
			_newPlaywrightBatchTestClassGroup(
				"playwright-js-upgrade-tomcat101-postgresql163", jobProperties);

		testEquals(
			false,
			playwrightBatchTestClassGroup.isDatabaseTypeSupported(
				"multiple-database-types-project"));
	}

	private void _testIsDatabaseTypeSupportedNoDatabaseType() {
		mockEnvironment(
			Collections.singletonMap(
				"PLAYWRIGHT_PROJECT_NAME",
				"mysql-project,no-database-types-project"));

		PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
			_newPlaywrightBatchTestClassGroup(
				"playwright-js-tomcat101", new Properties());

		testEquals(
			false,
			playwrightBatchTestClassGroup.isDatabaseTypeSupported(
				"mysql-project"));
		testEquals(
			true,
			playwrightBatchTestClassGroup.isDatabaseTypeSupported(
				"no-database-types-project"));
	}

	private void _testIsDatabaseTypeSupportedSuffixedBatchName() {
		Properties jobProperties = new Properties();

		jobProperties.setProperty(
			"playwright.projects.includes[playwright-js-tomcat101-*]",
			"mysql-project");

		for (String batchName :
				new String[] {
					"playwright-js-tomcat101-mysql84-jdk21_zulu",
					"playwright-js-tomcat101-mysql84_stable"
				}) {

			PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
				_newPlaywrightBatchTestClassGroup(batchName, jobProperties);

			testEquals(
				true,
				playwrightBatchTestClassGroup.isDatabaseTypeSupported(
					"mysql-project"));
		}

		PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
			_newPlaywrightBatchTestClassGroup(
				"playwright-js-tomcat101-postgresql163_stable", jobProperties);

		testEquals(
			false,
			playwrightBatchTestClassGroup.isDatabaseTypeSupported(
				"mysql-project"));
	}

	private void _testIsDatabaseTypeSupportedUnknownDatabaseType() {
		Properties jobProperties = new Properties();

		jobProperties.setProperty(
			"playwright.projects.includes[playwright-js-upgrade-tomcat101-*]",
			"unknown-database-type-project");

		PrintStream printStream = System.err;

		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();

		System.setErr(new PrintStream(byteArrayOutputStream, true));

		try {
			PlaywrightBatchTestClassGroup mySQLPlaywrightBatchTestClassGroup =
				_newPlaywrightBatchTestClassGroup(
					"playwright-js-upgrade-tomcat101-mysql84", jobProperties);

			testEquals(
				false,
				mySQLPlaywrightBatchTestClassGroup.isDatabaseTypeSupported(
					"unknown-database-type-project"));

			PlaywrightBatchTestClassGroup
				postgreSQLPlaywrightBatchTestClassGroup =
					_newPlaywrightBatchTestClassGroup(
						"playwright-js-upgrade-tomcat101-postgresql163",
						jobProperties);

			testEquals(
				true,
				postgreSQLPlaywrightBatchTestClassGroup.isDatabaseTypeSupported(
					"unknown-database-type-project"));
		}
		finally {
			System.setErr(printStream);
		}

		String errorOutput = byteArrayOutputStream.toString();

		testEquals(
			true,
			errorOutput.contains(
				"Ignoring unknown database type mysq in Playwright project " +
					"unknown-database-type-project"));
	}

	private void _testLoadPlaywrightJSONObjects(
			int expectedExecutionRequestsCount, boolean expectedNotified,
			JSONObject expectedPlaywrightJSONObject, String... reports)
		throws Exception {

		AtomicBoolean playwrightJSONObjectsLoaded =
			ReflectionTestUtil.getFieldValue(
				PlaywrightBatchTestClassGroup.class,
				"_playwrightJSONObjectsLoaded");

		playwrightJSONObjectsLoaded.set(false);

		File portalWorkingDirectory = temporaryFolder.newFolder();

		PortalGitWorkingDirectory portalGitWorkingDirectory = Mockito.mock(
			PortalGitWorkingDirectory.class);

		Mockito.doReturn(
			portalWorkingDirectory
		).when(
			portalGitWorkingDirectory
		).getWorkingDirectory();

		List<PlaywrightBatchTestClassGroup> playwrightBatchTestClassGroups =
			new ArrayList<>();

		for (int i = 0; i < 2; i++) {
			PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup =
				Mockito.mock(PlaywrightBatchTestClassGroup.class);

			Mockito.doCallRealMethod(
			).when(
				playwrightBatchTestClassGroup
			).getPlaywrightBaseDir();

			ReflectionTestUtil.setFieldValue(
				playwrightBatchTestClassGroup, "portalGitWorkingDirectory",
				portalGitWorkingDirectory);

			playwrightBatchTestClassGroups.add(playwrightBatchTestClassGroup);
		}

		PlaywrightBatchTestClassGroup firstPlaywrightBatchTestClassGroup =
			playwrightBatchTestClassGroups.get(0);

		JenkinsResultsParserUtil.write(
			new File(
				firstPlaywrightBatchTestClassGroup.getPlaywrightBaseDir(),
				"build.gradle"),
			"task runPlaywright");

		List<Shell.ExecutionRequest> executionRequests = new ArrayList<>();
		Set<File> reportFiles = new HashSet<>();

		Shell.setInstance(
			Mockito.mock(
				Shell.class,
				invocation -> {
					Shell.ExecutionRequest executionRequest =
						invocation.getArgument(0);

					executionRequests.add(executionRequest);

					String[] commands = executionRequest.getCommands();

					Matcher matcher = _playwrightJSONOutputNamePattern.matcher(
						commands[0]);

					Assert.assertTrue(commands[0], matcher.find());

					File reportFile = new File(matcher.group(1));

					reportFiles.add(reportFile);

					String report = reports[executionRequests.size() - 1];

					if (report == null) {
						throw new TimeoutException();
					}

					if (!report.isEmpty()) {
						JenkinsResultsParserUtil.write(reportFile, report);
					}

					return new Shell.ExecutionResult(0, "", "");
				}));

		mockEnvironment(
			Collections.singletonMap(
				"TOP_LEVEL_BUILD_URL",
				JenkinsResultsParserUtil.combine(
					"https://", RandomTestUtil.randomString(), "/job/",
					RandomTestUtil.randomString(), "(release)/",
					String.valueOf(RandomTestUtil.randomInt()))));

		try (MockedStatic<AntUtil> antUtilMockedStatic = Mockito.mockStatic(
				AntUtil.class);
			MockedStatic<JenkinsResultsParserUtil>
				jenkinsResultsParserUtilMockedStatic = Mockito.mockStatic(
					JenkinsResultsParserUtil.class, Mockito.CALLS_REAL_METHODS);
			MockedStatic<NotificationUtil> notificationUtilMockedStatic =
				Mockito.mockStatic(NotificationUtil.class)) {

			jenkinsResultsParserUtilMockedStatic.when(
				() -> JenkinsResultsParserUtil.sleep(Mockito.anyLong())
			).thenAnswer(
				invocation -> null
			);

			for (PlaywrightBatchTestClassGroup playwrightBatchTestClassGroup :
					playwrightBatchTestClassGroups) {

				ReflectionTestUtil.invoke(
					playwrightBatchTestClassGroup, "_loadPlaywrightJSONObjects",
					new Class<?>[0]);
			}

			notificationUtilMockedStatic.verify(
				() -> NotificationUtil.sendSlackNotification(
					Mockito.anyString(), Mockito.anyString(),
					Mockito.anyString(), Mockito.anyString(),
					Mockito.anyString()),
				getVerificationMode(expectedNotified));
		}

		Assert.assertEquals(
			executionRequests.toString(), expectedExecutionRequestsCount,
			executionRequests.size());

		for (Shell.ExecutionRequest executionRequest : executionRequests) {
			Assert.assertEquals(1000 * 60 * 30, executionRequest.getTimeout());
		}

		Assert.assertEquals(
			reportFiles.toString(), executionRequests.size(),
			reportFiles.size());

		String portalWorkingDirectoryPath =
			JenkinsResultsParserUtil.getCanonicalPath(portalWorkingDirectory);

		for (File reportFile : reportFiles) {
			String reportFilePath = JenkinsResultsParserUtil.getCanonicalPath(
				reportFile);

			Assert.assertFalse(reportFilePath, reportFile.exists());
			Assert.assertFalse(
				reportFilePath,
				reportFilePath.startsWith(portalWorkingDirectoryPath));
		}

		JSONObject playwrightJSONObject = ReflectionTestUtil.getFieldValue(
			PlaywrightBatchTestClassGroup.class, "_playwrightJSONObject");

		Assert.assertTrue(
			playwrightJSONObject.toString(),
			expectedPlaywrightJSONObject.similar(playwrightJSONObject));
	}

	private void _writeProject(
			File playwrightDir, String projectDirPath, String projectName,
			String testDirPath, String testProperties)
		throws Exception {

		File projectDir = new File(playwrightDir, projectDirPath);

		JenkinsResultsParserUtil.write(
			new File(projectDir, "config.ts"),
			JenkinsResultsParserUtil.combine(
				"export const config = {\n\tname: '", projectName,
				"',\n\ttestDir: '", testDirPath, "',\n};"));

		if (testProperties != null) {
			JenkinsResultsParserUtil.write(
				new File(projectDir, "test.properties"), testProperties);
		}
	}

	private static final Pattern _playwrightJSONOutputNamePattern =
		Pattern.compile("export PLAYWRIGHT_JSON_OUTPUT_NAME=(.+)");

	private File _workingDirectory;

}