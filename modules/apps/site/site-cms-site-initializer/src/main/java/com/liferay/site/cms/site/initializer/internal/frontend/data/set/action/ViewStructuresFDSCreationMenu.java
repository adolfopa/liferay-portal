/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.action;

import com.liferay.frontend.data.set.action.FDSCreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;
import com.liferay.site.cms.site.initializer.internal.util.ActionUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.STRUCTURES_SECTION,
	service = FDSCreationMenu.class
)
public class ViewStructuresFDSCreationMenu implements FDSCreationMenu {

	public static CreationMenu buildCreationMenu(
		HttpServletRequest httpServletRequest, Language language) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return CreationMenuBuilder.addPrimaryDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					ActionUtil.getBaseStructureBuilderURL(themeDisplay) +
						"?objectFolderExternalReferenceCode=" +
							ObjectFolderConstants.
								EXTERNAL_REFERENCE_CODE_CONTENT_STRUCTURES);
				dropdownItem.setLabel(
					language.get(httpServletRequest, "content"));
			}
		).addPrimaryDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					ActionUtil.getBaseStructureBuilderURL(themeDisplay) +
						"?objectFolderExternalReferenceCode=" +
							ObjectFolderConstants.
								EXTERNAL_REFERENCE_CODE_FILE_TYPES);
				dropdownItem.setLabel(language.get(httpServletRequest, "file"));
			}
		).build();
	}

	@Override
	public CreationMenu getCreationMenu(HttpServletRequest httpServletRequest) {
		return buildCreationMenu(httpServletRequest, _language);
	}

	@Reference
	private Language _language;

}