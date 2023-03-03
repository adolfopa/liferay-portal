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

<%@ include file="/document_library/init.jsp" %>

<%
String eventName = ParamUtil.getString(request, "eventName", liferayPortletResponse.getNamespace() + "FilterByExtensions");
%>

<section class="h-100">
	<div class="align-items-center d-flex justify-content-center min-vh-100">
		<span aria-hidden="true" class="loading-animation"></span>
	</div>

	<react:component
		module="document_library/js/FilterExtensions"
		props='<%=
			HashMapBuilder.<String, Object>put(
				"itemSelectorSaveEvent", eventName
			).build()
		%>'
	/>
</section>