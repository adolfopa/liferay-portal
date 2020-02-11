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
KBAttachmentItemSelectorViewDisplayContext kbAttachmentItemSelectorViewDisplayContext = (KBAttachmentItemSelectorViewDisplayContext)request.getAttribute(KBItemSelectorWebKeys.KB_ATTACHMENT_ITEM_SELECTOR_VIEW_DISPLAY_CONTEXT);
%>

<liferay-item-selector:repository-entry-browser
	emptyResultsMessage='<%= LanguageUtil.get(resourceBundle, "there-are-no-attachments") %>'
	extensions="<%= kbAttachmentItemSelectorViewDisplayContext.getMimeTypes() %>"
	itemSelectedEventName="<%= kbAttachmentItemSelectorViewDisplayContext.getItemSelectedEventName() %>"
	itemSelectorReturnTypeResolver="<%= kbAttachmentItemSelectorViewDisplayContext.getItemSelectorReturnTypeResolver() %>"
	maxFileSize="<%= kbAttachmentItemSelectorViewDisplayContext.getKBAttachmentMaxSize() %>"
	portletURL="<%= kbAttachmentItemSelectorViewDisplayContext.getPortletURL(request, liferayPortletResponse) %>"
	repositoryEntries="<%= kbAttachmentItemSelectorViewDisplayContext.getPortletFileEntries() %>"
	repositoryEntriesCount="<%= kbAttachmentItemSelectorViewDisplayContext.getPortletFileEntriesCount() %>"
	showDragAndDropZone="<%= false %>"
	tabName="<%= kbAttachmentItemSelectorViewDisplayContext.getTitle(locale) %>"
	uploadURL="<%= kbAttachmentItemSelectorViewDisplayContext.getUploadURL(liferayPortletResponse) %>"
/>