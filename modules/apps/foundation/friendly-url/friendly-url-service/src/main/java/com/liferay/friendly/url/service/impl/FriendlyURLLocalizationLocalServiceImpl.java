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

package com.liferay.friendly.url.service.impl;

import aQute.bnd.annotation.ProviderType;

import com.liferay.friendly.url.model.FriendlyURL;
import com.liferay.friendly.url.model.FriendlyURLLocalization;
import com.liferay.friendly.url.service.base.FriendlyURLLocalizationLocalServiceBaseImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The implementation of the friendly url localization local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the {@link com.liferay.friendly.url.service.FriendlyURLLocalizationLocalService} interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see FriendlyURLLocalizationLocalServiceBaseImpl
 * @see com.liferay.friendly.url.service.FriendlyURLLocalizationLocalServiceUtil
 */
@ProviderType
public class FriendlyURLLocalizationLocalServiceImpl
	extends FriendlyURLLocalizationLocalServiceBaseImpl {

	/**
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never reference this class directly. Always use {@link com.liferay.friendly.url.service.FriendlyURLLocalizationLocalServiceUtil} to access the friendly url localization local service.
	 */

	@Override
	public FriendlyURLLocalization addFriendlyURLLocalization(
		long companyId, long groupId, long friendlyURLId, String urlTitle,
		String languageId) {

		long friendlyURLLocalizationId = counterLocalService.increment();

		FriendlyURLLocalization friendlyURLLocalization =
			friendlyURLLocalizationPersistence.create(
				friendlyURLLocalizationId);

		friendlyURLLocalization.setCompanyId(companyId);
		friendlyURLLocalization.setGroupId(groupId);
		friendlyURLLocalization.setFriendlyURLId(friendlyURLId);
		friendlyURLLocalization.setUrlTitle(urlTitle);
		friendlyURLLocalization.setLanguageId(languageId);

		friendlyURLLocalizationPersistence.update(friendlyURLLocalization);

		return friendlyURLLocalization;
	}

	@Override
	public FriendlyURLLocalization deleteFriendlyURLLocalization(
			long groupId, long friendlyURLId, String languageId)
		throws PortalException {

		return friendlyURLLocalizationPersistence.removeByG_F_L(
			groupId, friendlyURLId, languageId);
	}

	@Override
	public void deleteFriendlyURLLocalizations(long groupId, long friendlyURLId)
		throws PortalException {

		friendlyURLLocalizationPersistence.removeByG_F(groupId, friendlyURLId);
	}

	@Override
	public FriendlyURLLocalization fetchFriendlyURLLocalization(
		long companyId, long groupId, long classNameId, long classPK,
		String languageId) {

		FriendlyURL friendlyURL = friendlyURLPersistence.fetchByC_G_C_C_M(
			companyId, groupId, classNameId, classPK, true);

		if (friendlyURL == null) {
			return null;
		}

		return friendlyURLLocalizationPersistence.fetchByG_F_L(
			groupId, friendlyURL.getFriendlyURLId(), languageId);
	}

	@Override
	public FriendlyURLLocalization fetchFriendlyURLLocalization(
		long groupId, long friendlyURLId, String languageId) {

		return friendlyURLLocalizationPersistence.fetchByG_F_L(
			groupId, friendlyURLId, languageId);
	}

	@Override
	public int getFriendlyURLLocalizationCount(
		long groupId, long friendlyURLId) {

		return friendlyURLLocalizationPersistence.countByG_F(
			groupId, friendlyURLId);
	}

	@Override
	public List<FriendlyURLLocalization> updateFriendlyURLLocaliozations(
			long companyId, long groupId, long friendlyURLId,
			Map<Locale, String> urlTitleMap)
		throws PortalException {

		List<FriendlyURLLocalization> friendlyURLLocalizations =
			new ArrayList<>();

		for (Locale locale : LanguageUtil.getAvailableLocales(groupId)) {
			String urlTitle = urlTitleMap.get(locale);

			String languageId = LocaleUtil.toLanguageId(locale);

			if (Validator.isNull(urlTitle)) {
				deleteFriendlyURLLocalization(
					groupId, friendlyURLId, languageId);

				continue;
			}

			FriendlyURLLocalization friendlyURLLocalization =
				friendlyURLLocalizationPersistence.fetchByG_U_L(
					groupId, urlTitle, languageId);

			if (friendlyURLLocalization != null) {
				friendlyURLLocalizations.add(friendlyURLLocalization);

				continue;
			}

			friendlyURLLocalization = addFriendlyURLLocalization(
				companyId, groupId, friendlyURLId, urlTitle, languageId);

			friendlyURLLocalizations.add(friendlyURLLocalization);
		}

		return friendlyURLLocalizations;
	}

}