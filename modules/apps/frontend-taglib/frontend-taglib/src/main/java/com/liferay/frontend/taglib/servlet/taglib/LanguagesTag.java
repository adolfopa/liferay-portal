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
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Ambrín Chaudhary
 */
public class LanguagesTag extends BaseBarTag {

	public Locale[] getSiteAvailableLocales() {
		return _siteAvailableLocales;
	}

	public String getSiteDefaultLocaleId() {
		return _siteDefaultLocaleId;
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

	public void setSiteDefaultLocaleId(String siteDefaultLocaleId) {
		_siteDefaultLocaleId = siteDefaultLocaleId;
	}

	public Object getTranslatedLanguages() {
		return _translatedLanguages;
	}

	public void setTranslatedLanguages(Object translatedLanguages) {
		this._translatedLanguages = translatedLanguages;
	}


	@Override
	protected void cleanUp() {
		super.cleanUp();

		_inheritLocales = false;
		_siteAvailableLocales = null;
		_siteDefaultLocaleId = null;
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

		JSONArray availableLocalesJSONArray = getAvailableLocalesJSONArray(
			themeDisplay.getLocale());
		Locale defaultLocale = null;

		try {
			defaultLocale = themeDisplay.getCompany().getLocale();
		}
		catch (PortalException e) {
			e.printStackTrace();
		}

		JSONArray siteAvailableLocalesJSONArray =
			getSiteAvailableLocalesJSONArray(themeDisplay.getLocale());

		Map<String, Object> data = new HashMap<>();

		data.put("availableLocales", availableLocalesJSONArray);
		data.put("defaultLocaleId", LocaleUtil.toLanguageId(defaultLocale));
		data.put("inheritLocales", _inheritLocales);
		data.put("siteAvailableLocales", siteAvailableLocalesJSONArray);
		data.put("siteDefaultLocaleId", _siteDefaultLocaleId);
		data.put("translatedLanguages", _translatedLanguages);

		httpServletRequest.setAttribute(
			"liferay-frontend:languages:data", data);
	}

	private JSONArray getAvailableLocalesJSONArray(Locale locale) {
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

	private JSONArray getSiteAvailableLocalesJSONArray(Locale locale) {
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

	private boolean _inheritLocales;
	private Locale[] _siteAvailableLocales;
	private String _siteDefaultLocaleId;
	private Object _translatedLanguages;

}