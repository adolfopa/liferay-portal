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

package com.liferay.info.item.field.reader;

import com.liferay.info.field.InfoFieldSet;
import com.liferay.info.field.InfoFieldValue;

import java.util.List;

/**
 * @author Jürgen Kappler
 * @author Jorge Ferrer
 */
public interface InfoItemFieldReaderFieldSetProvider {

	public <T> InfoFieldSet getInfoFieldSet(Class<T> clazz);

	public <T> List<InfoFieldValue<Object>> getInfoFieldValues(
		Class<T> clazz, T itemObject);

}