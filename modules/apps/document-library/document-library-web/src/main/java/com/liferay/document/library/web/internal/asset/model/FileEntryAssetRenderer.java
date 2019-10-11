/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.document.library.web.internal.asset.model;

import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.model.DDMFormValuesReader;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.document.library.util.DLURLHelper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.trash.TrashHelper;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Locale;

/**
 * @author Adolfo Pérez
 */
public class FileEntryAssetRenderer implements AssetRenderer<FileEntry> {

	public FileEntryAssetRenderer(AssetRenderer<FileEntry> assetRenderer) {
		_assetRenderer = assetRenderer;
	}

	@Override
	@Deprecated
	public String getAddToPagePortletId() throws Exception {
		return _assetRenderer.getAddToPagePortletId();
	}

	@Override
	public FileEntry getAssetObject() {
		return _assetRenderer.getAssetObject();
	}

	@Override
	public FileEntry getAssetObject(long versionClassPK) {
		return _assetRenderer.getAssetObject(versionClassPK);
	}

	@Override
	public AssetRendererFactory<FileEntry> getAssetRendererFactory() {
		return _assetRenderer.getAssetRendererFactory();
	}

	@Override
	public int getAssetRendererType() {
		return _assetRenderer.getAssetRendererType();
	}

	@Override
	public String[] getAvailableLanguageIds() throws Exception {
		return _assetRenderer.getAvailableLanguageIds();
	}

	@Override
	public DDMFormValuesReader getDDMFormValuesReader() {
		return _assetRenderer.getDDMFormValuesReader();
	}

	@Override
	public String getDefaultLanguageId() throws Exception {
		return _assetRenderer.getDefaultLanguageId();
	}

	@Override
	public String getDiscussionPath() {
		return _assetRenderer.getDiscussionPath();
	}

	@Override
	@Deprecated
	public Date getDisplayDate() {
		return _assetRenderer.getDisplayDate();
	}

	@Override
	public long getGroupId() {
		return _assetRenderer.getGroupId();
	}

	@Override
	public String getNewName(String oldName, String token) {
		return _assetRenderer.getNewName(oldName, token);
	}

	@Override
	@Deprecated
	public String getPreviewPath(
		PortletRequest portletRequest,
		PortletResponse portletResponse) throws Exception {
		return _assetRenderer.getPreviewPath(portletRequest, portletResponse);
	}

	@Override
	public String getSearchSummary(Locale locale) {
		return _assetRenderer.getSearchSummary(locale);
	}

	@Override
	public int getStatus() {
		return _assetRenderer.getStatus();
	}

	@Override
	public String getSummary() {
		return _assetRenderer.getSummary();
	}

	@Override
	public String[] getSupportedConversions() {
		return _assetRenderer.getSupportedConversions();
	}

	@Override
	public String getThumbnailPath(PortletRequest portletRequest)
		throws Exception {
		return _assetRenderer.getThumbnailPath(portletRequest);
	}

	@Override
	public String getURLDownload(
		ThemeDisplay themeDisplay) {
		return _assetRenderer.getURLDownload(themeDisplay);
	}

	@Override
	public PortletURL getURLEdit(
		HttpServletRequest httpServletRequest) throws Exception {
		return _assetRenderer.getURLEdit(httpServletRequest);
	}

	@Override
	public PortletURL getURLEdit(
		HttpServletRequest httpServletRequest,
		WindowState windowState, PortletURL redirectURL) throws Exception {
		return _assetRenderer.getURLEdit(
			httpServletRequest, windowState, redirectURL);
	}

	@Override
	public PortletURL getURLEdit(
		HttpServletRequest httpServletRequest,
		WindowState windowState, String redirect) throws Exception {
		return _assetRenderer.getURLEdit(
			httpServletRequest, windowState, redirect);
	}

	@Override
	public PortletURL getURLEdit(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse) throws Exception {
		return _assetRenderer.getURLEdit(
			liferayPortletRequest, liferayPortletResponse);
	}

	@Override
	public PortletURL getURLEdit(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		WindowState windowState, PortletURL redirectURL) throws Exception {
		return _assetRenderer.getURLEdit(liferayPortletRequest,
			liferayPortletResponse,
			windowState, redirectURL);
	}

	@Override
	public PortletURL getURLEdit(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		WindowState windowState, String redirect) throws Exception {
		return _assetRenderer.getURLEdit(liferayPortletRequest,
			liferayPortletResponse,
			windowState, redirect);
	}

	@Override
	public PortletURL getURLExport(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse) throws Exception {
		return _assetRenderer.getURLExport(
			liferayPortletRequest,
			liferayPortletResponse);
	}

	@Override
	public String getURLImagePreview(PortletRequest portletRequest)
		throws Exception {
		return _assetRenderer.getURLImagePreview(portletRequest);
	}

	@Override
	public String getUrlTitle() {
		return _assetRenderer.getUrlTitle();
	}

	@Override
	public String getUrlTitle(Locale locale) {
		return _assetRenderer.getUrlTitle(locale);
	}

	@Override
	public String getURLView(
		LiferayPortletResponse liferayPortletResponse,
		WindowState windowState) throws Exception {
		return _assetRenderer.getURLView(liferayPortletResponse, windowState);
	}

	@Override
	public PortletURL getURLViewDiffs(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse) throws Exception {
		return _assetRenderer.getURLViewDiffs(
			liferayPortletRequest,
			liferayPortletResponse);
	}

	@Override
	public String getURLViewInContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		String noSuchEntryRedirect) throws Exception {
		return _assetRenderer.getURLViewInContext(liferayPortletRequest,
			liferayPortletResponse, noSuchEntryRedirect);
	}

	@Override
	public String getURLViewUsages(
		HttpServletRequest httpServletRequest) throws Exception {
		return _assetRenderer.getURLViewUsages(httpServletRequest);
	}

	@Override
	public long getUserId() {
		return _assetRenderer.getUserId();
	}

	@Override
	public String getUserName() {
		return _assetRenderer.getUserName();
	}

	@Override
	public String getUuid() {
		return _assetRenderer.getUuid();
	}

	@Override
	public String getViewInContextMessage() {
		return _assetRenderer.getViewInContextMessage();
	}

	@Override
	public boolean hasEditPermission(
		PermissionChecker permissionChecker) throws PortalException {
		return _assetRenderer.hasEditPermission(permissionChecker);
	}

	@Override
	public boolean hasViewPermission(
		PermissionChecker permissionChecker) throws PortalException {
		return _assetRenderer.hasViewPermission(permissionChecker);
	}

	@Override
	public boolean isCategorizable(long groupId) {
		return _assetRenderer.isCategorizable(groupId);
	}

	@Override
	public boolean isCommentable() {
		return _assetRenderer.isCommentable();
	}

	@Override
	public boolean isConvertible() {
		return _assetRenderer.isConvertible();
	}

	@Override
	public boolean isDisplayable() {
		return _assetRenderer.isDisplayable();
	}

	@Override
	public boolean isLocalizable() {
		return _assetRenderer.isLocalizable();
	}

	@Override
	public boolean isPreviewInContext() {
		return _assetRenderer.isPreviewInContext();
	}

	@Override
	public boolean isPrintable() {
		return _assetRenderer.isPrintable();
	}

	@Override
	public boolean isRatable() {
		return _assetRenderer.isRatable();
	}

	@Override
	@Deprecated
	public void setAddToPagePreferences(
		PortletPreferences portletPreferences,
		String portletId, ThemeDisplay themeDisplay) throws Exception {
		_assetRenderer.setAddToPagePreferences(portletPreferences, portletId,
			themeDisplay);
	}

	@Override
	public String getClassName() {
		return FileEntry.class.getName();
	}

	@Override
	public long getClassPK() {
		return _assetRenderer.getClassPK();
	}

	@Override
	public String getIconCssClass() throws PortalException {
		return _assetRenderer.getIconCssClass();
	}

	@Override
	public String getSummary(
		PortletRequest portletRequest,
		PortletResponse portletResponse) {
		return _assetRenderer.getSummary(portletRequest, portletResponse);
	}

	@Override
	public String getTitle(Locale locale) {
		return _assetRenderer.getTitle(locale);
	}

	@Override
	public boolean include(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse,
		String template) throws Exception {
		return _assetRenderer.include(httpServletRequest, httpServletResponse,
			template);
	}

	private AssetRenderer<FileEntry> _assetRenderer;

}
