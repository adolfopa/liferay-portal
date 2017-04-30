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

package com.liferay.document.library.document.conversion.internal;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.DocumentFormatRegistry;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;

import com.liferay.document.library.document.conversion.configuration.OpenOfficeConfiguration;
import com.liferay.document.library.document.conversion.wrapper.BaseConverterWrapper;
import com.liferay.document.library.document.conversion.wrapper.ConverterWrapper;
import com.liferay.document.library.kernel.document.conversion.DocumentConversion;
import com.liferay.osgi.util.ServiceTrackerFactory;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.configuration.Filter;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.SortedArrayList;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.SystemProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portal.util.PropsValues;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Bruno Farache
 * @author Alexander Chow
 */
@Component(
	configurationPid = "com.liferay.document.library.document.conversion.configuration.OpenOfficeConfiguration",
	immediate = true, service = DocumentConversion.class
)
public class DocumentConversionImpl implements DocumentConversion {

	@Override
	public File convert(
			String id, InputStream inputStream, String sourceExtension,
			String targetExtension)
		throws IOException {

		ConverterWrapper converterWrapper = getEnabledConverter(
			sourceExtension, targetExtension);

		if (converterWrapper != null) {
			return converterWrapper.convert(
				id, inputStream, sourceExtension, targetExtension);
		}
		else {
			return null;
		}
	}

	@Override
	public void disconnect() {
		_log.info("disconnect");

		for (ConverterWrapper converterWrapper : _getConverterWrappers()) {
			if (converterWrapper.isEnabled()) {
				converterWrapper.disconnect();
			}
		}
	}

	@Override
	public String[] getConversions(String extension) {
		Set<String> result = new HashSet<String>();

		for (ConverterWrapper converterWrapper : _getConverterWrappers()) {
			if (!converterWrapper.isEnabled()) {
				continue;
			}

			for (String conversion : converterWrapper.getConversions(
					extension)) {

				result.add(conversion);
			}
		}

		return result.toArray(new String[result.size()]);
	}

	@Override
	public String getFilePath(String id, String targetExtension) {
		return BaseConverterWrapper.getFilePath(id, targetExtension);
	}

	@Override
	public boolean hasEnabledConverter(String extension) {
		ConverterWrapper converterWrapper = getEnabledConverter(extension);

		return converterWrapper != null;
	}

	@Override
	public boolean hasEnabledConverter(
		String sourceExtension, String targetExtension) {

		ConverterWrapper converterWrapper = getEnabledConverter(
			sourceExtension, targetExtension);

		return converterWrapper != null;
	}

	@Override
	public boolean isComparableVersion(String extension) {
		boolean enabled = false;

		String periodAndExtension = StringPool.PERIOD.concat(extension);

		for (int i = 0; i < _COMPARABLE_FILE_EXTENSIONS.length; i++) {
			if (StringPool.STAR.equals(_COMPARABLE_FILE_EXTENSIONS[i]) ||
				periodAndExtension.equals(_COMPARABLE_FILE_EXTENSIONS[i])) {

				enabled = true;

				break;
			}
		}

		if (!enabled) {
			return false;
		}

		if (extension.equals("css") || extension.equals("htm") ||
			extension.equals("html") || extension.equals("js") ||
			extension.equals("txt") || extension.equals("xml")) {

			return true;
		}

		try {
			if (isEnabled() && isConvertBeforeCompare(extension)) {
				return true;
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		return false;
	}

	@Override
	public boolean isConvertBeforeCompare(String extension) {
		if (extension.equals("txt")) {
			return false;
		}

		String[] conversions = getConversions(extension);

		for (int i = 0; i < conversions.length; i++) {
			if (conversions[i].equals("txt")) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isEnabled() {
		for (ConverterWrapper converterWrapper : _getConverterWrappers()) {
			if (converterWrapper.isEnabled()) {
				return true;
			}
		}

		return false;
	}

	protected ConverterWrapper getEnabledConverter(String extension) {
		for (ConverterWrapper converterWrapper : _getConverterWrappers()) {
			if (converterWrapper.isEnabled() &&
					converterWrapper.canConvert(extension)) {

				return converterWrapper;
			}
		}

		return null;
	}

	protected ConverterWrapper getEnabledConverter(
		String sourceExtension, String targetExtension) {

		for (ConverterWrapper converterWrapper : _getConverterWrappers()) {
			if (converterWrapper.isEnabled() &&
				converterWrapper.canConvert(sourceExtension, targetExtension)) {

				return converterWrapper;
			}
		}

		return null;
	}
	private ConverterWrapper[] _getConverterWrappers() {
		return _serviceTracker.getServices(new ConverterWrapper[0]);
	}

	private static final String[] _COMPARABLE_FILE_EXTENSIONS =
		PropsValues.DL_COMPARABLE_FILE_EXTENSIONS;


	private static final Log _log = LogFactoryUtil.getLog(
		DocumentConversionImpl.class);

	private static ServiceTracker<ConverterWrapper, ConverterWrapper>
		_serviceTracker = ServiceTrackerFactory.open(ConverterWrapper.class);

}