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

package com.liferay.frontend.taglib.servlet.taglib;

import com.liferay.frontend.taglib.servlet.taglib.base.BaseBarTag;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Ambrín Chaudhary
 */
public class LanguagesTag extends BaseBarTag {

	public Locale[] getSiteAvailableLocales() {
		return _siteAvailableLocales;
	}

	public Locale getSiteDefaultLocale() {
		return _siteDefaultLocale;
	}

	public Object getTranslatedLanguages() {
		return _translatedLanguages;
	}

	public boolean isInheritLocales() {
		return _inheritLocales;
	}

	public void setInheritLocales(boolean inheritLocales) {
		_inheritLocales = inheritLocales;
	}

	public void setSiteAvailableLocales(Locale[] siteAvailableLocales) {
		_siteAvailableLocales = siteAvailableLocales;
	}

	public void setSiteDefaultLocale(Locale siteDefaultLocale) {
		_siteDefaultLocale = siteDefaultLocale;
	}

	public void setTranslatedLanguages(Object translatedLanguages) {
		_translatedLanguages = translatedLanguages;
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();

		_inheritLocales = false;
		_siteAvailableLocales = null;
		_siteDefaultLocale = null;
		_translatedLanguages = null;
	}

	@Override
	protected String getPage() {
		return "/languages/page.jsp";
	}

	@Override
	protected void setAttributes(HttpServletRequest httpServletRequest) {
		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		HashMap<String, Object> data = HashMapBuilder.<String, Object>put(
			"availableLocales",
			_getAvailableLocalesJSONArray(themeDisplay.getLocale())
		).put(
			"defaultLocaleId", _getDefaultLanguageId(themeDisplay)
		).put(
			"inheritLocales", _inheritLocales
		).put(
			"siteAvailableLocales",
			_getSiteAvailableLocalesJSONArray(themeDisplay.getLocale())
		).put(
			"siteDefaultLocaleId",
			LanguageUtil.getLanguageId(_siteDefaultLocale)
		).put(
			"translatedLanguages", _translatedLanguages
		).build();

		httpServletRequest.setAttribute(
			"liferay-frontend:languages:data", data);
	}

	private JSONArray _getAvailableLocalesJSONArray(Locale locale) {
		JSONArray availableLocalesJSONArray = JSONFactoryUtil.createJSONArray();

		for (Locale availableLocale : LanguageUtil.getAvailableLocales()) {
			JSONObject languageObject = JSONUtil.put(
				"displayName", availableLocale.getDisplayName(locale)
			).put(
				"localeId", LocaleUtil.toLanguageId(availableLocale)
			);

			availableLocalesJSONArray.put(languageObject);
		}

		return availableLocalesJSONArray;
	}

	private String _getDefaultLanguageId(ThemeDisplay themeDisplay) {
		try {
			Company company = themeDisplay.getCompany();

			return LanguageUtil.getLanguageId(company.getLocale());
		}
		catch (PortalException portalException) {
			_log.error(portalException, portalException);

			return LanguageUtil.getLanguageId(LocaleUtil.getDefault());
		}
	}

	private JSONArray _getSiteAvailableLocalesJSONArray(Locale locale) {
		JSONArray siteAvailableLocalesJSONArray =
			JSONFactoryUtil.createJSONArray();

		for (Locale siteAvailableLocale : _siteAvailableLocales) {
			siteAvailableLocalesJSONArray.put(
				JSONUtil.put(
					"displayName", siteAvailableLocale.getDisplayName(locale)
				).put(
					"localeId", LocaleUtil.toLanguageId(siteAvailableLocale)
				));
		}

		return siteAvailableLocalesJSONArray;
	}

	private static final Log _log = LogFactoryUtil.getLog(LanguagesTag.class);

	private boolean _inheritLocales;
	private Locale[] _siteAvailableLocales;
	private Locale _siteDefaultLocale;
	private Object _translatedLanguages;

}