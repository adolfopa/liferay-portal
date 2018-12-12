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

import java.util.Collection;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Adolfo Pérez
 */
@XmlRootElement
public class BulkAssetEntryCommonTagsModel {

	public BulkAssetEntryCommonTagsModel(
		String description, Set<String> tagNames) {

		_description = description;
		_tagNames = tagNames;
		_status = "success";
	}

	public BulkAssetEntryCommonTagsModel(Throwable throwable) {
		_description = throwable.getMessage();
		_status = "error";
		_tagNames = null;
	}

	public String getDescription() {
		return _description;
	}

	public String getStatus() {
		return _status;
	}

	public Collection<String> getTagNames() {
		return _tagNames;
	}

	private final String _description;
	private final String _status;
	private final Set<String> _tagNames;

}