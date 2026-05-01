/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR
 * LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.frontend.data.set.action.ViewCategoriesFDSCreationMenu;
import com.liferay.site.cms.site.initializer.internal.frontend.data.set.action.ViewCategoriesFDSItemsActions;
import com.liferay.site.cms.site.initializer.internal.util.CategorizationBreadcrumbUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Cheryl Tang
 */
public class ViewCategoriesDisplayContext {

	public ViewCategoriesDisplayContext(
		AssetVocabularyLocalService assetVocabularyLocalService,
		HttpServletRequest httpServletRequest,
		LayoutLocalService layoutLocalService, Language language,
		Portal portal) {

		_assetVocabularyLocalService = assetVocabularyLocalService;
		_httpServletRequest = httpServletRequest;
		_layoutLocalService = layoutLocalService;
		_language = language;
		_portal = portal;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIURL() {
		if (getCategoryId() == 0) {
			return getCategoriesByVocabularyIdAPIURL();
		}

		return getCategoriesByCategoryIdAPIURL();
	}

	public Map<String, Object> getBreadcrumbProps() throws Exception {
		return HashMapBuilder.<String, Object>put(
			"breadcrumbItems",
			CategorizationBreadcrumbUtil.getNavigationBreadcrumbsJSONArray(
				getVocabularyId(), getCategoryId(), _themeDisplay)
		).put(
			"hideSpace", true
		).build();
	}

	public String getCategoriesByCategoryIdAPIURL() {
		return StringBundler.concat(
			"/o/headless-admin-taxonomy/v1.0/taxonomy-categories/",
			getCategoryId(),
			"/taxonomy-categories?nestedFields=taxonomyCategoryUsageCount");
	}

	public String getCategoriesByVocabularyIdAPIURL() {
		return StringBundler.concat(
			"/o/headless-admin-taxonomy/v1.0/taxonomy-vocabularies/",
			getVocabularyId(), "/taxonomy-categories");
	}

	public long getCategoryId() {
		if (_categoryId != null) {
			return _categoryId;
		}

		_categoryId = ParamUtil.getLong(_httpServletRequest, "categoryId");

		return _categoryId;
	}

	public CreationMenu getCreationMenu() {
		return ViewCategoriesFDSCreationMenu.buildCreationMenu(
			_httpServletRequest, _language, _layoutLocalService, _portal);
	}

	public Map<String, Object> getEmptyState() {
		return HashMapBuilder.<String, Object>put(
			"description",
			_language.get(
				_httpServletRequest, "click-new-to-create-your-first-category")
		).put(
			"image", "/states/cms_empty_state_categorization.svg"
		).put(
			"title", _language.get(_httpServletRequest, "no-categories-yet")
		).build();
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems() {
		return ViewCategoriesFDSItemsActions.buildFDSActionDropdownItems(
			_httpServletRequest, _language, _layoutLocalService, _portal);
	}

	public long getVocabularyId() {
		if (_vocabularyId != null) {
			return _vocabularyId;
		}

		_vocabularyId = ParamUtil.getLong(_httpServletRequest, "vocabularyId");

		return _vocabularyId;
	}

	private final AssetVocabularyLocalService _assetVocabularyLocalService;
	private Long _categoryId;
	private final HttpServletRequest _httpServletRequest;
	private final Language _language;
	private final LayoutLocalService _layoutLocalService;
	private final Portal _portal;
	private final ThemeDisplay _themeDisplay;
	private Long _vocabularyId;

}