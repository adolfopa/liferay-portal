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

package com.liferay.upload.web.internal;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.upload.UniqueFileNameProvider;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alejandro Tardín
 */
@Component
public class DefaultUniqueFileNameProvider implements UniqueFileNameProvider {

	@Override
	public String provide(String fileName, Predicate<String> predicate)
		throws PortalException {

		String uniqueFileName = fileName;

		int tries = 0;

		while (predicate.test(uniqueFileName)) {
			if (tries >= _UNIQUE_FILE_NAME_TRIES) {
				throw new PortalException(
					"Unable to get a unique file name for " + fileName);
			}

			tries++;

			Matcher matcher = _PARENTHETICAL_SUFFIX_REGEX.matcher(
				uniqueFileName);

			if (matcher.matches()) {
				String name = matcher.group("name");
				String extension = matcher.group("extension");

				uniqueFileName = name;

				if (extension != null) {
					uniqueFileName += "." + extension;
				}
			}

			uniqueFileName = FileUtil.appendParentheticalSuffix(
				uniqueFileName, String.valueOf(tries));
		}

		return uniqueFileName;
	}

	private static final Pattern _PARENTHETICAL_SUFFIX_REGEX = Pattern.compile(
		"(?<name>.+) \\(\\d+\\)(\\.(?<extension>[^.]+))?");

	private static final int _UNIQUE_FILE_NAME_TRIES = 50;

}