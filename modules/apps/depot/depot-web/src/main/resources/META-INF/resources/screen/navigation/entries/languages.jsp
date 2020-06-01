<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/init.jsp" %>

<%
DepotEntry depotEntry = (DepotEntry)request.getAttribute(DepotAdminWebKeys.DEPOT_ENTRY);

Group group = depotEntry.getGroup();

UnicodeProperties typeSettingsProperties = group.getTypeSettingsProperties();

boolean inheritLocales = GetterUtil.getBoolean(typeSettingsProperties.getProperty(GroupConstants.TYPE_SETTINGS_KEY_INHERIT_LOCALES), true);
%>

<liferay-ui:error exception="<%= DepotEntryNameException.class %>">
	<liferay-ui:message key="asset-library-name-is-required-for-the-default-language" />
</liferay-ui:error>

<liferay-ui:error exception="<%= GroupKeyException.class %>">
	<liferay-ui:message key="please-enter-a-valid-name" />
</liferay-ui:error>

<liferay-frontend:languages
	inheritLocales="<%= inheritLocales %>"
	siteAvailableLocales="<%= DepotLanguageUtil.getDepotAvailableLocales(group) %>"
	siteDefaultLocale="<%= PortalUtil.getSiteDefaultLocale(group.getGroupId()) %>"
	translatedLanguages="<%= group.getNameMap() %>"
/>