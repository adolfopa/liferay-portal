/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemBuilder;
import com.liferay.frontend.data.set.model.FDSSortItem;
import com.liferay.frontend.data.set.model.FDSSortItemBuilder;
import com.liferay.frontend.data.set.model.FDSSortItemList;
import com.liferay.frontend.data.set.model.FDSSortItemListBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryVersionLocalServiceUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.frontend.data.set.action.ViewVersionHistoryFDSItemsActions;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Mikel Lorza
 */
public class ViewVersionHistoryDisplayContext {

	public ViewVersionHistoryDisplayContext(
		HttpServletRequest httpServletRequest, Language language,
		ObjectDefinition objectDefinition, ObjectEntry objectEntry) {

		_httpServletRequest = httpServletRequest;
		_language = language;
		_objectDefinition = objectDefinition;
		_objectEntry = objectEntry;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIURL() throws PortalException {
		return StringBundler.concat(
			"/o", _objectDefinition.getRESTContextPath(), "/scopes/",
			_objectEntry.getGroupId(), "/by-external-reference-code/",
			_objectEntry.getExternalReferenceCode(),
			"/versions?nestedFields=file.thumbnailURL");
	}

	public List<DropdownItem> getBulkActionDropdownItems() {
		return ListUtil.fromArray(
			FDSActionDropdownItemBuilder.setHighlighted(
				true
			).setHref(
				"#"
			).setIcon(
				"time"
			).setLabel(
				LanguageUtil.get(_httpServletRequest, "expire")
			).build(
				"expire"
			),
			FDSActionDropdownItemBuilder.setHighlighted(
				true
			).setHref(
				"#"
			).setIcon(
				"trash"
			).setLabel(
				LanguageUtil.get(_httpServletRequest, "delete")
			).build(
				"delete"
			));
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems() {
		return ViewVersionHistoryFDSItemsActions.buildFDSActionDropdownItems(
			_httpServletRequest, _language);
	}

	public FDSSortItemList getFDSSortItemList() {
		return FDSSortItemListBuilder.add(
			_getFDSSortItem(false, "asc", "dateModified", "modified")
		).add(
			_getFDSSortItem(false, "asc", "title", "title")
		).add(
			_getFDSSortItem(true, "desc", "version", "version")
		).build();
	}

	public Map<String, Object> getProps() throws PortalException {
		return HashMapBuilder.<String, Object>put(
			"backURL", ParamUtil.getString(_httpServletRequest, "backURL")
		).put(
			"className", ObjectEntry.class.getName()
		).put(
			"classPK", _objectEntry.getObjectEntryId()
		).put(
			"entryClassName", _objectDefinition.getClassName()
		).put(
			"objectEntryCurrentVersion", _objectEntry.getVersion()
		).put(
			"objectEntryTitle",
			HtmlUtil.escape(
				_objectEntry.getTitleValue(_themeDisplay.getLanguageId()))
		).put(
			"objectEntryVersionsCount",
			ObjectEntryVersionLocalServiceUtil.getObjectEntryVersionsCount(
				_objectEntry.getObjectEntryId())
		).put(
			"title",
			StringBundler.concat(
				StringPool.QUOTE,
				_objectEntry.getTitleValue(_themeDisplay.getLanguageId(), true),
				"\" ", _language.get(_themeDisplay.getLocale(), "history"))
		).build();
	}

	private FDSSortItem _getFDSSortItem(
		boolean active, String direction, String key, String labelKey) {

		return FDSSortItemBuilder.setActive(
			active
		).setDirection(
			direction
		).setKey(
			key
		).setLabel(
			_language.get(_themeDisplay.getLocale(), labelKey)
		).build();
	}

	private final HttpServletRequest _httpServletRequest;
	private final Language _language;
	private final ObjectDefinition _objectDefinition;
	private final ObjectEntry _objectEntry;
	private final ThemeDisplay _themeDisplay;

}