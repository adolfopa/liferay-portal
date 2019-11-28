/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.layout.admin.web.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.randomizerbumpers.NumericStringRandomizerBumper;
import com.liferay.portal.kernel.test.randomizerbumpers.UniqueStringRandomizerBumper;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alicia García
 */
@RunWith(Arquillian.class)
public class LayoutsAdminDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	public static Group addGroup(
			long companyId, long userId, long parentGroupId)
		throws Exception {

		String name = RandomTestUtil.randomString(
			NumericStringRandomizerBumper.INSTANCE,
			UniqueStringRandomizerBumper.INSTANCE);

		Group group = GroupLocalServiceUtil.fetchGroup(companyId, name);

		if (group != null) {
			return group;
		}

		Map<Locale, String> nameMap = HashMapBuilder.put(
			LocaleUtil.getDefault(), name
		).build();

		Map<Locale, String> descriptionMap = HashMapBuilder.put(
			LocaleUtil.getDefault(), RandomTestUtil.randomString()
		).build();

		int type = GroupConstants.TYPE_SITE_OPEN;
		String friendlyURL = StringPool.BLANK;
		boolean site = true;
		boolean active = true;
		boolean manualMembership = true;
		int membershipRestriction =
			GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION;

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		serviceContext.setPortalURL("http://localhost:8080");
		serviceContext.setSignedIn(true);

		return GroupLocalServiceUtil.addGroup(
			userId, parentGroupId, null, 0,
			GroupConstants.DEFAULT_LIVE_GROUP_ID, nameMap, descriptionMap, type,
			manualMembership, membershipRestriction, friendlyURL, site, active,
			serviceContext);
	}

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_layout = LayoutTestUtil.addLayout(_group);

		_company = _companyLocalService.getCompany(
			TestPropsValues.getCompanyId());
	}

	@Test
	public void testGetCanonicalLayoutURL() throws Exception {
		Group childGroup = addGroup(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			_group.getParentGroupId());

		LayoutSet publicLayoutSet = childGroup.getPublicLayoutSet();
		Layout layout = LayoutTestUtil.addLayout(childGroup);

		String canonicalURL = PortalUtil.getCanonicalURL(
			"http://localhost:8080/web/" + layout.getName(),
			_getThemeDisplay(childGroup, layout), layout, false, false);

		Assert.assertTrue(canonicalURL.contains(_LOCALHOST));

		GroupTestUtil.deleteGroup(childGroup);
	}

	private ThemeDisplay _getThemeDisplay(Group group, Layout layout)
		throws PortalException {

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(_company);

		themeDisplay.setLanguageId(group.getDefaultLanguageId());
		themeDisplay.setLocale(
			LocaleUtil.fromLanguageId(group.getDefaultLanguageId()));
		themeDisplay.setLayout(layout);

		themeDisplay.setSiteGroupId(group.getGroupId());

		return themeDisplay;
	}

	private static final String _LOCALHOST = "localhost";

	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

	@DeleteAfterTestRun
	private Group _group;

	private Layout _layout;

}