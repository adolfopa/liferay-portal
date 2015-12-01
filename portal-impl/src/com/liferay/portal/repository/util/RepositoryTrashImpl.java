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

package com.liferay.portal.repository.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.LocalRepository;
import com.liferay.portal.kernel.repository.RepositoryProviderUtil;
import com.liferay.portal.kernel.repository.capabilities.TrashCapability;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.repository.util.RepositoryTrash;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionAttribute;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;

import java.util.concurrent.Callable;

/**
 * @author Adolfo Pérez
 */
public class RepositoryTrashImpl implements RepositoryTrash {

	@Override
	public FileEntry moveFileEntryFromTrash(
			final long userId, final long repositoryId, final long fileEntryId,
			final long newFolderId, final ServiceContext serviceContext)
		throws PortalException {

		try {
			return TransactionInvokerUtil.invoke(
				_transactionAttribute,
				new Callable<FileEntry>() {
					@Override
					public FileEntry call() throws PortalException {
						return doMoveFileEntryFromTrash(
							userId, repositoryId, fileEntryId, newFolderId,
							serviceContext);
					}
				});
		}
		catch (PortalException | SystemException e) {
			throw e;
		}
		catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	@Override
	public FileEntry moveFileEntryToTrash(
			final long userId, final long repositoryId, final long fileEntryId)
		throws PortalException {

		try {
			return TransactionInvokerUtil.invoke(
				_transactionAttribute,
				new Callable<FileEntry>() {
					@Override
					public FileEntry call() throws PortalException {
						return doMoveFileEntryToTrash(
							userId, repositoryId, fileEntryId);
					}
				});
		}
		catch (PortalException | SystemException e) {
			throw e;
		}
		catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	@Override
	public void restoreFileEntryFromTrash(
			final long userId, final long repositoryId, final long fileEntryId)
		throws PortalException {

		try {
			TransactionInvokerUtil.invoke(
				_transactionAttribute,
				new Callable<Void>() {
					@Override
					public Void call() throws PortalException {
						doRestoreFileEntryFromTrash(
							userId, repositoryId, fileEntryId);

						return null;
					}
				});
		}
		catch (PortalException | SystemException e) {
			throw e;
		}
		catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	protected FileEntry doMoveFileEntryFromTrash(
			long userId, long repositoryId, long fileEntryId, long newFolderId,
			ServiceContext serviceContext)
		throws PortalException {

		LocalRepository localRepository =
			RepositoryProviderUtil.getLocalRepository(repositoryId);

		TrashCapability trashCapability = localRepository.getCapability(
			TrashCapability.class);

		FileEntry fileEntry = localRepository.getFileEntry(fileEntryId);

		Folder newFolder = null;

		if (newFolderId != DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {
			newFolder = localRepository.getFolder(newFolderId);
		}

		return trashCapability.moveFileEntryFromTrash(
			userId, fileEntry, newFolder, serviceContext);
	}

	protected FileEntry doMoveFileEntryToTrash(
			long userId, long repositoryId, long fileEntryId)
		throws PortalException {

		LocalRepository localRepository =
			RepositoryProviderUtil.getLocalRepository(repositoryId);

		TrashCapability trashCapability = localRepository.getCapability(
			TrashCapability.class);

		FileEntry fileEntry = localRepository.getFileEntry(fileEntryId);

		return trashCapability.moveFileEntryToTrash(userId, fileEntry);
	}

	protected void doRestoreFileEntryFromTrash(
			long userId, long repositoryId, long fileEntryId)
		throws PortalException {

		LocalRepository localRepository =
			RepositoryProviderUtil.getLocalRepository(repositoryId);

		TrashCapability trashCapability = localRepository.getCapability(
			TrashCapability.class);

		FileEntry fileEntry = localRepository.getFileEntry(fileEntryId);

		trashCapability.restoreFileEntryFromTrash(userId, fileEntry);
	}

	private static final TransactionAttribute _transactionAttribute =
		TransactionAttribute.Factory.create(
			Propagation.REQUIRED, new Class<?>[] {PortalException.class});

}