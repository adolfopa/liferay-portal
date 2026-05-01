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
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.VIEW_HISTORY,
	service = SystemFDSEntry.class
)
public class ViewVersionHistorySystemFDSEntry implements SystemFDSEntry {

	@Override
	public int getDefaultItemsPerPage() {
		return 20;
	}

	@Override
	public String getDescription() {
		return "CMS Object Entry Version History";
	}

	@Override
	public String getName() {
		return CMSSiteInitializerFDSNames.VIEW_HISTORY;
	}

	@Override
	public String getPropsTransformer() {
		return "{ViewVersionHistoryFDSPropsTransformer} from " +
			"site-cms-site-initializer";
	}

	@Override
	public String getRESTApplication() {
		return "/headless-object/v1.0";
	}

	@Override
	public String getRESTEndpoint() {
		return "/v1.0/object-entries/{objectEntryId}/versions";
	}

	@Override
	public String getRESTSchema() {
		return "ObjectEntryVersion";
	}

	@Override
	public String getSymbol() {
		return "date-time";
	}

	@Override
	public String getTitle() {
		return "Version History";
	}

}