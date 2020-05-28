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

import javax.servlet.http.HttpServletRequest;

/**
 * @author Ambrín Chaudhary
 */
public class LanguagesTag extends BaseBarTag {

	public boolean isInheritLocales() {
		return _inheritLocales;
	}

	public void setInheritLocales(boolean inheritLocales) {
		_inheritLocales = inheritLocales;
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();

		_inheritLocales = false;
	}

	@Override
	protected String getPage() {
		return "/languages/page.jsp";
	}

	@Override
	protected void setAttributes(HttpServletRequest httpServletRequest) {
		httpServletRequest.setAttribute(
			"liferay-frontend:languages:inheritLocales", _inheritLocales);
	}

	private boolean _inheritLocales;
}
