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

package com.liferay.portal.upgrade.v7_0_0;

import com.liferay.counter.kernel.model.Counter;
import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author Adolfo Pérez
 */
public class UpgradeSocial extends UpgradeProcess {

	protected void addSocialActivitySets(long delta) throws Exception {
		try (Statement s = connection.createStatement()) {
			StringBundler sb = new StringBundler(6);

			sb.append("insert into SocialActivitySet select (activityId + ");
			sb.append(delta);
			sb.append(") as activitySetId, groupId, companyId, userId, ");
			sb.append("createDate, createDate AS modifiedDate, classNameId, ");
			sb.append("classPK, type_, extraData, 1 as activityCount from ");
			sb.append("SocialActivity where mirrorActivityId = 0");

			s.execute(sb.toString());
		}
	}

	protected void deleteOrphanedSocialRequests() throws Exception {
		try (PreparedStatement ps = connection.prepareStatement(
				"delete from SocialRequest where classNameId = ? and classPK " +
					"not in (select groupId from Group_)")) {

			ps.setLong(
				1,
				PortalUtil.getClassNameId(
					"com.liferay.portal.kernel.model.Group"));

			ps.execute();
		}
	}

	@Override
	protected void doUpgrade() throws Exception {
		if (getSocialActivitySetsCount() > 0) {
			return;
		}

		long increment = increment();

		long delta = getDelta(increment);

		addSocialActivitySets(delta);

		deleteOrphanedSocialRequests();

		updateSocialActivities(delta);

		incrementCounter();
	}

	protected long getDelta(long increment) throws Exception {
		try (Statement s = connection.createStatement()) {
			try (ResultSet rs = s.executeQuery(
					"select min(activityId) from SocialActivity")) {

				if (rs.next()) {
					long minActivityId = rs.getLong(1);

					return increment - minActivityId;
				}

				return 0;
			}
		}
	}

	protected int getSocialActivitySetsCount() throws Exception {
		try (Statement s = connection.createStatement()) {
			String query = "select count(activitySetId) from SocialActivitySet";

			try (ResultSet rs = s.executeQuery(query)) {
				if (rs.next()) {
					return rs.getInt(1);
				}

				return 0;
			}
		}
	}

	protected void incrementCounter() throws Exception {
		try (PreparedStatement ps = connection.prepareStatement(
			"select max(activitySetId) from SocialActivitySet");
			 ResultSet rs = ps.executeQuery();) {

			if (rs.next()) {
				Counter counter = CounterLocalServiceUtil.getCounter(
					Counter.class.getName());

				long lastSocialActivitySetId = rs.getLong(1);

				if (counter.getCurrentId() < lastSocialActivitySetId) {
					counter.setCurrentId(rs.getLong(1));

					CounterLocalServiceUtil.updateCounter(counter);
				}
			}
		}
	}

	protected void updateSocialActivities(long delta) throws Exception {
		try (PreparedStatement ps = connection.prepareStatement(
				"update SocialActivity set activitySetId = (activityId + ?)")) {

			ps.setLong(1, delta);

			ps.execute();
		}
	}

}