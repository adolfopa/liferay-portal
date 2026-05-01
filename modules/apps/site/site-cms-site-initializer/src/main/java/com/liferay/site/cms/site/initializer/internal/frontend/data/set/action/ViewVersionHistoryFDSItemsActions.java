/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.action;

import com.liferay.frontend.data.set.action.FDSItemsActions;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.VIEW_HISTORY,
	service = FDSItemsActions.class
)
public class ViewVersionHistoryFDSItemsActions implements FDSItemsActions {

	public static List<FDSActionDropdownItem> buildFDSActionDropdownItems(
		HttpServletRequest httpServletRequest, Language language) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return ListUtil.fromArray(
			new FDSActionDropdownItem(
				"{file.link.href}", "download", "download",
				language.get(httpServletRequest, "download"), "get", null,
				"link"),
			new FDSActionDropdownItem(
				StringBundler.concat(
					themeDisplay.getPortalURL(), themeDisplay.getPathMain(),
					GroupConstants.CMS_FRIENDLY_URL,
					"/edit_content_item?objectEntryId={id}",
					"&p_l_mode=read&p_p_state=", LiferayWindowState.POP_UP,
					"&redirect=", themeDisplay.getURLCurrent(),
					"&version={systemProperties.version.number}"),
				"view", "view-content",
				LanguageUtil.get(httpServletRequest, "view"), null, null, null),
			new FDSActionDropdownItem(
				StringPool.BLANK, "view", "view-file",
				language.get(httpServletRequest, "view"), null, null, null),
			new FDSActionDropdownItem(
				"{actions.restore.href}", "restore", "restore",
				language.get(httpServletRequest, "restore-version"), "put",
				"restore", null),
			new FDSActionDropdownItem(
				"{actions.expire.href}", "time", "expire",
				language.get(httpServletRequest, "expire"), "post", "expire",
				null),
			new FDSActionDropdownItem(
				"{actions.copy.href}", "copy", "copy",
				language.get(httpServletRequest, "make-a-copy"), "post", "copy",
				null),
			new FDSActionDropdownItem(
				"{actions.delete.href}", "trash", "delete",
				language.get(httpServletRequest, "delete"), "delete", "delete",
				null));
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
		HttpServletRequest httpServletRequest) {

		return buildFDSActionDropdownItems(httpServletRequest, _language);
	}

	@Reference
	private Language _language;

}