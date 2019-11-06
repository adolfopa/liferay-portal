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

package com.liferay.portal.upgrade.v7_3_x.util;

import java.sql.Types;

import java.util.HashMap;
import java.util.Map;

/**
 * @author	  Brian Wing Shun Chan
 * @generated
 */
public class UserNotificationDeliveryTable {

	public static final String TABLE_NAME = "UserNotificationDelivery";

	public static final Object[][] TABLE_COLUMNS = {
		{"mvccVersion", Types.BIGINT},
		{"userNotificationDeliveryId", Types.BIGINT},
		{"companyId", Types.BIGINT}, {"userId", Types.BIGINT},
		{"portletId", Types.VARCHAR}, {"classNameId", Types.BIGINT},
		{"notificationType", Types.VARCHAR}, {"deliveryType", Types.INTEGER},
		{"deliver", Types.BOOLEAN}
	};

	public static final Map<String, Integer> TABLE_COLUMNS_MAP =
new HashMap<String, Integer>();

static {
TABLE_COLUMNS_MAP.put("mvccVersion", Types.BIGINT);

TABLE_COLUMNS_MAP.put("userNotificationDeliveryId", Types.BIGINT);

TABLE_COLUMNS_MAP.put("companyId", Types.BIGINT);

TABLE_COLUMNS_MAP.put("userId", Types.BIGINT);

TABLE_COLUMNS_MAP.put("portletId", Types.VARCHAR);

TABLE_COLUMNS_MAP.put("classNameId", Types.BIGINT);

TABLE_COLUMNS_MAP.put("notificationType", Types.VARCHAR);

TABLE_COLUMNS_MAP.put("deliveryType", Types.INTEGER);

TABLE_COLUMNS_MAP.put("deliver", Types.BOOLEAN);

}
	public static final String TABLE_SQL_CREATE =
"create table UserNotificationDelivery (mvccVersion LONG default 0 not null,userNotificationDeliveryId LONG not null primary key,companyId LONG,userId LONG,portletId VARCHAR(200) null,classNameId LONG,notificationType VARCHAR(75) null,deliveryType INTEGER,deliver BOOLEAN)";

	public static final String TABLE_SQL_DROP =
"drop table UserNotificationDelivery";

	public static final String[] TABLE_SQL_ADD_INDEXES = {
		"create unique index IX_8B6E3ACE on UserNotificationDelivery (userId, portletId[$COLUMN_LENGTH:200$], classNameId, notificationType[$COLUMN_LENGTH:75$], deliveryType)"
	};

}