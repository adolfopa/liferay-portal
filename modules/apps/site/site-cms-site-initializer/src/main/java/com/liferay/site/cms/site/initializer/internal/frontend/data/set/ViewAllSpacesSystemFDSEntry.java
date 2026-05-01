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
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.ALL_SPACES_SECTION,
	service = SystemFDSEntry.class
)
public class ViewAllSpacesSystemFDSEntry implements SystemFDSEntry {

	@Override
	public int getDefaultItemsPerPage() {
		return 10;
	}

	@Override
	public String getDescription() {
		return "CMS All Spaces Section";
	}

	@Override
	public String getName() {
		return CMSSiteInitializerFDSNames.ALL_SPACES_SECTION;
	}

	@Override
	public String getPropsTransformer() {
		return "{AllSpacesFDSPropsTransformer} from site-cms-site-initializer";
	}

	@Override
	public String getRESTApplication() {
		return "/headless-asset-library/v1.0";
	}

	@Override
	public String getRESTEndpoint() {
		return "/v1.0/asset-libraries";
	}

	@Override
	public String getRESTSchema() {
		return "AssetLibrary";
	}

	@Override
	public String getSymbol() {
		return "asset-library";
	}

	@Override
	public String getTitle() {
		return "All Spaces Section";
	}

}