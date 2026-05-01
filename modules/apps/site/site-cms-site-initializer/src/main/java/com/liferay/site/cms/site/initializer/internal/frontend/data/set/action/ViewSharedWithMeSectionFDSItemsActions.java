/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.action;

import com.liferay.frontend.data.set.action.FDSItemsActions;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;
import com.liferay.site.cms.site.initializer.internal.util.ActionUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.SHARED_WITH_ME,
	service = FDSItemsActions.class
)
public class ViewSharedWithMeSectionFDSItemsActions implements FDSItemsActions {

	public static List<FDSActionDropdownItem> buildFDSActionDropdownItems(
		HttpServletRequest httpServletRequest, Portal portal) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return ListUtil.fromArray(
			new FDSActionDropdownItem(
				StringBundler.concat(
					themeDisplay.getPortalURL(), themeDisplay.getPathMain(),
					GroupConstants.CMS_FRIENDLY_URL,
					"/edit_content_item?objectEntryId={classPK}",
					"&p_l_mode=read&p_p_state=", LiferayWindowState.POP_UP,
					"&redirect=", themeDisplay.getURLCurrent()),
				"view", "actionLink",
				LanguageUtil.get(httpServletRequest, "view"), "get", null,
				"modal"),
			new FDSActionDropdownItem(
				null, "share", "share",
				LanguageUtil.get(httpServletRequest, "share"), "get", null,
				"link"),
			new FDSActionDropdownItem(
				StringPool.BLANK, "view", "view-file",
				LanguageUtil.get(httpServletRequest, "view"), null, null, null),
			new FDSActionDropdownItem(
				StringBundler.concat(
					themeDisplay.getPortalURL(), themeDisplay.getPathMain(),
					GroupConstants.CMS_FRIENDLY_URL,
					"/edit_content_item?p_l_mode=read&p_p_state=",
					LiferayWindowState.POP_UP, "&redirect=",
					themeDisplay.getURLCurrent(), "&objectEntryId={classPK}"),
				"view", "view-content",
				LanguageUtil.get(httpServletRequest, "view"), "get", null,
				null),
			new FDSActionDropdownItem(
				StringBundler.concat(
					themeDisplay.getPortalURL(), themeDisplay.getPathMain(),
					GroupConstants.CMS_FRIENDLY_URL,
					"/edit_content_item?objectEntryId={classPK}&redirect=",
					themeDisplay.getURLCurrent()),
				"pencil", "actionLinkEdit",
				LanguageUtil.get(httpServletRequest, "edit"), "get", null,
				null),
			new FDSActionDropdownItem(
				"{file.link.href}", "download", "download",
				LanguageUtil.get(httpServletRequest, "download"), "get", null,
				"link"),
			new FDSActionDropdownItem(
				ActionUtil.getBaseViewFolderURL(themeDisplay) + "{classPK}",
				"view", "actionLinkFolder",
				LanguageUtil.get(httpServletRequest, "view-folder"), "get",
				null, null,
				HashMapBuilder.<String, Object>put(
					"className", ObjectEntryFolder.class.getName()
				).build()),
			new FDSActionDropdownItem(
				StringBundler.concat(
					themeDisplay.getPathFriendlyURLPublic(),
					GroupConstants.CMS_FRIENDLY_URL, "/e/edit-folder/",
					portal.getClassNameId(ObjectEntryFolder.class),
					"/{classPK}?redirect=", themeDisplay.getURLCurrent()),
				"pencil", "edit-folder",
				LanguageUtil.get(httpServletRequest, "edit"), "get", null, null,
				HashMapBuilder.<String, Object>put(
					"className", ObjectEntryFolder.class.getName()
				).build()),
			new FDSActionDropdownItem(
				StringBundler.concat(
					"/o", GroupConstants.CMS_FRIENDLY_URL, "/download-folder/",
					portal.getClassNameId(ObjectEntryFolder.class),
					"/{classPK}"),
				"download", "download-folder",
				LanguageUtil.get(httpServletRequest, "download"), "get", null,
				"link",
				HashMapBuilder.<String, Object>put(
					"className", ObjectEntryFolder.class.getName()
				).build()));
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
		HttpServletRequest httpServletRequest) {

		return buildFDSActionDropdownItems(httpServletRequest, _portal);
	}

	@Reference
	private Portal _portal;

}