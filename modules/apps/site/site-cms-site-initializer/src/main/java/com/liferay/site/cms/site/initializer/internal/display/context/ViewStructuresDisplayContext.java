/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.constants.ObjectPortletKeys;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.frontend.data.set.action.ViewStructuresFDSCreationMenu;
import com.liferay.site.cms.site.initializer.internal.frontend.data.set.action.ViewStructuresFDSItemsActions;
import com.liferay.site.cms.site.initializer.internal.util.ExportImportUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Sam Ziemer
 */
public class ViewStructuresDisplayContext {

	public ViewStructuresDisplayContext(
		HttpServletRequest httpServletRequest, Language language,
		Portal portal) {

		_httpServletRequest = httpServletRequest;
		_language = language;
		_portal = portal;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIURL() {
		return StringBundler.concat(
			"/o/object-admin/v1.0/object-definitions?filter=",
			"(objectFolderExternalReferenceCode eq '",
			ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENT_STRUCTURES,
			"' or objectFolderExternalReferenceCode eq '",
			ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_FILE_TYPES, "')");
	}

	public Map<String, Object> getBreadcrumbProps() {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		_addBreadcrumbItem(jsonArray, false, null, _getLayoutName());

		return HashMapBuilder.<String, Object>put(
			"actionItems",
			JSONUtil.putAll(
				ExportImportUtil.getExportActionItemJSONObject(
					_httpServletRequest, ObjectPortletKeys.OBJECT_DEFINITIONS,
					"export-content-structures", _themeDisplay),
				ExportImportUtil.getImportActionItemJSONObject(
					_httpServletRequest, ObjectPortletKeys.OBJECT_DEFINITIONS,
					"import-content-structures", _themeDisplay))
		).put(
			"breadcrumbItems", jsonArray
		).put(
			"hideSpace", true
		).build();
	}

	public List<DropdownItem> getBulkActionDropdownItems() {
		return List.of(
			FDSActionDropdownItemBuilder.setHighlighted(
				true
			).setHref(
				"#"
			).setIcon(
				"workflow"
			).setLabel(
				_language.get(_httpServletRequest, "assign-default-workflow")
			).setModalSize(
				"lg"
			).setTarget(
				"modal"
			).build(
				"assign-default-workflow"
			));
	}

	public CreationMenu getCreationMenu() {
		return ViewStructuresFDSCreationMenu.buildCreationMenu(
			_httpServletRequest, _language);
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems() {
		return ViewStructuresFDSItemsActions.buildFDSActionDropdownItems(
			_httpServletRequest, _language, _portal);
	}

	private void _addBreadcrumbItem(
		JSONArray jsonArray, boolean active, String friendlyURL, String label) {

		jsonArray.put(
			JSONUtil.put(
				"active", active
			).put(
				"href", friendlyURL
			).put(
				"label", label
			));
	}

	private String _getLayoutName() {
		Layout layout = _themeDisplay.getLayout();

		if (layout == null) {
			return null;
		}

		return layout.getName(_themeDisplay.getLocale(), true);
	}

	private final HttpServletRequest _httpServletRequest;
	private final Language _language;
	private final Portal _portal;
	private final ThemeDisplay _themeDisplay;

}