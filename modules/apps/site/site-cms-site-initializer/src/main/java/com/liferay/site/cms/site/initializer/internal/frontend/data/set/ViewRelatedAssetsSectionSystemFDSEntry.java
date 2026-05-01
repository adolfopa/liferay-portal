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
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.RELATED_ASSETS_SECTION,
	service = SystemFDSEntry.class
)
public class ViewRelatedAssetsSectionSystemFDSEntry implements SystemFDSEntry {

	@Override
	public String getDescription() {
		return "CMS Related Assets Section";
	}

	@Override
	public String getName() {
		return CMSSiteInitializerFDSNames.RELATED_ASSETS_SECTION;
	}

	@Override
	public String getPropsTransformer() {
		return "{RelatedAssetsFDSPropsTransformer} from " +
			"site-cms-site-initializer";
	}

	@Override
	public String getRESTApplication() {
		return "/search/v1.0";
	}

	@Override
	public String getRESTEndpoint() {
		return "/v1.0/search";
	}

	@Override
	public String getRESTSchema() {
		return "SearchResult";
	}

	@Override
	public String getSymbol() {
		return "list-ul";
	}

	@Override
	public String getTitle() {
		return "Related Assets Section";
	}

}