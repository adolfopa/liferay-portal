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
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.BULK_ACTION_TASK_REPORT_SECTION,
	service = SystemFDSEntry.class
)
public class ViewBulkActionTaskReportSectionSystemFDSEntry
	implements SystemFDSEntry {

	@Override
	public int getDefaultItemsPerPage() {
		return 20;
	}

	@Override
	public String getDescription() {
		return "CMS Bulk Action Task Report Section";
	}

	@Override
	public String getName() {
		return CMSSiteInitializerFDSNames.BULK_ACTION_TASK_REPORT_SECTION;
	}

	@Override
	public String getPropsTransformer() {
		return "{BulkActionTaskReportFDSPropsTransformer} from " +
			"site-cms-site-initializer";
	}

	@Override
	public String getRESTApplication() {
		return "/cms";
	}

	@Override
	public String getRESTEndpoint() {
		return "/bulk-action-tasks";
	}

	@Override
	public String getRESTSchema() {
		return "BulkActionTask";
	}

	@Override
	public String getSymbol() {
		return "list-ul";
	}

	@Override
	public String getTitle() {
		return "Bulk Action Task Report Section";
	}

}