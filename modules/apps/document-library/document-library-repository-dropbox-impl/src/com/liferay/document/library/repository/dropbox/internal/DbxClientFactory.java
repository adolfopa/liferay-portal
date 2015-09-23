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

package com.liferay.document.library.repository.dropbox.internal;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxRequestConfig;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;

import java.util.Locale;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * @author Adolfo Pérez
 */
@Component(immediate = true, service = DbxClientFactory.class)
public class DbxClientFactory {

	public static DbxClientFactory getDbxClientFactory() {
		return _dbxClientFactory;
	}

	@Activate
	public void activate() {
		_dbxClientFactory = this;
	}

	public DbxClient getDbxClient(
		long repositoryId, UnicodeProperties typeSettingsProperties) {

		DbxRequestConfig dbxRequestConfig = new DbxRequestConfig(
			_CLIENT_IDENTIFIER,
			LanguageUtil.getLanguageId(Locale.getDefault()));

		String accessToken = typeSettingsProperties.getProperty("ACCESS_TOKEN");

		return new DbxClient(dbxRequestConfig, accessToken);
	}

	private static final String _CLIENT_IDENTIFIER = "Liferay-Portal/7.0.0";

	private static DbxClientFactory _dbxClientFactory;

}