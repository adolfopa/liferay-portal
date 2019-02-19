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

package com.liferay.headless.collaboration.internal.resource.v1_0;

import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryService;
import com.liferay.headless.collaboration.dto.v1_0.Comment;
import com.liferay.headless.collaboration.internal.dto.v1_0.util.CommentUtil;
import com.liferay.headless.collaboration.internal.odata.entity.v1_0.CommentEntityModel;
import com.liferay.headless.collaboration.resource.v1_0.CommentResource;
import com.liferay.message.boards.model.MBMessage;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.comment.CommentManager;
import com.liferay.portal.kernel.comment.Discussion;
import com.liferay.portal.kernel.comment.DiscussionComment;
import com.liferay.portal.kernel.comment.DiscussionPermission;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.SearchResultPermissionFilterFactory;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.search.filter.TermFilter;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.resource.EntityModelResource;
import com.liferay.portal.vulcan.util.SearchUtil;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MultivaluedMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Javier Gamarra
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/comment.properties",
	scope = ServiceScope.PROTOTYPE, service = CommentResource.class
)
public class CommentResourceImpl
	extends BaseCommentResourceImpl implements EntityModelResource {

	@Override
	public Page<Comment> getBlogPostingCommentsPage(
			Long blogPostingId, Filter filter, Pagination pagination,
			Sort[] sorts)
		throws Exception {

		BlogsEntry blogsEntry = _blogsEntryService.getEntry(blogPostingId);

		Discussion discussion = _commentManager.getDiscussion(
			blogsEntry.getUserId(), blogsEntry.getGroupId(),
			BlogsEntry.class.getName(), blogPostingId, null);

		DiscussionComment rootDiscussionComment =
			discussion.getRootDiscussionComment();

		return _getComments(
			rootDiscussionComment.getCommentId(), filter, pagination, sorts);
	}

	@Override
	public Comment getComment(Long commentId) throws Exception {
		com.liferay.portal.kernel.comment.Comment comment =
			_commentManager.fetchComment(commentId);

		_checkViewPermission(comment);

		return CommentUtil.toComment(comment, _portal);
	}

	@Override
	public Page<Comment> getCommentCommentsPage(
			Long commentId, Filter filter, Pagination pagination, Sort[] sorts)
		throws Exception {

		return _getComments(commentId, filter, pagination, sorts);
	}

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap) {
		return _commentEntityModel;
	}

	private void _checkViewPermission(
			com.liferay.portal.kernel.comment.Comment comment)
		throws Exception {

		if (comment == null) {
			throw new NotFoundException();
		}

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		DiscussionPermission discussionPermission =
			_commentManager.getDiscussionPermission(permissionChecker);

		discussionPermission.checkViewPermission(
			permissionChecker.getCompanyId(), comment.getGroupId(),
			comment.getClassName(), comment.getClassPK());
	}

	private Page<Comment> _getComments(
			Long parentCommentId, Filter filter, Pagination pagination,
			Sort[] sorts)
		throws SearchException {

		List<com.liferay.portal.kernel.comment.Comment> comments =
			new ArrayList<>();

		Hits hits = SearchUtil.getHits(
			filter, _indexerRegistry.nullSafeGetIndexer(MBMessage.class),
			pagination,
			booleanQuery -> {
				BooleanFilter booleanFilter =
					booleanQuery.getPreBooleanFilter();

				booleanFilter.add(
					new TermFilter(
						"parentMessageId", String.valueOf(parentCommentId)),
					BooleanClauseOccur.MUST);
			},
			queryConfig -> {
				queryConfig.setSelectedFieldNames(Field.CLASS_PK);
			},
			searchContext -> {
				searchContext.setAttribute("discussion", Boolean.TRUE);
				searchContext.setCompanyId(company.getCompanyId());
				searchContext.setAttribute(
					"searchPermissionContext", StringPool.BLANK);
			},
			_searchResultPermissionFilterFactory, sorts);

		for (Document document : hits.getDocs()) {
			com.liferay.portal.kernel.comment.Comment comment =
				_commentManager.fetchComment(
					GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK)));

			comments.add(comment);
		}

		return Page.of(
			transform(
				comments, comment -> CommentUtil.toComment(comment, _portal)),
			pagination, comments.size());
	}

	private static final CommentEntityModel _commentEntityModel =
		new CommentEntityModel();

	@Reference
	private BlogsEntryService _blogsEntryService;

	@Reference
	private CommentManager _commentManager;

	@Reference
	private IndexerRegistry _indexerRegistry;

	@Reference
	private Portal _portal;

	@Reference
	private SearchResultPermissionFilterFactory
		_searchResultPermissionFilterFactory;

}