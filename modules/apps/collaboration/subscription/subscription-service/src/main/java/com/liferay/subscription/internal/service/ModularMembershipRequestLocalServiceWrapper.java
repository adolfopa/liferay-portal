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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.MembershipRequest;
import com.liferay.portal.kernel.model.MembershipRequestConstants;
import com.liferay.portal.kernel.model.Resource;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroupRole;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.MembershipRequestLocalService;
import com.liferay.portal.kernel.service.MembershipRequestLocalServiceWrapper;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.UserGroupRoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.SubscriptionSender;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.ResourcePermissionUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(immediate = true, service = ServiceWrapper.class)
public class ModularMembershipRequestLocalServiceWrapper
	extends MembershipRequestLocalServiceWrapper {

	public ModularMembershipRequestLocalServiceWrapper() {
		super(null);
	}

	public ModularMembershipRequestLocalServiceWrapper(
		MembershipRequestLocalService membershipRequestLocalService) {

		super(membershipRequestLocalService);
	}

	@Override
	public MembershipRequest addMembershipRequest(
			long userId, long groupId, String comments,
			ServiceContext serviceContext)
		throws PortalException {

		MembershipRequest membershipRequest = super.addMembershipRequest(
			userId, groupId, comments, serviceContext);

		notifyGroupAdministrators(membershipRequest, serviceContext);

		return membershipRequest;
	}

	@Override
	public void updateStatus(
			long replierUserId, long membershipRequestId, String replyComments,
			long statusId, boolean addUserToGroup,
			ServiceContext serviceContext)
		throws PortalException {

		super.updateStatus(
			replierUserId, membershipRequestId, replyComments, statusId,
			addUserToGroup, serviceContext);

		MembershipRequest membershipRequest = getMembershipRequest(
			membershipRequestId);

		if (replierUserId != 0) {
			notify(
				membershipRequest.getUserId(), membershipRequest,
				PropsKeys.SITES_EMAIL_MEMBERSHIP_REPLY_SUBJECT,
				PropsKeys.SITES_EMAIL_MEMBERSHIP_REPLY_BODY, serviceContext);
		}
	}

	protected List<Long> getGroupAdministratorUserIds(long groupId)
		throws PortalException {

		Set<Long> userIds = new LinkedHashSet<>();

		Group group = _groupLocalService.getGroup(groupId);
		String modelResource = Group.class.getName();

		List<Role> roles = ListUtil.copy(
			ResourceActionsUtil.getRoles(
				group.getCompanyId(), group, modelResource, null));

		List<Role> teamRoles = _roleLocalService.getTeamRoles(groupId);

		roles.addAll(teamRoles);

		Resource resource = _resourceLocalService.getResource(
			group.getCompanyId(), modelResource,
			ResourceConstants.SCOPE_INDIVIDUAL, String.valueOf(groupId));

		List<String> actions = ResourceActionsUtil.getResourceActions(
			Group.class.getName());

		for (Role role : roles) {
			String roleName = role.getName();

			if (roleName.equals(RoleConstants.OWNER)) {
				continue;
			}

			if ((roleName.equals(RoleConstants.ORGANIZATION_ADMINISTRATOR) ||
				 roleName.equals(RoleConstants.ORGANIZATION_OWNER)) &&
				!group.isOrganization()) {

				continue;
			}

			if (roleName.equals(RoleConstants.SITE_ADMINISTRATOR) ||
				roleName.equals(RoleConstants.SITE_OWNER) ||
				roleName.equals(RoleConstants.ORGANIZATION_ADMINISTRATOR) ||
				roleName.equals(RoleConstants.ORGANIZATION_OWNER)) {

				Role curRole = _roleLocalService.getRole(
					group.getCompanyId(), roleName);

				List<UserGroupRole> userGroupRoles =
					_userGroupRoleLocalService.getUserGroupRolesByGroupAndRole(
						groupId, curRole.getRoleId());

				for (UserGroupRole userGroupRole : userGroupRoles) {
					userIds.add(userGroupRole.getUserId());
				}
			}

			List<String> currentIndividualActions = new ArrayList<>();
			List<String> currentGroupActions = new ArrayList<>();
			List<String> currentGroupTemplateActions = new ArrayList<>();
			List<String> currentCompanyActions = new ArrayList<>();

			ResourcePermissionUtil.populateResourcePermissionActionIds(
				groupId, role, resource, actions, currentIndividualActions,
				currentGroupActions, currentGroupTemplateActions,
				currentCompanyActions);

			if (currentIndividualActions.contains(ActionKeys.ASSIGN_MEMBERS) ||
				currentGroupActions.contains(ActionKeys.ASSIGN_MEMBERS) ||
				currentGroupTemplateActions.contains(
					ActionKeys.ASSIGN_MEMBERS) ||
				currentCompanyActions.contains(ActionKeys.ASSIGN_MEMBERS)) {

				List<UserGroupRole> currentUserGroupRoles =
					_userGroupRoleLocalService.getUserGroupRolesByGroupAndRole(
						groupId, role.getRoleId());

				for (UserGroupRole userGroupRole : currentUserGroupRoles) {
					userIds.add(userGroupRole.getUserId());
				}
			}
		}

		return new ArrayList<>(userIds);
	}

	protected void notify(
			long userId, MembershipRequest membershipRequest,
			String subjectProperty, String bodyProperty,
			ServiceContext serviceContext)
		throws PortalException {

		User user = _userLocalService.getUser(userId);
		User requestUser = _userLocalService.getUser(
			membershipRequest.getUserId());

		String fromName = PrefsPropsUtil.getStringFromNames(
			membershipRequest.getCompanyId(), PropsKeys.SITES_EMAIL_FROM_NAME,
			PropsKeys.ADMIN_EMAIL_FROM_NAME);

		String fromAddress = PrefsPropsUtil.getStringFromNames(
			membershipRequest.getCompanyId(),
			PropsKeys.SITES_EMAIL_FROM_ADDRESS,
			PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

		String toName = user.getFullName();
		String toAddress = user.getEmailAddress();

		String subject = PrefsPropsUtil.getContent(
			membershipRequest.getCompanyId(), subjectProperty);

		String body = PrefsPropsUtil.getContent(
			membershipRequest.getCompanyId(), bodyProperty);

		final String statusKey;

		if (membershipRequest.getStatusId() ==
				MembershipRequestConstants.STATUS_APPROVED) {

			statusKey = "approved";
		}
		else if (membershipRequest.getStatusId() ==
					MembershipRequestConstants.STATUS_DENIED) {

			statusKey = "denied";
		}
		else {
			statusKey = "pending";
		}

		SubscriptionSender subscriptionSender = new SubscriptionSender();

		subscriptionSender.setBody(body);
		subscriptionSender.setCompanyId(membershipRequest.getCompanyId());
		subscriptionSender.setContextAttributes(
			"[$COMMENTS$]", membershipRequest.getComments(),
			"[$REPLY_COMMENTS$]", membershipRequest.getReplyComments(),
			"[$REQUEST_USER_ADDRESS$]", requestUser.getEmailAddress(),
			"[$REQUEST_USER_NAME$]", requestUser.getFullName(),
			"[$USER_ADDRESS$]", user.getEmailAddress(), "[$USER_NAME$]",
			user.getFullName());
		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setLocalizedContextAttribute(
			"[$STATUS$]", locale -> LanguageUtil.get(locale, statusKey));
		subscriptionSender.setMailId(
			"membership_request", membershipRequest.getMembershipRequestId());
		subscriptionSender.setScopeGroupId(membershipRequest.getGroupId());
		subscriptionSender.setServiceContext(serviceContext);
		subscriptionSender.setSubject(subject);

		subscriptionSender.addRuntimeSubscribers(toAddress, toName);

		subscriptionSender.flushNotificationsAsync();
	}

	protected void notifyGroupAdministrators(
			MembershipRequest membershipRequest, ServiceContext serviceContext)
		throws PortalException {

		List<Long> userIds = getGroupAdministratorUserIds(
			membershipRequest.getGroupId());

		for (Long userId : userIds) {
			notify(
				userId, membershipRequest,
				PropsKeys.SITES_EMAIL_MEMBERSHIP_REQUEST_SUBJECT,
				PropsKeys.SITES_EMAIL_MEMBERSHIP_REQUEST_BODY, serviceContext);
		}
	}

	@Reference(unbind = "-")
	protected void setGroupLocalService(GroupLocalService groupLocalService) {
		_groupLocalService = groupLocalService;
	}

	@Reference(unbind = "-")
	protected void setResourceLocalService(
		ResourceLocalService resourceLocalService) {

		_resourceLocalService = resourceLocalService;
	}

	@Reference(unbind = "-")
	protected void setRoleLocalService(RoleLocalService roleLocalService) {
		_roleLocalService = roleLocalService;
	}

	@Reference(unbind = "-")
	protected void setUserGroupRoleLocalService(
		UserGroupRoleLocalService userGroupRoleLocalService) {

		_userGroupRoleLocalService = userGroupRoleLocalService;
	}

	@Reference(unbind = "-")
	protected void setUserLocalService(UserLocalService userLocalService) {
		_userLocalService = userLocalService;
	}

	private GroupLocalService _groupLocalService;
	private ResourceLocalService _resourceLocalService;
	private RoleLocalService _roleLocalService;
	private UserGroupRoleLocalService _userGroupRoleLocalService;
	private UserLocalService _userLocalService;

}