/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.action;

import com.liferay.frontend.data.set.action.FDSItemsActions;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.object.constants.ObjectPortletKeys;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.portlet.url.builder.ResourceURLBuilder;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;
import com.liferay.site.cms.site.initializer.internal.util.ActionUtil;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.PortletRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "frontend.data.set.name=" + CMSSiteInitializerFDSNames.STRUCTURES_SECTION,
	service = FDSItemsActions.class
)
public class ViewStructuresFDSItemsActions implements FDSItemsActions {

	public static List<FDSActionDropdownItem> buildFDSActionDropdownItems(
		HttpServletRequest httpServletRequest, Language language,
		Portal portal) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return List.of(
			new FDSActionDropdownItem(
				ActionUtil.getBaseStructureBuilderURL(themeDisplay) +
					"?objectDefinitionId={id}",
				"pencil", "edit", language.get(httpServletRequest, "edit"),
				"get", "update", null),
			new FDSActionDropdownItem(
				ActionUtil.getBaseStructureUsagesURL(themeDisplay) + "{id}",
				"list-ul", "viewUsages",
				language.get(httpServletRequest, "view-usages"), "get", null,
				null),
			new FDSActionDropdownItem(
				ResourceURLBuilder.createResourceURL(
					PortletURLFactoryUtil.create(
						httpServletRequest,
						ObjectPortletKeys.OBJECT_DEFINITIONS,
						PortletRequest.RESOURCE_PHASE)
				).setParameter(
					"objectDefinitionId", "{id}"
				).setResourceID(
					"/object_definitions/export_object_definition"
				).buildString(),
				"export", "export",
				language.get(httpServletRequest, "export-as-json"), "get",
				"exportObjectDefinition", null),
			new FDSActionDropdownItem(
				PortletURLBuilder.create(
					PortletURLFactoryUtil.create(
						httpServletRequest,
						ObjectPortletKeys.OBJECT_DEFINITIONS,
						PortletRequest.ACTION_PHASE)
				).setActionName(
					"/object_definitions/import_object_definition"
				).setParameter(
					"externalReferenceCode", "{externalReferenceCode}"
				).buildString(),
				"import", "import",
				language.get(httpServletRequest, "import-and-override"), "get",
				"update", null),
			new FDSActionDropdownItem(
				PortletURLBuilder.create(
					portal.getControlPanelPortletURL(
						httpServletRequest,
						"com_liferay_portlet_configuration_web_portlet_" +
							"PortletConfigurationPortlet",
						ActionRequest.RENDER_PHASE)
				).setMVCPath(
					"/edit_permissions.jsp"
				).setRedirect(
					themeDisplay.getURLCurrent()
				).setParameter(
					"modelResource", ObjectDefinition.class.getName()
				).setParameter(
					"modelResourceDescription", "{name}"
				).setParameter(
					"resourcePrimKey", "{id}"
				).setWindowState(
					LiferayWindowState.POP_UP
				).buildString(),
				"password-policies", "permissions",
				language.get(httpServletRequest, "permissions"), "get",
				"permissions", "modal-permissions"),
			new FDSActionDropdownItem(
				StringBundler.concat(
					themeDisplay.getPortalURL(), themeDisplay.getPathMain(),
					"/cms/get_object_definition_deletion_info?",
					"objectDefinitionId={id}"),
				"trash", "delete", language.get(httpServletRequest, "delete"),
				"delete", "delete", null, Map.of("system", false)));
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
		HttpServletRequest httpServletRequest) {

		return buildFDSActionDropdownItems(
			httpServletRequest, _language, _portal);
	}

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

}