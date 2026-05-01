/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.site.cms.site.initializer.internal.frontend.data.set.action.ViewStructureUsagesFDSItemsActions;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Marco Galluzzi
 */
public class ViewStructureUsagesDisplayContext {

	public ViewStructureUsagesDisplayContext(
		HttpServletRequest httpServletRequest, Language language,
		ObjectDefinition objectDefinition, Portal portal) {

		_httpServletRequest = httpServletRequest;
		_language = language;
		_objectDefinition = objectDefinition;
		_portal = portal;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIURL() {
		return StringBundler.concat(
			"/o/search/v1.0/search?emptySearch=true&",
			"filter=(folderId ne 0 and objectDefinitionId eq ",
			ParamUtil.getLong(_httpServletRequest, "objectDefinitionId"),
			" and status in (", _STATUSES, "))&nestedFields=embedded");
	}

	public Map<String, Object> getBreadcrumbProps() throws PortalException {
		return HashMapBuilder.<String, Object>put(
			"breadcrumbItems",
			JSONUtil.putAll(
				JSONUtil.put(
					"active", false
				).put(
					"href",
					() -> PortalUtil.getLayoutFullURL(
						LayoutLocalServiceUtil.getLayoutByFriendlyURL(
							_themeDisplay.getScopeGroupId(), false,
							"/structures"),
						_themeDisplay)
				).put(
					"label",
					LanguageUtil.get(_themeDisplay.getLocale(), "structures")
				)
			).put(
				JSONUtil.put(
					"active", true
				).put(
					"label",
					LanguageUtil.format(
						_themeDisplay.getLocale(), "x-usages",
						_objectDefinition.getLabel(
							_themeDisplay.getLanguageId()))
				)
			)
		).put(
			"hideSpace", true
		).build();
	}

	public List<DropdownItem> getBulkActionDropdownItems() {
		return Collections.emptyList();
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems() {
		return ViewStructureUsagesFDSItemsActions.buildFDSActionDropdownItems(
			_httpServletRequest, _language, _portal);
	}

	private static final String _STATUSES = StringUtil.merge(
		new int[] {
			WorkflowConstants.STATUS_APPROVED, WorkflowConstants.STATUS_DRAFT,
			WorkflowConstants.STATUS_EXPIRED, WorkflowConstants.STATUS_PENDING,
			WorkflowConstants.STATUS_SCHEDULED
		});

	private final HttpServletRequest _httpServletRequest;
	private final Language _language;
	private final ObjectDefinition _objectDefinition;
	private final Portal _portal;
	private final ThemeDisplay _themeDisplay;

}