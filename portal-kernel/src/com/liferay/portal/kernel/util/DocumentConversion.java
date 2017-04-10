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

/**
 * @author Mauro Mariuzzo
 */
public interface DocumentConversion {

	public File convert(
			String id, InputStream inputStream, String sourceExtension,
			String targetExtension)
		throws IOException;

	public String[] getConversions(String extension);

	public String getFilePath(String id, String targetExtension);

	public boolean hasEnabledConverter();

	public boolean hasEnabledConverter(String extension);

	public boolean hasEnabledConverter(
		String sourceExtension, String targetExtension);

	public boolean isComparable(String extension);

	public boolean isConvertBeforeCompare(String extension);

}
