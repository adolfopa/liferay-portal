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

package com.liferay.document.library.repository.dropbox.internal.model;

import com.dropbox.core.DbxEntry;

import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.repository.external.ExtRepositoryFolder;

import java.util.Date;

/**
 * @author Adolfo Pérez
 */
public class DropboxFolder implements ExtRepositoryFolder {

	public DropboxFolder(DbxEntry.Folder folder) {
		_folder = folder;
	}

	@Override
	public boolean containsPermission(
		ExtRepositoryPermission extRepositoryPermission) {

		return true;
	}

	@Override
	public Date getCreateDate() {
		return new Date();
	}

	public DbxEntry.Folder getDbxFolder() {
		return _folder;
	}

	@Override
	public String getDescription() {
		return StringPool.BLANK;
	}

	@Override
	public String getExtension() {
		return StringPool.BLANK;
	}

	@Override
	public String getExtRepositoryModelKey() {
		return _folder.path;
	}

	@Override
	public Date getModifiedDate() {
		return new Date();
	}

	@Override
	public String getName() {
		String name = _folder.path;

		int i = name.lastIndexOf(CharPool.SLASH);

		if (i == -1) {
			return name;
		}

		return name.substring(i + 1);
	}

	@Override
	public String getOwner() {
		return null;
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public boolean isRoot() {
		return _folder.path.equals(StringPool.SLASH);
	}

	private final DbxEntry.Folder _folder;

}