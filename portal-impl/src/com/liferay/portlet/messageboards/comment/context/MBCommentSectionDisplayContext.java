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

package com.liferay.portlet.messageboards.comment.context;

import com.liferay.portal.kernel.comment.Comment;
import com.liferay.portal.kernel.comment.CommentManager;
import com.liferay.portal.kernel.comment.DiscussionPermission;
import com.liferay.portal.kernel.comment.context.CommentSectionDisplayContext;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portlet.messageboards.comment.context.util.DiscussionRequestHelper;
import com.liferay.portlet.messageboards.comment.context.util.DiscussionTaglibHelper;

/**
 * @author Adolfo Pérez
 */
public class MBCommentSectionDisplayContext
	implements CommentSectionDisplayContext {

	public MBCommentSectionDisplayContext(
		DiscussionTaglibHelper discussionTaglibHelper,
		DiscussionRequestHelper discussionRequestHelper,
		DiscussionPermission discussionPermission,
		CommentManager commentManager, Comment rootComment) {

		_discussionTaglibHelper = discussionTaglibHelper;
		_discussionRequestHelper = discussionRequestHelper;
		_discussionPermission = discussionPermission;
		_commentManager = commentManager;
		_rootComment = rootComment;
	}

	@Override
	public boolean isControlsVisible() throws PortalException {
		if (_discussionTaglibHelper.isHideControls()) {
			return false;
		}

		return _discussionPermission.hasAddPermission(
			_discussionRequestHelper.getCompanyId(),
			_discussionRequestHelper.getScopeGroupId(),
			_discussionTaglibHelper.getPermissionClassName(),
			_discussionTaglibHelper.getPermissionClassPK(),
			_discussionTaglibHelper.getUserId());
	}

	@Override
	public boolean isDiscussionMaxComments() throws PortalException {
		if (_discussionMaxComments == null) {
			ServiceContext serviceContext = ServiceContextFactory.getInstance(
				_discussionRequestHelper.getRequest());

			_discussionMaxComments =
				_commentManager.isDiscussionMaxCommentsExceeded(
					_discussionTaglibHelper.getUserId(),
					_discussionRequestHelper.getScopeGroupId(),
					_discussionTaglibHelper.getClassName(),
					_discussionTaglibHelper.getClassPK(), serviceContext);
		}

		return _discussionMaxComments;
	}

	@Override
	public boolean isDiscussionVisible() throws PortalException {
		if (_rootComment == null) {
			return false;
		}

		if ((_rootComment.getThreadCommentCount() > 1) || hasViewPermission()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isMessageThreadVisible() throws PortalException {
		return _rootComment.getThreadCommentCount() > 1;
	}

	protected boolean hasViewPermission() throws PortalException {
		return _discussionPermission.hasViewPermission(
			_discussionRequestHelper.getCompanyId(),
			_discussionRequestHelper.getScopeGroupId(),
			_discussionTaglibHelper.getPermissionClassName(),
			_discussionTaglibHelper.getPermissionClassPK(),
			_discussionTaglibHelper.getUserId());
	}

	private final CommentManager _commentManager;
	private Boolean _discussionMaxComments;
	private final DiscussionPermission _discussionPermission;
	private final DiscussionRequestHelper _discussionRequestHelper;
	private final DiscussionTaglibHelper _discussionTaglibHelper;
	private final Comment _rootComment;

}