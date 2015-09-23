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

import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.repository.external.ExtRepositoryFileVersion;

import java.util.Date;

/**
 * @author Adolfo Pérez
 */
public class DropboxFileVersion implements ExtRepositoryFileVersion {

	public DropboxFileVersion(DbxEntry.File dbxFile) {
		this(dbxFile, dbxFile.rev);
	}

	public DropboxFileVersion(DbxEntry.File dbxFile, String revision) {
		_dbxFile = dbxFile;
		_revision = revision;
	}

	@Override
	public String getChangeLog() {
		return StringPool.BLANK;
	}

	@Override
	public Date getCreateDate() {
		return _dbxFile.lastModified;
	}

	public DbxEntry.File getDbxFile() {
		return _dbxFile;
	}

	@Override
	public String getExtRepositoryModelKey() {
		return _dbxFile.path + StringPool.AT + _revision;
	}

	@Override
	public String getMimeType() {
		return ContentTypes.APPLICATION_OCTET_STREAM;
	}

	@Override
	public String getOwner() {
		return null;
	}

	@Override
	public long getSize() {
		return _dbxFile.numBytes;
	}

	@Override
	public String getVersion() {
		return _revision;
	}

	private final DbxEntry.File _dbxFile;
	private final String _revision;

}