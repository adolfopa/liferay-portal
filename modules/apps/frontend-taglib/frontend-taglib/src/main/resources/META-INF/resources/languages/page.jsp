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

<%@ include file="/languages/init.jsp" %>

<liferay-ui:error exception="<%= LocaleException.class %>">

	<%
	LocaleException le = (LocaleException)errorException;
	%>

	<c:choose>
		<c:when test="<%= le.getType() == LocaleException.TYPE_DEFAULT %>">
			<liferay-ui:message key="you-cannot-remove-a-language-that-is-the-current-default-language" />
		</c:when>
		<c:when test="<%= le.getType() == LocaleException.TYPE_DISPLAY_SETTINGS %>">
			<liferay-ui:message arguments='<%= "<em>" + StringUtil.merge(LocaleUtil.toDisplayNames(le.getSourceAvailableLocales(), locale), StringPool.COMMA_AND_SPACE) + "</em>" %>' key="please-select-the-available-languages-of-the-asset-library-among-the-available-languages-of-the-portal-x" translateArguments="<%= false %>" />
		</c:when>
	</c:choose>
</liferay-ui:error>

<liferay-ui:error exception="<%= DuplicateGroupException.class %>">
	<liferay-ui:message key="there-is-already-a-workspace-with-the-same-name-in-the-selected-default-language.-please-enter-a-unique-name" />
</liferay-ui:error>

<div class="languages">
	<react:component
		data='<%= (Map<String, Object>)request.getAttribute("liferay-frontend:languages:data") %>'
		module="languages/components/Languages.es"
	/>
</div>