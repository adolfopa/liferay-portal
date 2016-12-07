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

package com.liferay.portal.service.impl;

import com.liferay.portal.kernel.exception.MembershipRequestCommentsException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.MembershipRequest;
import com.liferay.portal.kernel.model.MembershipRequestConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.service.base.MembershipRequestLocalServiceBaseImpl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Jorge Ferrer
 */
public class MembershipRequestLocalServiceImpl
	extends MembershipRequestLocalServiceBaseImpl {

	@Override
	public MembershipRequest addMembershipRequest(
			long userId, long groupId, String comments,
			ServiceContext serviceContext)
		throws PortalException {

		User user = userPersistence.findByPrimaryKey(userId);

		validate(comments);

		long membershipRequestId = counterLocalService.increment();

		MembershipRequest membershipRequest =
			membershipRequestPersistence.create(membershipRequestId);

		membershipRequest.setCompanyId(user.getCompanyId());
		membershipRequest.setUserId(userId);
		membershipRequest.setCreateDate(new Date());
		membershipRequest.setGroupId(groupId);
		membershipRequest.setComments(comments);
		membershipRequest.setStatusId(
			MembershipRequestConstants.STATUS_PENDING);

		membershipRequestPersistence.update(membershipRequest);

		return membershipRequest;
	}

	@Override
	public void deleteMembershipRequests(long groupId) {
		List<MembershipRequest> membershipRequests =
			membershipRequestPersistence.findByGroupId(groupId);

		for (MembershipRequest membershipRequest : membershipRequests) {
			deleteMembershipRequest(membershipRequest);
		}
	}

	@Override
	public void deleteMembershipRequests(long groupId, long statusId) {
		List<MembershipRequest> membershipRequests =
			membershipRequestPersistence.findByG_S(groupId, statusId);

		for (MembershipRequest membershipRequest : membershipRequests) {
			deleteMembershipRequest(membershipRequest);
		}
	}

	@Override
	public void deleteMembershipRequestsByUserId(long userId) {
		List<MembershipRequest> membershipRequests =
			membershipRequestPersistence.findByUserId(userId);

		for (MembershipRequest membershipRequest : membershipRequests) {
			deleteMembershipRequest(membershipRequest);
		}
	}

	@Override
	public List<MembershipRequest> getMembershipRequests(
		long userId, long groupId, long statusId) {

		return membershipRequestPersistence.findByG_U_S(
			groupId, userId, statusId);
	}

	@Override
	public boolean hasMembershipRequest(
		long userId, long groupId, long statusId) {

		List<MembershipRequest> membershipRequests = getMembershipRequests(
			userId, groupId, statusId);

		if (membershipRequests.isEmpty()) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public List<MembershipRequest> search(
		long groupId, int status, int start, int end) {

		return membershipRequestPersistence.findByG_S(
			groupId, status, start, end);
	}

	@Override
	public List<MembershipRequest> search(
		long groupId, int status, int start, int end,
		OrderByComparator<MembershipRequest> obc) {

		return membershipRequestPersistence.findByG_S(
			groupId, status, start, end, obc);
	}

	@Override
	public int searchCount(long groupId, int status) {
		return membershipRequestPersistence.countByG_S(groupId, status);
	}

	@Override
	public void updateStatus(
			long replierUserId, long membershipRequestId, String replyComments,
			long statusId, boolean addUserToGroup,
			ServiceContext serviceContext)
		throws PortalException {

		validate(replyComments);

		MembershipRequest membershipRequest =
			membershipRequestPersistence.findByPrimaryKey(membershipRequestId);

		membershipRequest.setReplyComments(replyComments);
		membershipRequest.setReplyDate(new Date());

		if (replierUserId != 0) {
			membershipRequest.setReplierUserId(replierUserId);
		}
		else {
			long defaultUserId = userLocalService.getDefaultUserId(
				membershipRequest.getCompanyId());

			membershipRequest.setReplierUserId(defaultUserId);
		}

		membershipRequest.setStatusId(statusId);

		membershipRequestPersistence.update(membershipRequest);

		if ((statusId == MembershipRequestConstants.STATUS_APPROVED) &&
			addUserToGroup) {

			long[] addUserIds = new long[] {membershipRequest.getUserId()};

			userLocalService.addGroupUsers(
				membershipRequest.getGroupId(), addUserIds);
		}
	}

	/**
	 * @deprecated As of 7.0.0, with no direct replacement.
	 */
	@Deprecated
	protected List<Long> getGroupAdministratorUserIds(long groupId)
		throws PortalException {

		return Collections.emptyList();
	}

	/**
	 * @deprecated As of 7.0.0, with no direct replacement.
	 */
	@Deprecated
	protected void notify(
			long userId, MembershipRequest membershipRequest,
			String subjectProperty, String bodyProperty,
			ServiceContext serviceContext)
		throws PortalException {
	}

	/**
	 * @deprecated As of 7.0.0, with no direct replacement.
	 */
	@Deprecated
	protected void notifyGroupAdministrators(
			MembershipRequest membershipRequest, ServiceContext serviceContext)
		throws PortalException {
	}

	protected void validate(String comments) throws PortalException {
		if (Validator.isNull(comments) || Validator.isNumber(comments)) {
			throw new MembershipRequestCommentsException();
		}
	}

}