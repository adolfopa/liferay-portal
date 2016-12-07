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

package com.liferay.subscription.internal.service;

import com.liferay.announcements.kernel.model.AnnouncementsDelivery;
import com.liferay.announcements.kernel.model.AnnouncementsEntry;
import com.liferay.announcements.kernel.service.AnnouncementsDeliveryLocalService;
import com.liferay.announcements.kernel.service.AnnouncementsEntryLocalService;
import com.liferay.announcements.kernel.service.AnnouncementsEntryLocalServiceWrapper;
import com.liferay.petra.content.ContentUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.interval.IntervalActionProcessor;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.SubscriptionSender;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.PropsValues;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(immediate = true, service = ServiceWrapper.class)
public class ModularAnnouncementsEntryLocalServiceWrapper
	extends AnnouncementsEntryLocalServiceWrapper {

	public ModularAnnouncementsEntryLocalServiceWrapper() {
		super(null);
	}

	public ModularAnnouncementsEntryLocalServiceWrapper(
		AnnouncementsEntryLocalService announcementsEntryLocalService) {

		super(announcementsEntryLocalService);
	}

	@Override
	public void checkEntries() throws PortalException {
		Date now = new Date();

		if (_previousCheckDate == null) {
			_previousCheckDate = new Date(
				now.getTime() - _ANNOUNCEMENTS_ENTRY_CHECK_INTERVAL);
		}

		DynamicQuery dynamicQuery = dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.between(
				"checkDate", now, _previousCheckDate));

		List<AnnouncementsEntry> entries = dynamicQuery(dynamicQuery);

		if (_log.isDebugEnabled()) {
			_log.debug("Processing " + entries.size() + " entries");
		}

		for (AnnouncementsEntry entry : entries) {
			notifyUsers(entry);
		}

		_previousCheckDate = now;
	}

	protected void notifyUsers(AnnouncementsEntry entry)
		throws PortalException {

		Company company = _companyLocalService.getCompany(entry.getCompanyId());

		String className = entry.getClassName();
		long classPK = entry.getClassPK();

		String toName = PropsValues.ANNOUNCEMENTS_EMAIL_TO_NAME;
		String toAddress = PropsValues.ANNOUNCEMENTS_EMAIL_TO_ADDRESS;

		long teamId = 0;

		LinkedHashMap<String, Object> params = new LinkedHashMap<>();

		params.put("announcementsDeliveryEmailOrSms", entry.getType());

		if (classPK > 0) {
			if (className.equals(Group.class.getName())) {
				Group group = _groupLocalService.getGroup(classPK);

				toName = group.getDescriptiveName();

				params.put("inherit", Boolean.TRUE);
				params.put("usersGroups", classPK);
			}
			else if (className.equals(Organization.class.getName())) {
				Organization organization =
					_organizationLocalService.getOrganization(classPK);

				toName = organization.getName();

				params.put(
					"usersOrgsTree",
					ListUtil.fromArray(new Organization[] {organization}));
			}
			else if (className.equals(Role.class.getName())) {
				Role role = _roleLocalService.getRole(classPK);

				toName = role.getName();

				if (role.getType() == RoleConstants.TYPE_REGULAR) {
					params.put("inherit", Boolean.TRUE);
					params.put("usersRoles", classPK);
				}
				else if (role.isTeam()) {
					teamId = role.getClassPK();
				}
				else {
					params.put(
						"userGroupRole", new Long[] {Long.valueOf(0), classPK});
				}
			}
			else if (className.equals(UserGroup.class.getName())) {
				UserGroup userGroup = _userGroupLocalService.getUserGroup(
					classPK);

				toName = userGroup.getName();

				params.put("usersUserGroups", classPK);
			}
		}

		if (className.equals(User.class.getName())) {
			User user = _userLocalService.getUser(classPK);

			if (Validator.isNull(user.getEmailAddress())) {
				return;
			}

			notifyUsers(
				ListUtil.fromArray(new User[] {user}), entry,
				company.getLocale(), user.getEmailAddress(),
				user.getFullName());
		}
		else {
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
			total = _userLocalService.getTeamUsersCount(teamId);
		}
		else {
			total = _userLocalService.searchCount(
				company.getCompanyId(), null, WorkflowConstants.STATUS_APPROVED,
				params);
		}

		final IntervalActionProcessor<Void> intervalActionProcessor =
			new IntervalActionProcessor<>(total);

		intervalActionProcessor.setPerformIntervalActionMethod(
			new IntervalActionProcessor.PerformIntervalActionMethod<Void>() {

				@Override
				public Void performIntervalAction(int start, int end)
					throws PortalException {

					List<User> users = null;

					if (teamId > 0) {
						users = _userLocalService.getTeamUsers(
							teamId, start, end);
					}
					else {
						users = _userLocalService.search(
							company.getCompanyId(), null,
							WorkflowConstants.STATUS_APPROVED, params, start,
							end, (OrderByComparator<User>)null);
					}

					notifyUsers(
						users, entry, company.getLocale(), toAddress, toName);

					intervalActionProcessor.incrementStart(users.size());

					return null;
				}

			});

		intervalActionProcessor.performIntervalActions();
	}

	protected void notifyUsers(
			List<User> users, AnnouncementsEntry entry, Locale locale,
			String toAddress, String toName)
		throws PortalException {

		if (_log.isDebugEnabled()) {
			_log.debug("Notifying " + users.size() + " users");
		}

		boolean notifyUsers = false;

		SubscriptionSender subscriptionSender = new SubscriptionSender();

		for (User user : users) {
			AnnouncementsDelivery announcementsDelivery =
				_announcementsDeliveryLocalService.getUserDelivery(
					user.getUserId(), entry.getType());

			if (announcementsDelivery.isEmail()) {
				subscriptionSender.addRuntimeSubscribers(
					user.getEmailAddress(), user.getFullName());

				notifyUsers = true;
			}

			if (announcementsDelivery.isSms()) {
				String smsSn = user.getContact().getSmsSn();

				subscriptionSender.addRuntimeSubscribers(
					smsSn, user.getFullName());

				notifyUsers = true;
			}
		}

		if (!notifyUsers) {
			return;
		}

		String body = ContentUtil.get(
			PortalClassLoaderUtil.getClassLoader(),
			PropsValues.ANNOUNCEMENTS_EMAIL_BODY);
		String fromAddress = PrefsPropsUtil.getStringFromNames(
			entry.getCompanyId(), PropsKeys.ANNOUNCEMENTS_EMAIL_FROM_ADDRESS,
			PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);
		String fromName = PrefsPropsUtil.getStringFromNames(
			entry.getCompanyId(), PropsKeys.ANNOUNCEMENTS_EMAIL_FROM_NAME,
			PropsKeys.ADMIN_EMAIL_FROM_NAME);
		String subject = ContentUtil.get(
			PortalClassLoaderUtil.getClassLoader(),
			PropsValues.ANNOUNCEMENTS_EMAIL_SUBJECT);

		subscriptionSender.setBody(body);
		subscriptionSender.setCompanyId(entry.getCompanyId());
		subscriptionSender.setContextAttribute(
			"[$ENTRY_CONTENT$]", entry.getContent(), false);
		subscriptionSender.setContextAttributes(
			"[$ENTRY_ID$]", entry.getEntryId(), "[$ENTRY_TITLE$]",
			entry.getTitle(), "[$ENTRY_URL$]", entry.getUrl());
		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setLocalizedContextAttribute(
			"[$ENTRY_TYPE$]",
			notificationLocale ->
				LanguageUtil.get(notificationLocale, entry.getType()));
		subscriptionSender.setLocalizedContextAttribute(
			"[$PORTLET_NAME$]",
			notificationLocale -> LanguageUtil.get(
				notificationLocale,
				entry.isAlert() ? "alert" : "announcement"));
		subscriptionSender.setMailId("announcements_entry", entry.getEntryId());

		String portletId = PortletProviderUtil.getPortletId(
			AnnouncementsEntry.class.getName(), PortletProvider.Action.VIEW);

		subscriptionSender.setPortletId(portletId);

		subscriptionSender.setScopeGroupId(entry.getGroupId());
		subscriptionSender.setSubject(subject);

		subscriptionSender.addRuntimeSubscribers(toAddress, toName);

		subscriptionSender.flushNotificationsAsync();
	}

	@Reference(unbind = "-")
	protected void setAnnouncementsDeliveryLocalService(
		AnnouncementsDeliveryLocalService announcementsDeliveryLocalService) {

		_announcementsDeliveryLocalService = announcementsDeliveryLocalService;
	}

	@Reference(unbind = "-")
	protected void setCompanyLocalService(
		CompanyLocalService companyLocalService) {

		_companyLocalService = companyLocalService;
	}

	@Reference(unbind = "-")
	protected void setGroupLocalService(GroupLocalService groupLocalService) {
		_groupLocalService = groupLocalService;
	}

	@Reference(unbind = "-")
	protected void setOrganizationLocalService(
		OrganizationLocalService organizationLocalService) {

		_organizationLocalService = organizationLocalService;
	}

	@Reference(unbind = "-")
	protected void setRoleLocalService(RoleLocalService roleLocalService) {
		_roleLocalService = roleLocalService;
	}

	@Reference(unbind = "-")
	protected void setUserGroupLocalService(
		UserGroupLocalService userGroupLocalService) {

		_userGroupLocalService = userGroupLocalService;
	}

	@Reference(unbind = "-")
	protected void setUserLocalService(UserLocalService userLocalService) {
		_userLocalService = userLocalService;
	}

	private static final long _ANNOUNCEMENTS_ENTRY_CHECK_INTERVAL =
		PropsValues.ANNOUNCEMENTS_ENTRY_CHECK_INTERVAL * Time.MINUTE;

	private static final Log _log = LogFactoryUtil.getLog(
		ModularAnnouncementsEntryLocalServiceWrapper.class);

	private AnnouncementsDeliveryLocalService
		_announcementsDeliveryLocalService;
	private CompanyLocalService _companyLocalService;
	private GroupLocalService _groupLocalService;
	private OrganizationLocalService _organizationLocalService;
	private Date _previousCheckDate;
	private RoleLocalService _roleLocalService;
	private UserGroupLocalService _userGroupLocalService;
	private UserLocalService _userLocalService;

}