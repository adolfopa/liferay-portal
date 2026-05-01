/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set;

import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.frontend.data.set.SystemFDSEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectDefinitionSettingLocalService;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;
import com.liferay.site.cms.site.initializer.internal.display.context.SectionDisplayContextHelper;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.CONTENTS_SECTION,
	service = SystemFDSEntry.class
)
public class ViewContentsSectionSystemFDSEntry implements SystemFDSEntry {

	@Override
	public String getAdditionalAPIURLParameters(
		HttpServletRequest httpServletRequest) {

		return _sectionDisplayContextHelper.getAdditionalAPIURLParameters(
			_sectionDisplayContextHelper.appendStatus(
				_sectionDisplayContextHelper.appendGroupIds(
					"cmsRoot eq true and cmsSection eq 'contents'",
					httpServletRequest)),
			httpServletRequest, null);
	}

	@Override
	public int getDefaultItemsPerPage() {
		return 20;
	}

	@Override
	public String getDescription() {
		return "CMS Contents Section";
	}

	@Override
	public boolean getHideManagementBarInEmptyState() {
		return true;
	}

	@Override
	public String getName() {
		return CMSSiteInitializerFDSNames.CONTENTS_SECTION;
	}

	@Override
	public String getPropsTransformer() {
		return "{AssetsFilesDropFDSPropsTransformer} from " +
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
		return "documents-and-media";
	}

	@Override
	public String getTitle() {
		return "Contents Section";
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_sectionDisplayContextHelper = new SectionDisplayContextHelper(
			_depotEntryLocalService, _groupLocalService, _language,
			_objectDefinitionSettingLocalService,
			_objectEntryFolderModelResourcePermission, _portal);
	}

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Language _language;

	@Reference
	private ObjectDefinitionSettingLocalService
		_objectDefinitionSettingLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.object.model.ObjectEntryFolder)"
	)
	private ModelResourcePermission<ObjectEntryFolder>
		_objectEntryFolderModelResourcePermission;

	@Reference
	private Portal _portal;

	private SectionDisplayContextHelper _sectionDisplayContextHelper;

}