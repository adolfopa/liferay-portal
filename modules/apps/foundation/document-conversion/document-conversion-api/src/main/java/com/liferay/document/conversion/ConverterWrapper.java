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

package com.liferay.document.conversion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Using this interface you can register new Component able to convert to
 * convert file from a format to another.
 * This is used, for example, inside Document Media Library to generate a pdf
 * from the file to generate thumbnail and previews
 *
 * @author Mauro Mariuzzo
 */
public interface ConverterWrapper {

	public File convert(
			String id, InputStream inputStream, String sourceExtension,
			String targetExtension)
		throws IOException;

	public boolean canConvert(String extension);

	public boolean canConvert(String sourceExtension, String targetExtension);

	public String[] getConversions(String extension);

	public boolean isEnabled();

}
