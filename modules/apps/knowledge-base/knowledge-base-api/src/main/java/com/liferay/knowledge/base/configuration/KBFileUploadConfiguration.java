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

package com.liferay.knowledge.base.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Roberto Díaz
 */
@ExtendedObjectClassDefinition(category = "knowledge-base")
@Meta.OCD(
	id = "com.liferay.knowledge.base.configuration.KBFileUploadConfiguration",
	localization = "content/Language",
	name = "knowledge-base-file-uploads-configuration-name"
)
public interface KBFileUploadConfiguration {

	@Meta.AD(
		deflt = "*",
		description = "allowed-knowledge-base-attachment-mime-types-description",
		name = "allowed-knowledge-base-attachment-mime-types", required = false
	)
	public String[] attachmentMimeTypes();

	@Meta.AD(
		deflt = "104857600", name = "maximum-knowledge-base-attachment-size",
		required = false
	)
	public long attachmentMaxSize();

}