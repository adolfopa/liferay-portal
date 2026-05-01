/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.action;

import com.liferay.frontend.data.set.action.FDSCreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.VOCABULARIES,
	service = FDSCreationMenu.class
)
public class ViewVocabulariesFDSCreationMenu implements FDSCreationMenu {

	public static CreationMenu buildCreationMenu(
		HttpServletRequest httpServletRequest) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return CreationMenuBuilder.addDropdownItem(
			dropdownItem -> {
				try {
					dropdownItem.setHref(
						PortalUtil.getLayoutFullURL(
							LayoutLocalServiceUtil.getLayoutByFriendlyURL(
								themeDisplay.getScopeGroupId(), false,
								"/categorization/new-vocabulary"),
							themeDisplay));
					dropdownItem.setLabel(
						LanguageUtil.get(httpServletRequest, "new-vocabulary"));
				}
				catch (PortalException portalException) {
					if (_log.isDebugEnabled()) {
						_log.debug(portalException);
					}
				}
			}
		).build();
	}

	@Override
	public CreationMenu getCreationMenu(HttpServletRequest httpServletRequest) {
		return buildCreationMenu(httpServletRequest);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ViewVocabulariesFDSCreationMenu.class);

}