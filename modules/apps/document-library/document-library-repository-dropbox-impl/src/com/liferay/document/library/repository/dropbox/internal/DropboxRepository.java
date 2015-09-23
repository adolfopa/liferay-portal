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
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;

import com.liferay.document.library.repository.dropbox.internal.model.DropboxFileEntry;
import com.liferay.document.library.repository.dropbox.internal.model.DropboxFileVersion;
import com.liferay.document.library.repository.dropbox.internal.model.DropboxFolder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.service.CompanyLocalService;
import com.liferay.portal.service.RepositoryEntryLocalService;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portlet.asset.service.AssetEntryLocalService;
import com.liferay.portlet.documentlibrary.service.DLAppHelperLocalService;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalService;
import com.liferay.repository.external.CredentialsProvider;
import com.liferay.repository.external.ExtRepository;
import com.liferay.repository.external.ExtRepositoryAdapter;
import com.liferay.repository.external.ExtRepositoryFileEntry;
import com.liferay.repository.external.ExtRepositoryFileVersion;
import com.liferay.repository.external.ExtRepositoryFileVersionDescriptor;
import com.liferay.repository.external.ExtRepositoryFolder;
import com.liferay.repository.external.ExtRepositoryObject;
import com.liferay.repository.external.ExtRepositoryObjectType;
import com.liferay.repository.external.ExtRepositorySearchResult;
import com.liferay.repository.external.search.ExtRepositoryQueryMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import jodd.io.StreamUtil;

/**
 * @author Adolfo Pérez
 */
public class DropboxRepository
	extends ExtRepositoryAdapter implements ExtRepository {

	public DropboxRepository(
		long companyId, long groupId, long repositoryId,
		UnicodeProperties typeSettingsProperties,
		DbxClientFactory dbxClientFactory,
		AssetEntryLocalService assetEntryLocalService,
		CompanyLocalService companyLocalService,
		DLAppHelperLocalService dlAppHelperLocalService,
		DLFolderLocalService dlFolderLocalService,
		RepositoryEntryLocalService repositoryEntryLocalService,
		UserLocalService userLocalService) {

		super(null);

		setAssetEntryLocalService(assetEntryLocalService);
		setCompanyId(companyId);
		setCompanyLocalService(companyLocalService);
		setDLAppHelperLocalService(dlAppHelperLocalService);
		setDLFolderLocalService(dlFolderLocalService);
		setGroupId(groupId);
		setRepositoryId(repositoryId);
		setRepositoryEntryLocalService(repositoryEntryLocalService);
		setTypeSettingsProperties(typeSettingsProperties);
		setUserLocalService(userLocalService);

		_dbxClientFactory = dbxClientFactory;
	}

	@Override
	public ExtRepositoryFileEntry addExtRepositoryFileEntry(
			String extRepositoryParentFolderKey, String mimeType, String title,
			String description, String changeLog, InputStream inputStream)
		throws PortalException {

		File tempFile = null;
		InputStream is = null;

		try {
			DbxClient dbxClient = getDbxClient();

			tempFile = FileUtil.createTempFile(inputStream);

			is = new FileInputStream(tempFile);

			DbxEntry.File dbxFile = dbxClient.uploadFile(
				getDropboxPath(extRepositoryParentFolderKey, title),
				DbxWriteMode.add(), tempFile.length(), inputStream);

			return new DropboxFileEntry(dbxFile);
		}
		catch (DbxException | IOException e) {
			throw new PortalException(e);
		}
		finally {
			StreamUtil.close(is);
			FileUtil.delete(tempFile);
		}
	}

	@Override
	public ExtRepositoryFolder addExtRepositoryFolder(
			String extRepositoryParentFolderKey, String name,
			String description)
		throws PortalException {

		try {
			DbxClient dbxClient = getDbxClient();

			DbxEntry.Folder dbxFolder = dbxClient.createFolder(
				getDropboxPath(extRepositoryParentFolderKey, name));

			return new DropboxFolder(dbxFolder);
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public ExtRepositoryFileVersion cancelCheckOut(
			String extRepositoryFileEntryKey)
		throws PortalException {

		throw new UnsupportedOperationException(
			"Cancel check-out is not supported for Dropbox repositories");
	}

	@Override
	public void checkInExtRepositoryFileEntry(
			String extRepositoryFileEntryKey, boolean createMajorVersion,
			String changeLog)
		throws PortalException {

		throw new UnsupportedOperationException(
			"Check-in is not supported for Dropbox repositories");
	}

	@Override
	public ExtRepositoryFileEntry checkOutExtRepositoryFileEntry(
			String extRepositoryFileEntryKey)
		throws PortalException {

		throw new UnsupportedOperationException(
			"Check-out is not supported for Dropbox repositories");
	}

	@Override
	public <T extends ExtRepositoryObject> T copyExtRepositoryObject(
			ExtRepositoryObjectType<T> extRepositoryObjectType,
			String extRepositoryFileEntryKey, String newExtRepositoryFolderKey,
			String newTitle)
		throws PortalException {

		try {
			DbxClient dbxClient = getDbxClient();

			DbxEntry dbxEntry = dbxClient.copy(
				extRepositoryFileEntryKey,
				getDropboxPath(newExtRepositoryFolderKey, newTitle));

			return (T)createExtRepositoryObject(dbxEntry);
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public void deleteExtRepositoryObject(
			ExtRepositoryObjectType<? extends ExtRepositoryObject>
				extRepositoryObjectType,
			String extRepositoryObjectKey)
		throws PortalException {

		try {
			DbxClient dbxClient = getDbxClient();

			dbxClient.delete(extRepositoryObjectKey);
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public InputStream getContentStream(
			ExtRepositoryFileEntry extRepositoryFileEntry)
		throws PortalException {

		DropboxFileEntry dropboxFileEntry =
			(DropboxFileEntry)extRepositoryFileEntry;

		return getContentStream(dropboxFileEntry.getDbxFile());
	}

	@Override
	public InputStream getContentStream(
			ExtRepositoryFileVersion extRepositoryFileVersion)
		throws PortalException {

		DropboxFileVersion dropboxFileVersion =
			(DropboxFileVersion)extRepositoryFileVersion;

		return getContentStream(dropboxFileVersion.getDbxFile());
	}

	@Override
	public ExtRepositoryFileVersion getExtRepositoryFileVersion(
			ExtRepositoryFileEntry extRepositoryFileEntry, String version)
		throws PortalException {

		DropboxFileEntry dropboxFileEntry =
			(DropboxFileEntry)extRepositoryFileEntry;

		DbxEntry.File dbxFile = dropboxFileEntry.getDbxFile();

		return new DropboxFileVersion(dbxFile, version);
	}

	@Override
	public ExtRepositoryFileVersionDescriptor
		getExtRepositoryFileVersionDescriptor(
			String extRepositoryFileVersionKey) {

		int i = extRepositoryFileVersionKey.lastIndexOf(CharPool.AT);

		if (i == -1) {
			throw new IllegalArgumentException(
				"Dropbox repository version keys must be of the form " +
					"path@rev: " + extRepositoryFileVersionKey);
		}

		return new ExtRepositoryFileVersionDescriptor(
			extRepositoryFileVersionKey.substring(0, i),
			extRepositoryFileVersionKey.substring(i + 1));
	}

	@Override
	public List<ExtRepositoryFileVersion> getExtRepositoryFileVersions(
			ExtRepositoryFileEntry extRepositoryFileEntry)
		throws PortalException {

		try {
			DropboxFileEntry dropboxFileEntry =
				(DropboxFileEntry)extRepositoryFileEntry;

			DbxEntry.File dbxFile = dropboxFileEntry.getDbxFile();

			DbxClient dbxClient = getDbxClient();

			List<DbxEntry.File> revisions = dbxClient.getRevisions(
				dbxFile.path);

			List<ExtRepositoryFileVersion> extRepositoryFileVersions =
				new ArrayList<>(revisions.size());

			for (DbxEntry.File revision : revisions) {
				extRepositoryFileVersions.add(new DropboxFileVersion(revision));
			}

			return extRepositoryFileVersions;
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public <T extends ExtRepositoryObject> T getExtRepositoryObject(
			ExtRepositoryObjectType<T> extRepositoryObjectType,
			String extRepositoryObjectKey)
		throws PortalException {

		try {
			DbxClient dbxClient = getDbxClient();

			DbxEntry dbxEntry = dbxClient.getMetadata(extRepositoryObjectKey);

			if (!isOfType(dbxEntry, extRepositoryObjectType)) {
				throw new IllegalArgumentException();
			}

			return (T)createExtRepositoryObject(dbxEntry);
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public <T extends ExtRepositoryObject> T getExtRepositoryObject(
			ExtRepositoryObjectType<T> extRepositoryObjectType,
			String extRepositoryFolderKey, String title)
		throws PortalException {

		return getExtRepositoryObject(
			extRepositoryObjectType,
			getDropboxPath(extRepositoryFolderKey, title));
	}

	@Override
	public <T extends ExtRepositoryObject> List<T> getExtRepositoryObjects(
			ExtRepositoryObjectType<T> extRepositoryObjectType,
			String extRepositoryFolderKey)
		throws PortalException {

		try {
			DbxClient dbxClient = getDbxClient();

			DbxEntry.WithChildren metadataWithChildren =
				dbxClient.getMetadataWithChildren(extRepositoryFolderKey);

			List<T> extRepositoryObjects = new ArrayList<>();

			for (DbxEntry dbxEntry : metadataWithChildren.children) {
				if (isOfType(dbxEntry, extRepositoryObjectType)) {
					extRepositoryObjects.add(
						(T)createExtRepositoryObject(dbxEntry));
				}
			}

			return extRepositoryObjects;
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public int getExtRepositoryObjectsCount(
			ExtRepositoryObjectType<? extends ExtRepositoryObject>
				extRepositoryObjectType,
			String extRepositoryFolderKey)
		throws PortalException {

		try {
			DbxClient dbxClient = getDbxClient();

			DbxEntry.WithChildren metadataWithChildren =
				dbxClient.getMetadataWithChildren(extRepositoryFolderKey);

			int count = 0;

			for (DbxEntry dbxEntry : metadataWithChildren.children) {
				if (isOfType(dbxEntry, extRepositoryObjectType)) {
					count++;
				}
			}

			return count;
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public ExtRepositoryFolder getExtRepositoryParentFolder(
			ExtRepositoryObject extRepositoryObject)
		throws PortalException {

		try {
			DbxEntry dbxEntry = null;

			if (extRepositoryObject instanceof DropboxFileEntry) {
				dbxEntry = ((DropboxFileEntry)extRepositoryObject).getDbxFile();
			}
			else if (extRepositoryObject instanceof DropboxFolder) {
				dbxEntry = ((DropboxFolder)extRepositoryObject).getDbxFolder();
			}
			else {
				throw new IllegalArgumentException(
					"Only files and folders have parents: " +
						extRepositoryObject);
			}

			String parentObjectKey = getDropboxParentPath(dbxEntry);

			if (parentObjectKey == null) {
				return null;
			}

			DbxClient dbxClient = getDbxClient();

			DbxEntry parentDbxEntry = dbxClient.getMetadata(parentObjectKey);

			return new DropboxFolder(parentDbxEntry.asFolder());
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public String getRootFolderKey() throws PortalException {
		return StringPool.SLASH;
	}

	@Override
	public List<String> getSubfolderKeys(
			String extRepositoryFolderKey, boolean recurse)
		throws PortalException {

		try {
			DbxClient dbxClient = getDbxClient();

			List<String> subfolderKeys = new ArrayList<>();

			collectSubfolderKeys(
				extRepositoryFolderKey, dbxClient, subfolderKeys, recurse);

			return subfolderKeys;
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public void initRepository(
			UnicodeProperties typeSettingsProperties,
			CredentialsProvider credentialsProvider)
		throws PortalException {
	}

	@Override
	public <T extends ExtRepositoryObject> T moveExtRepositoryObject(
			ExtRepositoryObjectType<T> extRepositoryObjectType,
			String extRepositoryObjectKey, String newExtRepositoryFolderKey,
			String newTitle)
		throws PortalException {

		try {
			DbxClient dbxClient = getDbxClient();

			DbxEntry entry = dbxClient.move(
				extRepositoryObjectKey,
				getDropboxPath(newExtRepositoryFolderKey, newTitle));

			return (T)createExtRepositoryObject(entry);
		}
		catch (DbxException de) {
			throw new PortalException(de);
		}
	}

	@Override
	public List<ExtRepositorySearchResult<?>> search(
			SearchContext searchContext, Query query,
			ExtRepositoryQueryMapper extRepositoryQueryMapper)
		throws PortalException {

		throw new UnsupportedOperationException();
	}

	@Override
	public ExtRepositoryFileEntry updateExtRepositoryFileEntry(
			String extRepositoryFileEntryKey, String mimeType,
			InputStream inputStream)
		throws PortalException {

		File tempFile = null;
		InputStream is = null;

		try {
			DbxClient dbxClient = getDbxClient();

			DbxEntry metadata = dbxClient.getMetadata(
				extRepositoryFileEntryKey);

			DbxEntry.File fileMetadata = metadata.asFile();

			tempFile = FileUtil.createTempFile(inputStream);

			is = new FileInputStream(tempFile);

			DbxEntry.File file = dbxClient.uploadFile(
				extRepositoryFileEntryKey,
				DbxWriteMode.update(fileMetadata.rev), tempFile.length(), is);

			return new DropboxFileEntry(file);
		}
		catch (DbxException | IOException e) {
			throw new PortalException(e);
		}
		finally {
			StreamUtil.close(is);
			FileUtil.delete(tempFile);
		}
	}

	protected void collectSubfolderKeys(
			String path, DbxClient dbxClient, List<String> subfolderKeys,
			boolean recurse)
		throws DbxException {

		DbxEntry.WithChildren metadataWithChildren =
			dbxClient.getMetadataWithChildren(path);

		for (DbxEntry dbxEntry : metadataWithChildren.children) {
			if (dbxEntry.isFolder()) {
				subfolderKeys.add(dbxEntry.path);

				if (recurse) {
					collectSubfolderKeys(
						dbxEntry.path, dbxClient, subfolderKeys, recurse);
				}
			}
		}
	}

	protected ExtRepositoryObject createExtRepositoryObject(DbxEntry dbxEntry) {
		if (dbxEntry.isFile()) {
			return new DropboxFileEntry(dbxEntry.asFile());
		}

		if (dbxEntry.isFolder()) {
			return new DropboxFolder(dbxEntry.asFolder());
		}

		throw new IllegalArgumentException(
			"Expected file or folder, got " + dbxEntry);
	}

	protected String escapePathComponent(String pathComponent) {
		return pathComponent; // For the moment, we won't escape anything.
	}

	protected InputStream getContentStream(DbxEntry.File dbxFile)
		throws PortalException {

		return getContentStream(dbxFile, dbxFile.rev);
	}

	protected InputStream getContentStream(
			DbxEntry.File dbxFile, String revision)
		throws PortalException {

		DbxClient.Downloader downloader = null;

		try {
			DbxClient dbxClient = getDbxClient();

			downloader = dbxClient.startGetFile(dbxFile.path, revision);

			final File tempFile = FileUtil.createTempFile(downloader.body);

			return new FileInputStream(tempFile) {

				@Override
				public void close() throws IOException {
					super.close();

					FileUtil.delete(tempFile);
				}

			};
		}
		catch (IOException | DbxException e) {
			throw new PortalException(e);
		}
		finally {
			downloader.close();
		}
	}

	protected DbxClient getDbxClient() {
		return _dbxClientFactory.getDbxClient(
			getRepositoryId(), getTypeSettingsProperties());
	}

	protected String getDropboxParentPath(DbxEntry dbxEntry) {
		if (dbxEntry.path.equals(StringPool.SLASH)) {
			return null;
		}

		int i = dbxEntry.path.lastIndexOf(CharPool.SLASH);

		if (i == 0) {
			return StringPool.SLASH;
		}

		return dbxEntry.path.substring(0, i);
	}

	protected String getDropboxPath(
		String extRepositoryParentFolderKey, String extRepositoryObjectName) {

		String[] parentPathComponents = StringUtil.split(
			extRepositoryParentFolderKey, StringPool.SLASH);

		String[] extRepositoryObjectPathComponents =
			new String[parentPathComponents.length + 1];

		int i = 0;

		for (i = 0; i < parentPathComponents.length; i++) {
			extRepositoryObjectPathComponents[i] = escapePathComponent(
				parentPathComponents[i]);
		}

		extRepositoryObjectPathComponents[i] = escapePathComponent(
			extRepositoryObjectName);

		return StringUtil.merge(
			extRepositoryObjectPathComponents, StringPool.SLASH);
	}

	protected boolean isOfType(
		DbxEntry dbxEntry, ExtRepositoryObjectType extRepositoryObjectType) {

		if (extRepositoryObjectType == ExtRepositoryObjectType.FILE) {
			return dbxEntry.isFile();
		}

		if (extRepositoryObjectType == ExtRepositoryObjectType.FOLDER) {
			return dbxEntry.isFolder();
		}

		return true;
	}

	private final DbxClientFactory _dbxClientFactory;

}