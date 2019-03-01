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

package com.liferay.knowledge.base.web.internal;

import com.liferay.knowledge.base.model.KBFolder;
import com.liferay.knowledge.base.service.KBFolderServiceUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * @author István András Dézsi
 */
public class KBFolderSelectorItem {

	public static List<KBFolderSelectorItem> fromKBFolder(
		long groupId, long kbFolderId) {

		List<KBFolderSelectorItem> kbFolderSelectorItems = new LinkedList<>();

		try {
			List<KBFolder> kbFolders = KBFolderServiceUtil.getKBFolders(
				groupId, kbFolderId, QueryUtil.ALL_POS, QueryUtil.ALL_POS);

			for (KBFolder kbFolder : kbFolders) {
				_addKBFolderSelectorItemsToList(
					kbFolderSelectorItems, groupId, kbFolder, 1);
			}
		}
		catch (PortalException pe) {
			if (_log.isDebugEnabled()) {
				_log.debug(pe, pe);
			}
		}

		return kbFolderSelectorItems;
	}

	public int getDepth() {
		return _depth;
	}

	public KBFolder getKBFolder() {
		return _kbFolder;
	}

	public void setDepth(int depth) {
		_depth = depth;
	}

	public void setKBFolder(KBFolder kbFolder) {
		_kbFolder = kbFolder;
	}

	private static void _addKBFolderSelectorItemsToList(
			List<KBFolderSelectorItem> kbFolderSelectorItems, long groupId,
			KBFolder kbFolder, int depth)
		throws PortalException {

		if (kbFolder.isEmpty()) {
			return;
		}

		KBFolderSelectorItem kbFolderSelectorItem = new KBFolderSelectorItem();

		kbFolderSelectorItem.setDepth(depth);
		kbFolderSelectorItem.setKBFolder(kbFolder);

		kbFolderSelectorItems.add(kbFolderSelectorItem);

		List<KBFolder> childKBFolders = KBFolderServiceUtil.getKBFolders(
			groupId, kbFolder.getKbFolderId(), QueryUtil.ALL_POS,
			QueryUtil.ALL_POS);

		for (KBFolder childKBFolder : childKBFolders) {
			_addKBFolderSelectorItemsToList(
				kbFolderSelectorItems, groupId, childKBFolder, depth + 1);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		KBFolderSelectorItem.class);

	private int _depth;
	private KBFolder _kbFolder;

}