/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.asset.categories.admin.web.constants.AssetCategoriesAdminPortletKeys;
import com.liferay.asset.tags.constants.AssetTagsAdminPortletKeys;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.site.cms.site.initializer.internal.frontend.data.set.action.ViewVocabulariesFDSCreationMenu;
import com.liferay.site.cms.site.initializer.internal.frontend.data.set.action.ViewVocabulariesFDSItemsActions;
import com.liferay.site.cms.site.initializer.internal.util.ExportImportUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Noor Najjar
 */
public class ViewVocabulariesDisplayContext {

	public ViewVocabulariesDisplayContext(
		HttpServletRequest httpServletRequest, ThemeDisplay themeDisplay) {

		_httpServletRequest = httpServletRequest;
		_themeDisplay = themeDisplay;
	}

	public String getAPIURL() {
		return "/o/headless-admin-taxonomy/v1.0/sites/" +
			_themeDisplay.getScopeGroupId() + "/taxonomy-vocabularies";
	}

	public CreationMenu getCreationMenu() {
		return ViewVocabulariesFDSCreationMenu.buildCreationMenu(
			_httpServletRequest);
	}

	public Map<String, Object> getEmptyState() {
		return HashMapBuilder.<String, Object>put(
			"description",
			LanguageUtil.get(
				_httpServletRequest,
				"vocabularies-are-needed-to-create-categories")
		).put(
			"image", "/states/cms_empty_state_categorization.svg"
		).put(
			"title",
			LanguageUtil.get(_httpServletRequest, "no-vocabularies-yet")
		).build();
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems() {
		return ViewVocabulariesFDSItemsActions.buildFDSActionDropdownItems(
			_httpServletRequest);
	}

	public Map<String, Object> getReactData() throws Exception {
		return HashMapBuilder.<String, Object>put(
			"actionItems",
			_putAll(
				ExportImportUtil.getActionItemJSONObject(
					_httpServletRequest, "export-import-vocabularies",
					AssetCategoriesAdminPortletKeys.ASSET_CATEGORIES_ADMIN,
					_themeDisplay),
				ExportImportUtil.getActionItemJSONObject(
					_httpServletRequest, "export-import-tags",
					AssetTagsAdminPortletKeys.ASSET_TAGS_ADMIN, _themeDisplay))
		).put(
			"activeTab", "vocabularies"
		).put(
			"tagsURL",
			PortalUtil.getLayoutFullURL(
				LayoutLocalServiceUtil.getLayoutByFriendlyURL(
					_themeDisplay.getScopeGroupId(), false,
					"/categorization/view-tags"),
				_themeDisplay)
		).put(
			"vocabulariesURL",
			PortalUtil.getLayoutFullURL(
				LayoutLocalServiceUtil.getLayoutByFriendlyURL(
					_themeDisplay.getScopeGroupId(), false,
					"/categorization/view-vocabularies"),
				_themeDisplay)
		).build();
	}

	private JSONArray _putAll(JSONObject... jsonObjects) {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (JSONObject jsonObject : jsonObjects) {
			if (jsonObject != null) {
				jsonArray.put(jsonObject);
			}
		}

		return jsonArray;
	}

	private final HttpServletRequest _httpServletRequest;
	private final ThemeDisplay _themeDisplay;

}