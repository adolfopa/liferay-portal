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

package com.liferay.depot.roles.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.RequiredRoleException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alicia García
 */
@RunWith(Arquillian.class)
public class RoleModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test(expected = ModelListenerException.class)
	public void testDeleteSystemAssetLibraryRole() throws Exception {
		_company = CompanyTestUtil.addCompany();

		for (String systemRoleName : RoleConstants.SYSTEM_ASSET_LIBRARY_ROLES) {
			try {
				_roleLocalService.deleteRole(
					_roleLocalService.getRole(
						_company.getCompanyId(), systemRoleName));
				Assert.fail(
					"Allowed to delete default role: " + systemRoleName);
			}
			catch (ModelListenerException modelListenerException) {
				Throwable throwable = modelListenerException.getCause();

				Assert.assertTrue(throwable instanceof RequiredRoleException);

				String message = throwable.getMessage();

				Assert.assertTrue(
					message.contains("is a default asset library role"));

				throw modelListenerException;
			}
		}
	}

	@DeleteAfterTestRun
	private Company _company;

	@Inject
	private RoleLocalService _roleLocalService;

}