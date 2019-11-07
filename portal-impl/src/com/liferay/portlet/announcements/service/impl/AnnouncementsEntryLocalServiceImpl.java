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
import com.liferay.announcements.kernel.exception.EntryExpirationDateException;
import com.liferay.announcements.kernel.exception.EntryTitleException;
import com.liferay.announcements.kernel.exception.EntryURLException;
import com.liferay.announcements.kernel.model.AnnouncementsEntry;
import com.liferay.petra.content.ContentUtil;
import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.interval.IntervalActionProcessor;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.service.UserNotificationDeliveryLocalService;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.SubscriptionSender;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.PropsValues;
import com.liferay.portlet.announcements.service.base.AnnouncementsEntryLocalServiceBaseImpl;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

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

		entry = announcementsEntryPersistence.update(entry);

		// Resources

		resourceLocalService.addResources(
			user.getCompanyId(), 0, user.getUserId(),
			AnnouncementsEntry.class.getName(), entry.getEntryId(), false,
			false, false);

		return entry;
	}

	@Override
	public void checkEntries() throws PortalException {
		Date now = new Date();

		Date previousCheckDate = new Date(
			now.getTime() - _ANNOUNCEMENTS_ENTRY_CHECK_INTERVAL);

		checkEntries(previousCheckDate, now);
	}

	@Override
	public void checkEntries(Date startDate, Date endDate)
		throws PortalException {

		List<AnnouncementsEntry> entries =
			announcementsEntryFinder.findByDisplayDate(endDate, startDate);

		if (_log.isDebugEnabled()) {
			_log.debug("Processing " + entries.size() + " entries");
		}

		for (AnnouncementsEntry entry : entries) {
			notifyUsers(entry);
		}
	}

	@Override
	public void deleteEntries(long companyId) {
		announcementsFlagPersistence.removeByCompanyId(companyId);

		announcementsEntryPersistence.removeByCompanyId(companyId);
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
	public void deleteEntries(long companyId, long classNameId, long classPK)
		throws PortalException {

		List<AnnouncementsEntry> entries =
			announcementsEntryPersistence.findByC_C_C(
				companyId, classNameId, classPK);

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

		User user = userLocalService.fetchUser(userId);

		if (user == null) {
			return Collections.emptyList();
		}

		return announcementsEntryFinder.findByScopes(
			user.getCompanyId(), userId, scopes, displayDateMonth,
			displayDateDay, displayDateYear, displayDateHour, displayDateMinute,
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, alert, flagValue, start,
			end);
	}

	@Override
	public List<AnnouncementsEntry> getEntries(
		long companyId, long classNameId, long classPK, boolean alert,
		int start, int end) {

		return announcementsEntryPersistence.findByC_C_C_A(
			companyId, classNameId, classPK, alert, start, end);
	}

	@Override
	public List<AnnouncementsEntry> getEntries(
		long userId, long classNameId, long[] classPKs, int displayDateMonth,
		int displayDateDay, int displayDateYear, int displayDateHour,
		int displayDateMinute, int expirationDateMonth, int expirationDateDay,
		int expirationDateYear, int expirationDateHour,
		int expirationDateMinute, boolean alert, int flagValue, int start,
		int end) {

		User user = userLocalService.fetchUser(userId);

		if (user == null) {
			return Collections.emptyList();
		}

		return announcementsEntryFinder.findByScope(
			user.getCompanyId(), userId, classNameId, classPKs,
			displayDateMonth, displayDateDay, displayDateYear, displayDateHour,
			displayDateMinute, expirationDateMonth, expirationDateDay,
			expirationDateYear, expirationDateHour, expirationDateMinute, alert,
			flagValue, start, end);
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

		User user = userLocalService.fetchUser(userId);

		if (user == null) {
			return 0;
		}

		return announcementsEntryFinder.countByScopes(
			user.getCompanyId(), userId, scopes, displayDateMonth,
			displayDateDay, displayDateYear, displayDateHour, displayDateMinute,
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, alert, flagValue);
	}

	@Override
	public int getEntriesCount(
		long companyId, long classNameId, long classPK, boolean alert) {

		return announcementsEntryPersistence.countByC_C_C_A(
			companyId, classNameId, classPK, alert);
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

		User user = userLocalService.fetchUser(userId);

		if (user == null) {
			return 0;
		}

		return announcementsEntryFinder.countByScope(
			user.getCompanyId(), userId, classNameId, classPKs,
			displayDateMonth, displayDateDay, displayDateYear, displayDateHour,
			displayDateMinute, expirationDateMonth, expirationDateDay,
			expirationDateYear, expirationDateHour, expirationDateMinute, alert,
			flagValue);
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

		entry = announcementsEntryPersistence.update(entry);

		// Flags

		announcementsFlagLocalService.deleteFlags(entry.getEntryId());

		return entry;
	}

	protected void notifyUsers(AnnouncementsEntry entry)
		throws PortalException {

		Company company = companyPersistence.findByPrimaryKey(
			entry.getCompanyId());

		String className = entry.getClassName();
		long classPK = entry.getClassPK();

		String toName = PropsValues.ANNOUNCEMENTS_EMAIL_TO_NAME;

		long teamId = 0;

		LinkedHashMap<String, Object> params = new LinkedHashMap<>();

		if (classPK > 0) {
			if (className.equals(Group.class.getName())) {
				Group group = groupPersistence.findByPrimaryKey(classPK);

				toName = group.getDescriptiveName();

				params.put("inherit", Boolean.TRUE);
				params.put("usersGroups", classPK);
			}
			else if (className.equals(Organization.class.getName())) {
				Organization organization =
					organizationPersistence.findByPrimaryKey(classPK);

				toName = organization.getName();

				params.put("usersOrgsTree", ListUtil.fromArray(organization));
			}
			else if (className.equals(Role.class.getName())) {
				Role role = rolePersistence.findByPrimaryKey(classPK);

				toName = role.getName();

				if (role.getType() == RoleConstants.TYPE_REGULAR) {
					params.put("inherit", Boolean.TRUE);
					params.put("usersRoles", classPK);
				}
				else if (role.isTeam()) {
					teamId = role.getClassPK();
				}
				else {
					params.put("userGroupRole", new Long[] {0L, classPK});
				}
			}
			else if (className.equals(UserGroup.class.getName())) {
				UserGroup userGroup = userGroupPersistence.findByPrimaryKey(
					classPK);

				toName = userGroup.getName();

				params.put("usersUserGroups", classPK);
			}
		}

		if (className.equals(User.class.getName())) {
			User user = userPersistence.findByPrimaryKey(classPK);

			if (Validator.isNull(user.getEmailAddress())) {
				return;
			}

			notifyUsers(
				ListUtil.fromArray(user), entry, user.getEmailAddress(),
				user.getFullName(), entry.getType());
		}
		else {
			String toAddress = PropsValues.ANNOUNCEMENTS_EMAIL_TO_ADDRESS;

			notifyUsers(entry, teamId, params, toName, toAddress, company);
		}
	}

	protected void notifyUsers(
			final AnnouncementsEntry entry, final long teamId,
			final LinkedHashMap<String, Object> params, final String toName,
			final String toAddress, final Company company)
		throws PortalException {

		int total = 0;

		if (teamId > 0) {
			total = userLocalService.getTeamUsersCount(teamId);
		}
		else {
			total = userLocalService.searchCount(
				company.getCompanyId(), null, WorkflowConstants.STATUS_APPROVED,
				params);
		}

		final IntervalActionProcessor<Void> intervalActionProcessor =
			new IntervalActionProcessor<>(total);

		intervalActionProcessor.setPerformIntervalActionMethod(
			(start, end) -> {
				List<User> users = null;

				if (teamId > 0) {
					users = userLocalService.getTeamUsers(teamId, start, end);
				}
				else {
					users = userLocalService.search(
						company.getCompanyId(), null,
						WorkflowConstants.STATUS_APPROVED, params, start, end,
						(OrderByComparator<User>)null);
				}

				notifyUsers(
					users, entry, toAddress, toName, entry.getType());

				intervalActionProcessor.incrementStart(users.size());

				return null;
			});

		intervalActionProcessor.performIntervalActions();
	}

	protected void notifyUsers(
			List<User> users, AnnouncementsEntry entry,
			String toAddress, String toName, String notificationType)
		throws PortalException {

		if (_log.isDebugEnabled()) {
			_log.debug("Notifying " + users.size() + " users");
		}

		SubscriptionSender subscriptionSender = new SubscriptionSender();

		for (User user : users) {
			subscriptionSender.addRuntimeSubscribers(
				user.getEmailAddress(), user.getFullName());
		}

		Class<?> clazz = getClass();

		String body = ContentUtil.get(
			clazz.getClassLoader(), PropsValues.ANNOUNCEMENTS_EMAIL_BODY);
		String fromAddress = PrefsPropsUtil.getStringFromNames(
			entry.getCompanyId(), PropsKeys.ANNOUNCEMENTS_EMAIL_FROM_ADDRESS,
			PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);
		String fromName = PrefsPropsUtil.getStringFromNames(
			entry.getCompanyId(), PropsKeys.ANNOUNCEMENTS_EMAIL_FROM_NAME,
			PropsKeys.ADMIN_EMAIL_FROM_NAME);
		String subject = ContentUtil.get(
			clazz.getClassLoader(), PropsValues.ANNOUNCEMENTS_EMAIL_SUBJECT);

		subscriptionSender.setBody(body);
		subscriptionSender.setCompanyId(entry.getCompanyId());
		subscriptionSender.setContextAttribute(
			"[$ENTRY_CONTENT$]", entry.getContent(), false);
		subscriptionSender.setContextAttributes(
			"[$ENTRY_ID$]", entry.getEntryId(), "[$ENTRY_TITLE$]",
			entry.getTitle(), "[$ENTRY_URL$]", entry.getUrl());
		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setLocalizedContextAttributeWithFunction(
			"[$ENTRY_TYPE$]",
			notificationLocale ->
				LanguageUtil.get(notificationLocale, entry.getType()));
		subscriptionSender.setLocalizedContextAttributeWithFunction(
			"[$PORTLET_NAME$]",
			notificationLocale -> LanguageUtil.get(
				notificationLocale,
				entry.isAlert() ? "alert" : "announcement"));
		subscriptionSender.setMailId("announcements_entry", entry.getEntryId());
		subscriptionSender.setNotificationType(notificationType);

		String portletId = PortletProviderUtil.getPortletId(
			AnnouncementsEntry.class.getName(), PortletProvider.Action.VIEW);

		subscriptionSender.setPortletId(portletId);

		subscriptionSender.setScopeGroupId(entry.getGroupId());
		subscriptionSender.setSubject(subject);

		subscriptionSender.addRuntimeSubscribers(toAddress, toName);

		subscriptionSender.flushNotificationsAsync();
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

	public static final String[] ANNOUNCEMENTS_ENTRY_TYPES = PropsUtil.getArray(
		PropsKeys.ANNOUNCEMENTS_ENTRY_TYPES);

	private static final long _ANNOUNCEMENTS_ENTRY_CHECK_INTERVAL =
		PropsValues.ANNOUNCEMENTS_ENTRY_CHECK_INTERVAL * Time.MINUTE;

	private static final Log _log = LogFactoryUtil.getLog(
		AnnouncementsEntryLocalServiceImpl.class);

	@BeanReference(type = UserNotificationDeliveryLocalService.class)
	protected UserNotificationDeliveryLocalService
		_userNotificationDeliveryLocalService;

}