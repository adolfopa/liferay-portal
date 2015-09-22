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

import com.liferay.document.library.repository.dropbox.internal.constants.DropboxRepositoryConstants;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.DefaultLocalRepositoryImpl;
import com.liferay.portal.kernel.repository.LocalRepository;
import com.liferay.portal.kernel.repository.Repository;
import com.liferay.portal.kernel.repository.RepositoryFactory;
import com.liferay.portal.service.CompanyLocalService;
import com.liferay.portal.service.RepositoryEntryLocalService;
import com.liferay.portal.service.RepositoryLocalService;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portlet.asset.service.AssetEntryLocalService;
import com.liferay.portlet.documentlibrary.service.DLAppHelperLocalService;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	immediate = true,
	property = {
		"repository.targetClassName=" + DropboxRepositoryConstants.DROPBOX_REPOSITORY_CLASS_NAME
	},
	service = RepositoryFactory.class
)
public class DropboxRepositoryFactory implements RepositoryFactory {

	@Override
	public LocalRepository createLocalRepository(long repositoryId)
		throws PortalException {

		return new DefaultLocalRepositoryImpl(createRepository(repositoryId));
	}

	@Override
	public Repository createRepository(long repositoryId)
		throws PortalException {

		com.liferay.portal.model.Repository repository =
			_repositoryLocalService.getRepository(repositoryId);

		return new DropboxRepository(
			repository.getCompanyId(), repository.getGroupId(), repositoryId,
			repository.getTypeSettingsProperties(), _dbxClientFactory,
			_assetEntryLocalService, _companyLocalService,
			_dlAppHelperLocalService, _dlFolderLocalService,
			_repositoryEntryLocalService, _userLocalService);
	}

	@Reference(unbind = "-")
	public void setAssetEntryLocalService(
		AssetEntryLocalService assetEntryLocalService) {

		_assetEntryLocalService = assetEntryLocalService;
	}

	@Reference(unbind = "-")
	public void setCompanyLocalService(
		CompanyLocalService companyLocalService) {

		_companyLocalService = companyLocalService;
	}

	@Reference(unbind = "-")
	public void setDbxClientFactory(DbxClientFactory dbxClientFactory) {
		_dbxClientFactory = dbxClientFactory;
	}

	@Reference(unbind = "-")
	public void setDLAppHelperLocalService(
		DLAppHelperLocalService dlAppHelperLocalService) {

		_dlAppHelperLocalService = dlAppHelperLocalService;
	}

	@Reference(unbind = "-")
	public void setDLFolderLocalService(
		DLFolderLocalService dlFolderLocalService) {

		_dlFolderLocalService = dlFolderLocalService;
	}

	@Reference(unbind = "-")
	public void setRepositoryEntryLocalService(
		RepositoryEntryLocalService repositoryEntryLocalService) {

		_repositoryEntryLocalService = repositoryEntryLocalService;
	}

	@Reference(unbind = "-")
	public void setRepositoryLocalService(
		RepositoryLocalService repositoryLocalService) {

		_repositoryLocalService = repositoryLocalService;
	}

	@Reference(unbind = "-")
	public void setUserLocalService(UserLocalService userLocalService) {
		_userLocalService = userLocalService;
	}

	private AssetEntryLocalService _assetEntryLocalService;
	private CompanyLocalService _companyLocalService;
	private DbxClientFactory _dbxClientFactory;
	private DLAppHelperLocalService _dlAppHelperLocalService;
	private DLFolderLocalService _dlFolderLocalService;
	private RepositoryEntryLocalService _repositoryEntryLocalService;
	private RepositoryLocalService _repositoryLocalService;
	private UserLocalService _userLocalService;

}