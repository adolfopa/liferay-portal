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
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.VOCABULARIES,
	service = SystemFDSEntry.class
)
public class ViewVocabulariesSystemFDSEntry implements SystemFDSEntry {

	@Override
	public String getDescription() {
		return "CMS Vocabularies Section";
	}

	@Override
	public String getName() {
		return CMSSiteInitializerFDSNames.VOCABULARIES;
	}

	@Override
	public String getPropsTransformer() {
		return "{VocabularyFDSPropsTransformer} from site-cms-site-initializer";
	}

	@Override
	public String getRESTApplication() {
		return "/headless-admin-taxonomy/v1.0";
	}

	@Override
	public String getRESTEndpoint() {
		return "/v1.0/sites/{siteId}/taxonomy-vocabularies";
	}

	@Override
	public String getRESTSchema() {
		return "TaxonomyVocabulary";
	}

	@Override
	public String getSymbol() {
		return "tag";
	}

	@Override
	public String getTitle() {
		return "Vocabularies Section";
	}

}