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

package com.liferay.portal.kernel.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.liferay.portal.kernel.util.ServiceProxyFactory;

/**
 * @author Bruno Farache
 * @author Alexander Chow
 * @author Mauro Mariuzzo
 */
public class DocumentConversionUtil {

	/**
	 * Convert the provided stream from source format to target format.
	 * Format are identified by extension
	 *
	 * @param id
	 * @param inputStream
	 * @param sourceExtension
	 * @param targetExtension
	 * @return converted file
	 * @throws IOException
	 */
	public static File convert(
			String id, InputStream inputStream, String sourceExtension,
			String targetExtension)
		throws IOException {

		return _documentConversion.convert(
			id, inputStream, sourceExtension, targetExtension);
	}

	/**
	 * Return all the available formats the provided format can be converted to.
	 *
	 * @param extension
	 * @return list of estensions
	 */
	public static String[] getConversions(String extension) {
		return _documentConversion.getConversions(extension);
	}

	/**
	 * Return the full path of the target file resultant of the conversion
	 *
	 * @param id
	 * @param targetExtension
	 * @return target file name
	 */
	public static String getFilePath(String id, String targetExtension) {
		return _documentConversion.getFilePath(id, targetExtension);
	}

	/**
	 * Return true if at least one ConverterWrapper is installed and enabled
	 * inside the DocumentConversion Framework
	 *
	 * @return true or false
	 */
	public static boolean hasEnabledConverter() {
		return _documentConversion.hasEnabledConverter();
	}

	/**
	 * Return true if at least one ConverterWrapper installed and enabled inside
	 * the DocumentConversion Framework is able to process the provided source
	 * format
	 *
	 * @param extension identify the format of the source file
	 * @return true or false
	 */
	public static boolean hasEnabledConverter(String extension) {
		return _documentConversion.hasEnabledConverter(extension);
	}

	/**
	 * Return true if at least one ConverterWrapper installed and enabled inside
	 * the DocumentConversion Framework is able to convert a file from source
	 * format to target format
	 *
	 * @param sourceExtension identify the format of the source file
	 * @param targetExtension identify the format of the target file
	 * @return true or false
	 */
	public static boolean hasEnabledConverter(
		String sourceExtension, String targetExtension) {

		return _documentConversion.hasEnabledConverter(
			sourceExtension, targetExtension);
	}

	/**
	 * Return true is the format represent a textual file (or can be converted
	 * to) that can be compared to identify changes
	 *
	 * @param extension
	 * @return true or false
	 */
	public static boolean isComparableVersion(String extension) {
		return _documentConversion.isComparable(extension);
	}

	/**
	 * Return true if the format has to be converted to text before proceed to
	 * compare it
	 *
	 * @param extension
	 * @return true or false
	 */
	public static boolean isConvertBeforeCompare(String extension) {
		return _documentConversion.isConvertBeforeCompare(extension);
	}

	private static volatile DocumentConversion _documentConversion =
		ServiceProxyFactory.newServiceTrackedInstance(
			DocumentConversion.class, DocumentConversionUtil.class,
			"_documentConversion", false);

}
