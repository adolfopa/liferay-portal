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

<%@ include file="/asset_tags_selector/init.jsp" %>

<%
Map<String, Object> data = (Map<String, Object>)request.getAttribute("liferay-asset:asset-tags-selector:data");
List<String> selectedItems = (List<String>)data.get("selectedItems");
String inputName = (String)data.get("inputName");
%>

<div>
	<div class="lfr-tags-selector-content">

		<%
		for (String selectedItem : selectedItems) {
		%>

			<input name="<%= inputName %>" type="hidden" value="<%= selectedItem %>" />

		<%
		}
		%>

		<div class="form-group">
			<label>
				<liferay-ui:message key="tags" />
			</label>

			<div class="input-group input-group-stacked-sm-down">
				<div class="input-group-item">
					<div class="form-control form-control-tag-group">

						<%
						for (String selectedItem : selectedItems) {
						%>

							<clay:label
								closeable="<%= true %>"
								label="<%= selectedItem %>"
							/>

						<%
						}
						%>

						<input class="form-control-inset" type="text" value="" />
					</div>
				</div>
			</div>
		</div>

		<button class="btn btn-secondary" type="button">
			<liferay-ui:message key="select" />
		</button>
	</div>

	<react:component
		data="<%= data %>"
		module="asset_tags_selector/index.es"
	/>
</div>