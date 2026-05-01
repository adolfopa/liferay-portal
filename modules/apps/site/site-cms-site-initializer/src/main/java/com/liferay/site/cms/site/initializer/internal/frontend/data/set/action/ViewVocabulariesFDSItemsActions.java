/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.action;

import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.frontend.data.set.action.FDSItemsActions;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemBuilder;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemList;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;
import com.liferay.taglib.security.PermissionsURLTag;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.VOCABULARIES,
	service = FDSItemsActions.class
)
public class ViewVocabulariesFDSItemsActions implements FDSItemsActions {

	public static List<FDSActionDropdownItem> buildFDSActionDropdownItems(
		HttpServletRequest httpServletRequest, Language language,
		LayoutLocalService layoutLocalService, Portal portal) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		try {
			String fullLayoutURL = portal.getLayoutFullURL(
				layoutLocalService.getLayoutByFriendlyURL(
					themeDisplay.getScopeGroupId(), false,
					"/categorization/edit-vocabulary"),
				themeDisplay);

			return FDSActionDropdownItemList.of(
				FDSActionDropdownItemBuilder.setFDSActionDropdownItems(
					FDSActionDropdownItemList.of(
						new FDSActionDropdownItem(
							fullLayoutURL + "?vocabularyId={id}", "pencil",
							"edit", language.get(httpServletRequest, "edit"),
							"get", "update", null),
						new FDSActionDropdownItem(
							HttpComponentsUtil.addParameter(
								portal.getLayoutFullURL(
									layoutLocalService.getLayoutByFriendlyURL(
										themeDisplay.getScopeGroupId(), false,
										"/categorization/new-category"),
									themeDisplay),
								"vocabularyId", "{id}"),
							null, "add-category",
							language.get(httpServletRequest, "add-category"),
							"get", "update", null),
						new FDSActionDropdownItem(
							HttpComponentsUtil.addParameter(
								portal.getLayoutFullURL(
									layoutLocalService.getLayoutByFriendlyURL(
										themeDisplay.getScopeGroupId(), false,
										"/categorization/view-categories"),
									themeDisplay),
								"vocabularyId", "{id}"),
							null, "view-categories",
							language.get(httpServletRequest, "view-categories"),
							"get", null, null))
				).setSeparator(
					true
				).setType(
					"group"
				).build(
					"view-edit"
				),
				FDSActionDropdownItemBuilder.setFDSActionDropdownItems(
					FDSActionDropdownItemList.of(
						new FDSActionDropdownItem(
							_getEditPermissionsURL(
								httpServletRequest, themeDisplay),
							"password-policies", "permissions",
							language.get(httpServletRequest, "permissions"),
							"get", null, "modal-permissions"),
						new FDSActionDropdownItem(
							null, "trash", "delete",
							language.get(httpServletRequest, "delete"), null,
							"delete", null))
				).setSeparator(
					true
				).setType(
					"group"
				).build(
					"delete-permissions"
				));
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}

			return new FDSActionDropdownItemList();
		}
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
		HttpServletRequest httpServletRequest) {

		return buildFDSActionDropdownItems(
			httpServletRequest, _language, _layoutLocalService, _portal);
	}

	private static String _getEditPermissionsURL(
		HttpServletRequest httpServletRequest, ThemeDisplay themeDisplay) {

		try {
			return PermissionsURLTag.doTag(
				themeDisplay.getURLCurrent(), AssetVocabulary.class.getName(),
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
		ViewVocabulariesFDSItemsActions.class);

	@Reference
	private Language _language;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private Portal _portal;

}