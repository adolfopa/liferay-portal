/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.action;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.frontend.data.set.action.FDSItemsActions;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemBuilder;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemList;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;
import com.liferay.taglib.security.PermissionsURLTag;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.CATEGORIES,
	service = FDSItemsActions.class
)
public class ViewCategoriesFDSItemsActions implements FDSItemsActions {

	public static List<FDSActionDropdownItem> buildFDSActionDropdownItems(
		HttpServletRequest httpServletRequest) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		long categoryId = ParamUtil.getLong(httpServletRequest, "categoryId");

		try {
			return FDSActionDropdownItemList.of(
				FDSActionDropdownItemBuilder.setFDSActionDropdownItems(
					FDSActionDropdownItemList.of(
						new FDSActionDropdownItem(
							HttpComponentsUtil.addParameters(
								PortalUtil.getLayoutFullURL(
									LayoutLocalServiceUtil.
										getLayoutByFriendlyURL(
											themeDisplay.getScopeGroupId(),
											false,
											"/categorization/edit-category"),
									themeDisplay),
								"categoryId", "{id}", "parentCategoryId",
								categoryId, "vocabularyId",
								"{taxonomyVocabularyId}"),
							"pencil", "edit",
							LanguageUtil.get(httpServletRequest, "edit"), "get",
							"update", null),
						new FDSActionDropdownItem(
							HttpComponentsUtil.addParameters(
								PortalUtil.getLayoutFullURL(
									LayoutLocalServiceUtil.
										getLayoutByFriendlyURL(
											themeDisplay.getScopeGroupId(),
											false,
											"/categorization/edit-category"),
									themeDisplay),
								"parentCategoryId", "{id}", "vocabularyId",
								"{taxonomyVocabularyId}"),
							null, "add-subcategory",
							LanguageUtil.get(
								httpServletRequest, "add-subcategory"),
							"get", "update", null),
						new FDSActionDropdownItem(
							HttpComponentsUtil.addParameters(
								PortalUtil.getLayoutFullURL(
									LayoutLocalServiceUtil.
										getLayoutByFriendlyURL(
											themeDisplay.getScopeGroupId(),
											false,
											"/categorization/view-categories"),
									themeDisplay),
								"categoryId", "{id}", "vocabularyId",
								"{taxonomyVocabularyId}"),
							null, "view-categories",
							LanguageUtil.get(
								httpServletRequest, "view-subcategories"),
							"get", null, null),
						new FDSActionDropdownItem(
							HttpComponentsUtil.addParameter(
								PortalUtil.getLayoutFullURL(
									LayoutLocalServiceUtil.
										getLayoutByFriendlyURL(
											themeDisplay.getScopeGroupId(),
											false,
											"/categorization/view-category-usages"),
									themeDisplay),
								"categoryId", "{id}"),
							"list-ul", "view-category-usages",
							LanguageUtil.get(httpServletRequest, "view-usages"),
							"get", null, null))
				).setSeparator(
					true
				).setType(
					"group"
				).build(
					"add-view"
				),
				FDSActionDropdownItemBuilder.setFDSActionDropdownItems(
					FDSActionDropdownItemList.of(
						new FDSActionDropdownItem(
							null, "move-folder", "move",
							LanguageUtil.get(httpServletRequest, "move"), "get",
							"update", null),
						new FDSActionDropdownItem(
							_getEditPermissionsURL(
								httpServletRequest, themeDisplay),
							"password-policies", "permissions",
							LanguageUtil.get(httpServletRequest, "permissions"),
							"get", null, "modal-permissions"))
				).setSeparator(
					true
				).setType(
					"group"
				).build(
					"permissions"
				),
				FDSActionDropdownItemBuilder.setFDSActionDropdownItems(
					FDSActionDropdownItemList.of(
						new FDSActionDropdownItem(
							null, "trash", "delete",
							LanguageUtil.get(httpServletRequest, "delete"),
							null, "delete", null))
				).setSeparator(
					true
				).setType(
					"group"
				).build(
					"delete"
				));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			return new FDSActionDropdownItemList();
		}
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
		HttpServletRequest httpServletRequest) {

		return buildFDSActionDropdownItems(httpServletRequest);
	}

	private static String _getEditPermissionsURL(
		HttpServletRequest httpServletRequest, ThemeDisplay themeDisplay) {

		try {
			return PermissionsURLTag.doTag(
				themeDisplay.getURLCurrent(), AssetCategory.class.getName(),
				"{name}", GroupConstants.DEFAULT_LIVE_GROUP_ID, "{id}",
				LiferayWindowState.POP_UP.toString(), null, httpServletRequest);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return StringPool.BLANK;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ViewCategoriesFDSItemsActions.class);

}