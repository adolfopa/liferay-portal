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

package com.liferay.document.library.web.internal.asset.model;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.model.ClassTypeReader;
import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.Tuple;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	immediate = true,
	property = "javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY,
	service = AssetRendererFactory.class
)
public class FileEntryAssetRendererFactory
	implements AssetRendererFactory<FileEntry> {

	@Override
	public AssetEntry getAssetEntry(long assetEntryId) throws PortalException {
		return _dlFileEntryAssetRendererFactory.getAssetEntry(assetEntryId);
	}

	@Override
	public AssetEntry getAssetEntry(String className, long classPK)
		throws PortalException {

		return _dlFileEntryAssetRendererFactory.getAssetEntry(
			className, classPK);
	}

	@Override
	public AssetRenderer<FileEntry> getAssetRenderer(FileEntry entry, int type)
		throws PortalException {

		return _dlFileEntryAssetRendererFactory.getAssetRenderer(entry, type);
	}

	@Override
	public AssetRenderer<FileEntry> getAssetRenderer(long classPK)
		throws PortalException {

		return _dlFileEntryAssetRendererFactory.getAssetRenderer(classPK);
	}

	@Override
	public AssetRenderer<FileEntry> getAssetRenderer(long classPK, int type)
		throws PortalException {

		return _dlFileEntryAssetRendererFactory.getAssetRenderer(classPK, type);
	}

	@Override
	public AssetRenderer<FileEntry> getAssetRenderer(
			long groupId, String urlTitle)
		throws PortalException {

		return _dlFileEntryAssetRendererFactory.getAssetRenderer(
			groupId, urlTitle);
	}

	@Override
	public String getClassName() {
		return FileEntry.class.getName();
	}

	@Override
	public long getClassNameId() {
		return _dlFileEntryAssetRendererFactory.getClassNameId();
	}

	@Override
	public Tuple getClassTypeFieldName(
			long classTypeId, String fieldName, Locale locale)
		throws Exception {

		return _dlFileEntryAssetRendererFactory.getClassTypeFieldName(
			classTypeId, fieldName, locale);
	}

	@Override
	public List<Tuple> getClassTypeFieldNames(
			long classTypeId, Locale locale, int start, int end)
		throws Exception {

		return _dlFileEntryAssetRendererFactory.getClassTypeFieldNames(
			classTypeId, locale, start, end);
	}

	@Override
	public int getClassTypeFieldNamesCount(long classTypeId, Locale locale)
		throws Exception {

		return _dlFileEntryAssetRendererFactory.getClassTypeFieldNamesCount(
			classTypeId, locale);
	}

	@Override
	public ClassTypeReader getClassTypeReader() {
		return _dlFileEntryAssetRendererFactory.getClassTypeReader();
	}

	@Override
	public Map<Long, String> getClassTypes(long[] groupIds, Locale locale)
		throws Exception {

		return _dlFileEntryAssetRendererFactory.getClassTypes(groupIds, locale);
	}

	@Override
	public String getIconCssClass() {
		return _dlFileEntryAssetRendererFactory.getIconCssClass();
	}

	@Override
	public String getIconPath(PortletRequest portletRequest) {
		return _dlFileEntryAssetRendererFactory.getIconPath(portletRequest);
	}

	@Override
	public String getPortletId() {
		return _dlFileEntryAssetRendererFactory.getPortletId();
	}

	@Override
	public String getSubtypeTitle(Locale locale) {
		return _dlFileEntryAssetRendererFactory.getSubtypeTitle(locale);
	}

	@Override
	public String getType() {
		return _dlFileEntryAssetRendererFactory.getType();
	}

	@Override
	public String getTypeName(Locale locale) {
		return _dlFileEntryAssetRendererFactory.getTypeName(locale);
	}

	@Override
	public String getTypeName(Locale locale, boolean hasSubtypes) {
		return _dlFileEntryAssetRendererFactory.getTypeName(
			locale, hasSubtypes);
	}

	@Override
	public String getTypeName(Locale locale, long subtypeId) {
		return _dlFileEntryAssetRendererFactory.getTypeName(locale, subtypeId);
	}

	@Override
	public PortletURL getURLAdd(
			LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse)
		throws PortalException {

		return _dlFileEntryAssetRendererFactory.getURLAdd(
			liferayPortletRequest, liferayPortletResponse);
	}

	@Override
	public PortletURL getURLAdd(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse, long classTypeId) {

		return _dlFileEntryAssetRendererFactory.getURLAdd(
			liferayPortletRequest, liferayPortletResponse, classTypeId);
	}

	@Override
	public PortletURL getURLView(
		LiferayPortletResponse liferayPortletResponse,
		WindowState windowState) {

		return _dlFileEntryAssetRendererFactory.getURLView(
			liferayPortletResponse, windowState);
	}

	@Override
	public boolean hasAddPermission(
			PermissionChecker permissionChecker, long groupId, long classTypeId)
		throws Exception {

		return _dlFileEntryAssetRendererFactory.hasAddPermission(
			permissionChecker, groupId, classTypeId);
	}

	@Override
	public boolean hasClassTypeFieldNames(long classTypeId, Locale locale)
		throws Exception {

		return _dlFileEntryAssetRendererFactory.hasClassTypeFieldNames(
			classTypeId, locale);
	}

	@Override
	public boolean hasPermission(
			PermissionChecker permissionChecker, long classPK, String actionId)
		throws Exception {

		return _dlFileEntryAssetRendererFactory.hasPermission(
			permissionChecker, classPK, actionId);
	}

	@Override
	public boolean isActive(long companyId) {
		return _dlFileEntryAssetRendererFactory.isActive(companyId);
	}

	@Override
	public boolean isCategorizable() {
		return _dlFileEntryAssetRendererFactory.isCategorizable();
	}

	@Override
	public boolean isLinkable() {
		return _dlFileEntryAssetRendererFactory.isLinkable();
	}

	@Override
	public boolean isSearchable() {
		return _dlFileEntryAssetRendererFactory.isSearchable();
	}

	@Override
	public boolean isSelectable() {
		return _dlFileEntryAssetRendererFactory.isSelectable();
	}

	@Override
	public boolean isSupportsClassTypes() {
		return _dlFileEntryAssetRendererFactory.isSupportsClassTypes();
	}

	@Override
	public void setClassName(String className) {
		_dlFileEntryAssetRendererFactory.setClassName(className);
	}

	@Override
	public void setPortletId(String portletId) {
		_dlFileEntryAssetRendererFactory.setPortletId(portletId);
	}

	@Reference
	private DLFileEntryAssetRendererFactory _dlFileEntryAssetRendererFactory;

}