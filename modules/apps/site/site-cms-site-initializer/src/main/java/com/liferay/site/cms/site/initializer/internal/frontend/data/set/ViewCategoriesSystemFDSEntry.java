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
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.CATEGORIES,
	service = SystemFDSEntry.class
)
public class ViewCategoriesSystemFDSEntry implements SystemFDSEntry {

	@Override
	public String getDescription() {
		return "CMS Categories Section";
	}

	@Override
	public String getName() {
		return CMSSiteInitializerFDSNames.CATEGORIES;
	}

	@Override
	public String getPropsTransformer() {
		return "{CategoryFDSPropsTransformer} from site-cms-site-initializer";
	}

	@Override
	public String getRESTApplication() {
		return "/headless-admin-taxonomy/v1.0";
	}

	@Override
	public String getRESTEndpoint() {
		return "/v1.0/taxonomy-vocabularies/{vocabularyId}/taxonomy-categories";
	}

	@Override
	public String getRESTSchema() {
		return "TaxonomyCategory";
	}

	@Override
	public String getSymbol() {
		return "categories";
	}

	@Override
	public String getTitle() {
		return "Categories Section";
	}

}