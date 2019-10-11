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

package com.liferay.document.library.web.internal.util;

import com.liferay.asset.kernel.model.ClassType;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.info.display.contributor.InfoDisplayContributor;
import com.liferay.info.display.contributor.InfoDisplayField;
import com.liferay.info.display.contributor.InfoDisplayObjectProvider;
import com.liferay.info.display.contributor.field.InfoDisplayContributorField;
import com.liferay.info.display.contributor.field.InfoDisplayContributorFieldType;
import com.liferay.info.display.url.provider.InfoEditURLProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.repository.liferayrepository.model.LiferayFileEntry;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Adolfo Pérez
 */
public class DLFileEntryInfoDisplayRegistryUtil {

	public static Runnable register(
		BundleContext bundleContext,
		InfoDisplayContributor<FileEntry> infoDisplayContributor) {

		return _register(
			bundleContext, InfoDisplayContributor.class,
			new DLFileEntryInfoDisplayContributor(infoDisplayContributor));
	}

	public static Runnable register(
		BundleContext bundleContext,
		InfoDisplayContributorField<FileEntry> infoDisplayContributorField) {

		return _register(
			bundleContext, InfoDisplayContributorField.class,
			new DLFileEntryInfoDisplayContributorField(
				infoDisplayContributorField));
	}

	public static Runnable register(
		BundleContext bundleContext,
		InfoEditURLProvider<FileEntry> infoEditURLProvider) {

		return _register(
			bundleContext, InfoEditURLProvider.class,
			new DLFileEntryInfoEditURLProvider(infoEditURLProvider));
	}

	private static <S, T> Runnable _register(
		BundleContext bundleContext, Class<T> clazz, T value) {

//		ServiceRegistration<T> serviceRegistration =
//			bundleContext.registerService(
//				clazz, value,
//				new HashMapDictionary<String, Object>() {
//					{
//						put(
//							"model.class.name",
//							DLFileEntryConstants.getClassName());
//					}
//				});
//
//		return serviceRegistration::unregister;
		return () -> {};
	}

	private static class DLFileEntryInfoDisplayContributor
		implements InfoDisplayContributor<DLFileEntry> {

		public DLFileEntryInfoDisplayContributor(
			InfoDisplayContributor<FileEntry> infoDisplayContributor) {

			_infoDisplayContributor = infoDisplayContributor;
		}

		@Override
		public String getClassName() {
			return DLFileEntryConstants.getClassName();
		}

		@Override
		public List<InfoDisplayField> getClassTypeInfoDisplayFields(
				long classTypeId, Locale locale)
			throws PortalException {

			return _infoDisplayContributor.getClassTypeInfoDisplayFields(
				classTypeId, locale);
		}

		@Override
		public List<ClassType> getClassTypes(long groupId, Locale locale)
			throws PortalException {

			return _infoDisplayContributor.getClassTypes(groupId, locale);
		}

		@Override
		public Set<InfoDisplayField> getInfoDisplayFields(
				long classTypeId, Locale locale)
			throws PortalException {

			return _infoDisplayContributor.getInfoDisplayFields(
				classTypeId, locale);
		}

		@Override
		public Map<String, Object> getInfoDisplayFieldsValues(
				DLFileEntry dlFileEntry, Locale locale)
			throws PortalException {

			return _infoDisplayContributor.getInfoDisplayFieldsValues(
				new LiferayFileEntry(dlFileEntry), locale);
		}

		@Override
		public Object getInfoDisplayFieldValue(
				DLFileEntry dlFileEntry, String fieldName, Locale locale)
			throws PortalException {

			return _infoDisplayContributor.getInfoDisplayFieldValue(
				new LiferayFileEntry(dlFileEntry), fieldName, locale);
		}

		@Override
		public InfoDisplayObjectProvider getInfoDisplayObjectProvider(
				long classPK)
			throws PortalException {

			return new DLFileEntryInfoDisplayObjectProvider(
				_infoDisplayContributor.getInfoDisplayObjectProvider(classPK));
		}

		@Override
		public InfoDisplayObjectProvider<DLFileEntry>
				getInfoDisplayObjectProvider(long groupId, String urlTitle)
			throws PortalException {

			return new DLFileEntryInfoDisplayObjectProvider(
				_infoDisplayContributor.getInfoDisplayObjectProvider(
					groupId, urlTitle));
		}

		@Override
		public String getInfoURLSeparator() {
			return _infoDisplayContributor.getInfoURLSeparator();
		}

		@Override
		public String getLabel(Locale locale) {
			return _infoDisplayContributor.getLabel(locale);
		}

		@Override
		public InfoDisplayObjectProvider getPreviewInfoDisplayObjectProvider(
				long classPK, int type)
			throws PortalException {

			return new DLFileEntryInfoDisplayObjectProvider(
				_infoDisplayContributor.getPreviewInfoDisplayObjectProvider(
					classPK, type));
		}

		@Override
		public Map<String, Object> getVersionInfoDisplayFieldsValues(
				DLFileEntry dlFileEntry, long versionClassPK, Locale locale)
			throws PortalException {

			return _infoDisplayContributor.getVersionInfoDisplayFieldsValues(
				new LiferayFileEntry(dlFileEntry), versionClassPK, locale);
		}

		private final InfoDisplayContributor<FileEntry> _infoDisplayContributor;

	}

	private static class DLFileEntryInfoDisplayContributorField
		implements InfoDisplayContributorField<DLFileEntry> {

		public DLFileEntryInfoDisplayContributorField(
			InfoDisplayContributorField<FileEntry>
				infoDisplayContributorField) {

			_infoDisplayContributorField = infoDisplayContributorField;
		}

		@Override
		public String getKey() {
			return _infoDisplayContributorField.getKey();
		}

		@Override
		public String getLabel(Locale locale) {
			return _infoDisplayContributorField.getLabel(locale);
		}

		@Override
		public InfoDisplayContributorFieldType getType() {
			return _infoDisplayContributorField.getType();
		}

		@Override
		public Object getValue(DLFileEntry dlFileEntry, Locale locale) {
			return _infoDisplayContributorField.getValue(
				new LiferayFileEntry(dlFileEntry), locale);
		}

		private final InfoDisplayContributorField<FileEntry>
			_infoDisplayContributorField;

	}

	private static class DLFileEntryInfoDisplayObjectProvider
		implements InfoDisplayObjectProvider<DLFileEntry> {

		public DLFileEntryInfoDisplayObjectProvider(
			InfoDisplayObjectProvider<FileEntry> infoDisplayObjectProvider) {

			_infoDisplayObjectProvider = infoDisplayObjectProvider;
		}

		@Override
		public long getClassNameId() {
			return PortalUtil.getClassNameId(
				DLFileEntryConstants.getClassName());
		}

		@Override
		public long getClassPK() {
			return _infoDisplayObjectProvider.getClassPK();
		}

		@Override
		public long getClassTypeId() {
			return _infoDisplayObjectProvider.getClassTypeId();
		}

		@Override
		public String getDescription(Locale locale) {
			return _infoDisplayObjectProvider.getDescription(locale);
		}

		@Override
		public DLFileEntry getDisplayObject() {
			FileEntry fileEntry = _infoDisplayObjectProvider.getDisplayObject();

			return (DLFileEntry)fileEntry.getModel();
		}

		@Override
		public long getGroupId() {
			return _infoDisplayObjectProvider.getGroupId();
		}

		@Override
		public String getKeywords(Locale locale) {
			return _infoDisplayObjectProvider.getKeywords(locale);
		}

		@Override
		public String getTitle(Locale locale) {
			return _infoDisplayObjectProvider.getTitle(locale);
		}

		@Override
		public String getURLTitle(Locale locale) {
			return _infoDisplayObjectProvider.getURLTitle(locale);
		}

		private final InfoDisplayObjectProvider<FileEntry>
			_infoDisplayObjectProvider;

	}

	private static class DLFileEntryInfoEditURLProvider
		implements InfoEditURLProvider<DLFileEntry> {

		public DLFileEntryInfoEditURLProvider(
			InfoEditURLProvider<FileEntry> infoEditURLProvider) {

			_infoEditURLProvider = infoEditURLProvider;
		}

		@Override
		public String getURL(
				DLFileEntry dlFileEntry, HttpServletRequest httpServletRequest)
			throws Exception {

			return _infoEditURLProvider.getURL(
				new LiferayFileEntry(dlFileEntry), httpServletRequest);
		}

		private final InfoEditURLProvider<FileEntry> _infoEditURLProvider;

	}

}