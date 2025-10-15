/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.util;

import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.portal.kernel.util.StringUtil;

import jakarta.ws.rs.InternalServerErrorException;

/**
 * @author Alejandro Tardín
 */
public class GroupUtil {

	public static Long getGroupId(
		long companyId, String scope, String scopeKey) {

		if (StringUtil.equals(scope, ObjectDefinitionConstants.SCOPE_DEPOT)) {
			return com.liferay.portal.vulcan.util.GroupUtil.getDepotGroupId(
				scopeKey, companyId);
		}
		else if (StringUtil.equals(
					scope, ObjectDefinitionConstants.SCOPE_SITE)) {

			return com.liferay.portal.vulcan.util.GroupUtil.getGroupId(
				companyId, scopeKey);
		}

		throw new InternalServerErrorException("Unexpected scopeKey parameter");
	}

}