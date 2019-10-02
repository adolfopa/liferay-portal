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

package com.liferay.layout.seo.web.internal.servlet.taglib;

import com.liferay.layout.seo.kernel.LayoutSEOLink;
import com.liferay.layout.seo.kernel.LayoutSEOLinkManagerUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.servlet.taglib.BaseDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alicia García
 */
@Component(service = DynamicInclude.class)
public class OpenGraphTopHeadDynamicInclude extends BaseDynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		PrintWriter printWriter = httpServletResponse.getWriter();

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		StringBundler sb = new StringBundler(15);
		Layout layout = themeDisplay.getLayout();

		String languageId = themeDisplay.getLanguageId();

		_appendOpenGraphTag(sb, "og:title", layout.getHTMLTitle(languageId));
		_appendOpenGraphTag(
			sb, "og:description", layout.getDescription(languageId));

		_appendOpenGraphTag(sb, "og:locale", languageId);

		try {
			String canonicalURL = _portal.getCanonicalURL(
				_portal.getCurrentCompleteURL(httpServletRequest), themeDisplay,
				layout, false, false);

			if (Validator.isNotNull(canonicalURL)) {
				_appendOpenGraphTag(sb, "og:url", canonicalURL);

				Map<Locale, String> alternateURLs = Collections.emptyMap();

				Set<Locale> availableLocales = LanguageUtil.getAvailableLocales(
					themeDisplay.getSiteGroupId());

				if (availableLocales.size() > 1) {
					alternateURLs = _portal.getAlternateURLs(
						canonicalURL, themeDisplay, layout);
				}

				for (LayoutSEOLink layoutSEOLink :
						LayoutSEOLinkManagerUtil.getLocalizedLayoutSEOLinks(
							layout, _portal.getLocale(httpServletRequest),
							canonicalURL, alternateURLs)) {

					_appendOpenGraphTag(
						sb, "og:locale:alternate", layoutSEOLink.getHrefLang());
				}
			}
		}
		catch (PortalException pe) {
			if (_log.isDebugEnabled()) {
				_log.debug(pe, pe);
			}
		}

		Group pageGroup = layout.getGroup();

		try {
			_appendOpenGraphTag(
				sb, "og:site_name", pageGroup.getDescriptiveName());
		}
		catch (PortalException pe) {
			if (_log.isDebugEnabled()) {
				_log.debug(pe, pe);
			}
		}

		printWriter.println(sb.toString());
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/top_head.jsp#pre");
	}

	private void _appendOpenGraphTag(
		StringBundler sb, String property, String content) {

		if (Validator.isNotNull(content)) {
			sb.append("<meta property=\"");
			sb.append(property);
			sb.append("\" content=\"");
			sb.append(content);
			sb.append("\">");
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OpenGraphTopHeadDynamicInclude.class);

	@Reference
	private Portal _portal;

}