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

package com.liferay.portlet.announcements.service.impl;

import com.liferay.announcements.kernel.exception.EntryContentException;
import com.liferay.announcements.kernel.exception.EntryDisplayDateException;
import com.liferay.announcements.kernel.exception.EntryExpirationDateException;
import com.liferay.announcements.kernel.exception.EntryTitleException;
import com.liferay.announcements.kernel.exception.EntryURLException;
import com.liferay.announcements.kernel.model.AnnouncementsEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portlet.announcements.service.base.AnnouncementsEntryLocalServiceBaseImpl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author Brian Wing Shun Chan
 * @author Raymond Augé
 * @author Roberto Díaz
 */
public class AnnouncementsEntryLocalServiceImpl
	extends AnnouncementsEntryLocalServiceBaseImpl {

	@Override
	public AnnouncementsEntry addEntry(
			long userId, long classNameId, long classPK, String title,
			String content, String url, String type, Date displayDate,
			Date expirationDate, int priority, boolean alert)
		throws PortalException {

		// Entry

		User user = userPersistence.findByPrimaryKey(userId);

		validate(title, content, url, displayDate, expirationDate);

		long entryId = counterLocalService.increment();

		AnnouncementsEntry entry = announcementsEntryPersistence.create(
			entryId);

		entry.setCompanyId(user.getCompanyId());
		entry.setUserId(user.getUserId());
		entry.setUserName(user.getFullName());
		entry.setClassNameId(classNameId);
		entry.setClassPK(classPK);
		entry.setTitle(title);
		entry.setContent(content);
		entry.setUrl(url);
		entry.setType(type);
		entry.setDisplayDate(displayDate);
		entry.setExpirationDate(expirationDate);
		entry.setPriority(priority);
		entry.setAlert(alert);

		announcementsEntryPersistence.update(entry);

		// Resources

		resourceLocalService.addResources(
			user.getCompanyId(), 0, user.getUserId(),
			AnnouncementsEntry.class.getName(), entry.getEntryId(), false,
			false, false);

		return entry;
	}

	/**
	 * @deprecated As of 7.0.0, replaced by {@link #addEntry(long, long, long,
	 *             String, String, String, String, Date, Date, int, boolean)}
	 */
	@Deprecated
	@Override
	public AnnouncementsEntry addEntry(
			long userId, long classNameId, long classPK, String title,
			String content, String url, String type, int displayDateMonth,
			int displayDateDay, int displayDateYear, int displayDateHour,
			int displayDateMinute, boolean displayImmediately,
			int expirationDateMonth, int expirationDateDay,
			int expirationDateYear, int expirationDateHour,
			int expirationDateMinute, int priority, boolean alert)
		throws PortalException {

		User user = userPersistence.findByPrimaryKey(userId);

		Date displayDate = new Date();

		if (!displayImmediately) {
			displayDate = PortalUtil.getDate(
				displayDateMonth, displayDateDay, displayDateYear,
				displayDateHour, displayDateMinute, user.getTimeZone(),
				EntryDisplayDateException.class);
		}

		Date expirationDate = PortalUtil.getDate(
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, user.getTimeZone(),
			EntryExpirationDateException.class);

		return addEntry(
			userId, classNameId, classPK, title, content, url, type,
			displayDate, expirationDate, priority, alert);
	}

	@Override
	public void checkEntries() throws PortalException {
	}

	@Override
	public void deleteEntries(long classNameId, long classPK)
		throws PortalException {

		List<AnnouncementsEntry> entries =
			announcementsEntryPersistence.findByC_C(classNameId, classPK);

		for (AnnouncementsEntry entry : entries) {
			deleteEntry(entry);
		}
	}

	@Override
	public void deleteEntry(AnnouncementsEntry entry) throws PortalException {

		// Entry

		announcementsEntryPersistence.remove(entry);

		// Resources

		resourceLocalService.deleteResource(
			entry.getCompanyId(), AnnouncementsEntry.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL, entry.getEntryId());

		// Flags

		announcementsFlagLocalService.deleteFlags(entry.getEntryId());
	}

	@Override
	public void deleteEntry(long entryId) throws PortalException {
		AnnouncementsEntry entry =
			announcementsEntryPersistence.findByPrimaryKey(entryId);

		deleteEntry(entry);
	}

	@Override
	public List<AnnouncementsEntry> getEntries(
		long userId, LinkedHashMap<Long, long[]> scopes, boolean alert,
		int flagValue, int start, int end) {

		return getEntries(
			userId, scopes, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, alert, flagValue,
			start, end);
	}

	@Override
	public List<AnnouncementsEntry> getEntries(
		long userId, LinkedHashMap<Long, long[]> scopes, int displayDateMonth,
		int displayDateDay, int displayDateYear, int displayDateHour,
		int displayDateMinute, int expirationDateMonth, int expirationDateDay,
		int expirationDateYear, int expirationDateHour,
		int expirationDateMinute, boolean alert, int flagValue, int start,
		int end) {

		return announcementsEntryFinder.findByScopes(
			userId, scopes, displayDateMonth, displayDateDay, displayDateYear,
			displayDateHour, displayDateMinute, expirationDateMonth,
			expirationDateDay, expirationDateYear, expirationDateHour,
			expirationDateMinute, alert, flagValue, start, end);
	}

	@Override
	public List<AnnouncementsEntry> getEntries(
		long classNameId, long classPK, boolean alert, int start, int end) {

		return announcementsEntryPersistence.findByC_C_A(
			classNameId, classPK, alert, start, end);
	}

	@Override
	public List<AnnouncementsEntry> getEntries(
		long userId, long classNameId, long[] classPKs, int displayDateMonth,
		int displayDateDay, int displayDateYear, int displayDateHour,
		int displayDateMinute, int expirationDateMonth, int expirationDateDay,
		int expirationDateYear, int expirationDateHour,
		int expirationDateMinute, boolean alert, int flagValue, int start,
		int end) {

		return announcementsEntryFinder.findByScope(
			userId, classNameId, classPKs, displayDateMonth, displayDateDay,
			displayDateYear, displayDateHour, displayDateMinute,
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, alert, flagValue, start,
			end);
	}

	@Override
	public int getEntriesCount(
		long userId, LinkedHashMap<Long, long[]> scopes, boolean alert,
		int flagValue) {

		return getEntriesCount(
			userId, scopes, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, alert, flagValue);
	}

	@Override
	public int getEntriesCount(
		long userId, LinkedHashMap<Long, long[]> scopes, int displayDateMonth,
		int displayDateDay, int displayDateYear, int displayDateHour,
		int displayDateMinute, int expirationDateMonth, int expirationDateDay,
		int expirationDateYear, int expirationDateHour,
		int expirationDateMinute, boolean alert, int flagValue) {

		return announcementsEntryFinder.countByScopes(
			userId, scopes, displayDateMonth, displayDateDay, displayDateYear,
			displayDateHour, displayDateMinute, expirationDateMonth,
			expirationDateDay, expirationDateYear, expirationDateHour,
			expirationDateMinute, alert, flagValue);
	}

	@Override
	public int getEntriesCount(long classNameId, long classPK, boolean alert) {
		return announcementsEntryPersistence.countByC_C_A(
			classNameId, classPK, alert);
	}

	@Override
	public int getEntriesCount(
		long userId, long classNameId, long[] classPKs, boolean alert,
		int flagValue) {

		return getEntriesCount(
			userId, classNameId, classPKs, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, alert,
			flagValue);
	}

	@Override
	public int getEntriesCount(
		long userId, long classNameId, long[] classPKs, int displayDateMonth,
		int displayDateDay, int displayDateYear, int displayDateHour,
		int displayDateMinute, int expirationDateMonth, int expirationDateDay,
		int expirationDateYear, int expirationDateHour,
		int expirationDateMinute, boolean alert, int flagValue) {

		return announcementsEntryFinder.countByScope(
			userId, classNameId, classPKs, displayDateMonth, displayDateDay,
			displayDateYear, displayDateHour, displayDateMinute,
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, alert, flagValue);
	}

	@Override
	public AnnouncementsEntry getEntry(long entryId) throws PortalException {
		return announcementsEntryPersistence.findByPrimaryKey(entryId);
	}

	@Override
	public List<AnnouncementsEntry> getUserEntries(
		long userId, int start, int end) {

		return announcementsEntryPersistence.findByUserId(userId, start, end);
	}

	@Override
	public int getUserEntriesCount(long userId) {
		return announcementsEntryPersistence.countByUserId(userId);
	}

	/**
	 * @deprecated As of 7.0.0, replaced by {@link #updateEntry(long, String,
	 *             String, String, String, Date, Date, int)}
	 */
	@Deprecated
	@Override
	public AnnouncementsEntry updateEntry(
			long userId, long entryId, String title, String content, String url,
			String type, int displayDateMonth, int displayDateDay,
			int displayDateYear, int displayDateHour, int displayDateMinute,
			boolean displayImmediately, int expirationDateMonth,
			int expirationDateDay, int expirationDateYear,
			int expirationDateHour, int expirationDateMinute, int priority)
		throws PortalException {

		// Entry

		User user = userPersistence.findByPrimaryKey(userId);

		Date displayDate = new Date();

		if (!displayImmediately) {
			displayDate = PortalUtil.getDate(
				displayDateMonth, displayDateDay, displayDateYear,
				displayDateHour, displayDateMinute, user.getTimeZone(),
				EntryDisplayDateException.class);
		}

		Date expirationDate = PortalUtil.getDate(
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, user.getTimeZone(),
			EntryExpirationDateException.class);

		return updateEntry(
			entryId, title, content, url, type, displayDate, expirationDate,
			priority);
	}

	@Override
	public AnnouncementsEntry updateEntry(
			long entryId, String title, String content, String url, String type,
			Date displayDate, Date expirationDate, int priority)
		throws PortalException {

		validate(title, content, url, displayDate, expirationDate);

		AnnouncementsEntry entry =
			announcementsEntryPersistence.findByPrimaryKey(entryId);

		entry.setTitle(title);
		entry.setContent(content);
		entry.setUrl(url);
		entry.setType(type);
		entry.setDisplayDate(displayDate);
		entry.setExpirationDate(expirationDate);
		entry.setPriority(priority);

		announcementsEntryPersistence.update(entry);

		// Flags

		announcementsFlagLocalService.deleteFlags(entry.getEntryId());

		return entry;
	}

	/**
	 * @deprecated As of 7.0.0, with no direct replacement.
	 */
	@Deprecated
	protected void notifyUsers(AnnouncementsEntry entry)
		throws PortalException {
	}

	/**
	 * @deprecated As of 7.0.0, with no direct replacement.
	 */
	@Deprecated
	protected void notifyUsers(
			final AnnouncementsEntry entry, final long teamId,
			final LinkedHashMap<String, Object> params, final String toName,
			final String toAddress, final Company company)
		throws PortalException {
	}

	/**
	 * @deprecated As of 7.0.0, with no direct replacement.
	 */
	@Deprecated
	protected void notifyUsers(
			List<User> users, AnnouncementsEntry entry, Locale locale,
			String toAddress, String toName)
		throws PortalException {
	}

	protected void validate(
			String title, String content, String url, Date displayDate,
			Date expirationDate)
		throws PortalException {

		if (Validator.isNull(title)) {
			throw new EntryTitleException();
		}

		if (Validator.isNull(content)) {
			throw new EntryContentException();
		}

		if (Validator.isNotNull(url) && !Validator.isUrl(url)) {
			throw new EntryURLException();
		}

		if ((expirationDate != null) &&
			(expirationDate.before(new Date()) ||
			 ((displayDate != null) && expirationDate.before(displayDate)))) {

			throw new EntryExpirationDateException(
				"Expiration date " + expirationDate + " is in the past");
		}
	}

}