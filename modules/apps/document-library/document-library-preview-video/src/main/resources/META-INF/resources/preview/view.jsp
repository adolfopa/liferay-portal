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

<%@ include file="/preview/init.jsp" %>

<%
String videoPosterURL = (String)request.getAttribute(DLPreviewVideoWebKeys.VIDEO_THUMBNAIL_URL);

List<String> previewFileURLs = (List<String>)request.getAttribute(DLPreviewVideoWebKeys.PREVIEW_FILE_URLS);

String randomNamespace = PortalUtil.generateRandomKey(request, "portlet_document_library_view_file_entry_preview") + StringPool.UNDERLINE;
String modulePath = (String)request.getAttribute(DLPreviewVideoWebKeys.MODULE_PATH);

List<Map<String, String>> videoSources = new ArrayList<>();

for (String previewFileURL : previewFileURLs) {
	if (Validator.isNotNull(previewFileURL)) {
		if (previewFileURL.endsWith("mp4")) {
			videoSources.add(MapUtil.fromArray("type", "video/mp4", "url", previewFileURL));
		}
		else if (previewFileURL.endsWith("ogv")) {
			videoSources.add(MapUtil.fromArray("type", "video/ogv", "url", previewFileURL));
		}
	}
}

Map<String, Object> context = new HashMap<>();
context.put("videoSources", videoSources);
context.put("videoPosterURL", videoPosterURL);
%>

<liferay-util:html-top
	outputKey="document_library_preview_video_css"
>
	<link href="<%= PortalUtil.getStaticResourceURL(request, application.getContextPath() + "/preview/css/main.css") %>" rel="stylesheet" type="text/css" />
</liferay-util:html-top>

<soy:component-renderer
	componentId='<%= renderResponse.getNamespace() + randomNamespace + "previewVideo" %>'
	context="<%= context %>"
	module="<%= modulePath %>"
	templateNamespace="com.liferay.document.library.preview.VideoPreviewer.render"
/>