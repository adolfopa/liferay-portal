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

package com.liferay.bulk.rest.internal.model;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Adolfo Pérez
 */
@XmlRootElement
public class BulkAssetEntryUpdateTagsActionModel {

	public Map<String, String> getParameterMap() {
		return _parameterMap;
	}

	public List<String> getToAddTagNames() {
		return _toAddTagNames;
	}

	public List<String> getToRemoveTagNames() {
		return _toRemoveTagNames;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		_parameterMap = parameterMap;
	}

	public void setToAddTagNames(List<String> toAddTagNames) {
		_toAddTagNames = toAddTagNames;
	}

	public void setToRemoveTagNames(List<String> toRemoveTagNames) {
		_toRemoveTagNames = toRemoveTagNames;
	}

	private Map<String, String> _parameterMap;
	private List<String> _toAddTagNames;
	private List<String> _toRemoveTagNames;

}