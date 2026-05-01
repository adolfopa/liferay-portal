/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set;

import com.liferay.frontend.data.set.SystemFDSEntry;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;

import org.osgi.service.component.annotations.Component;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.SHARED_WITH_ME,
	service = SystemFDSEntry.class
)
public class ViewSharedWithMeSectionSystemFDSEntry implements SystemFDSEntry {

	@Override
	public int getDefaultItemsPerPage() {
		return 20;
	}

	@Override
	public String getDescription() {
		return "CMS Shared With Me Section";
	}

	@Override
	public String getName() {
		return CMSSiteInitializerFDSNames.SHARED_WITH_ME;
	}

	@Override
	public String getPropsTransformer() {
		return "{SharedWithMeFDSPropsTransformer} from " +
			"site-cms-site-initializer";
	}

	@Override
	public String getRESTApplication() {
		return "/headless-admin-user/v1.0";
	}

	@Override
	public String getRESTEndpoint() {
		return "/v1.0/my-user-account/shared-assets/shared-with-me";
	}

	@Override
	public String getRESTSchema() {
		return "SharedAsset";
	}

	@Override
	public String getSymbol() {
		return "share";
	}

	@Override
	public String getTitle() {
		return "Shared With Me Section";
	}

}