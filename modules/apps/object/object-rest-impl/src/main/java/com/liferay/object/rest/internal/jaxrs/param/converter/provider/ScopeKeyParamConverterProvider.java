/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.internal.jaxrs.param.converter.provider;

import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.util.GroupUtil;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.apache.cxf.jaxrs.utils.AnnotationUtils;

/**
 * @author Jorge García Jiménez
 */
@Provider
public class ScopeKeyParamConverterProvider
	implements ParamConverter<String>, ParamConverterProvider {

	public ScopeKeyParamConverterProvider(
		DepotEntryLocalService depotEntryLocalService,
		GroupLocalService groupLocalService) {

		_depotEntryLocalService = depotEntryLocalService;
		_groupLocalService = groupLocalService;
	}

	@Override
	public String fromString(String parameter) {
		if (parameter == null) {
			return null;
		}

		Long groupId = _getGroupId(parameter);

		if (groupId != null) {
			return String.valueOf(groupId);
		}

		StringBundler sb = new StringBundler(4);

		sb.append("Unable to get a valid ");

		if (StringUtil.equals(
				_objectDefinition.getScope(),
				ObjectDefinitionConstants.SCOPE_DEPOT)) {

			sb.append("asset library");
		}
		else {
			sb.append("site");
		}

		sb.append(" with scopeKey ");
		sb.append(parameter);

		throw new NotFoundException(sb.toString());
	}

	@Override
	public <T> ParamConverter<T> getConverter(
		Class<T> clazz, Type type, Annotation[] annotations) {

		if (String.class.equals(clazz) && _hasScopeKeyAnnotation(annotations)) {
			return (ParamConverter<T>)this;
		}

		return null;
	}

	@Override
	public String toString(String parameter) {
		return String.valueOf(parameter);
	}

	private Long _getGroupId(String parameter) {
		if (StringUtil.equals(
				_objectDefinition.getScope(),
				ObjectDefinitionConstants.SCOPE_DEPOT)) {

			return GroupUtil.getDepotGroupId(
				parameter, _company.getCompanyId(), _depotEntryLocalService,
				_groupLocalService);
		}
		else if (StringUtil.equals(
					_objectDefinition.getScope(),
					ObjectDefinitionConstants.SCOPE_SITE)) {

			return GroupUtil.getGroupId(
				_company.getCompanyId(), parameter, _groupLocalService);
		}

		throw new InternalServerErrorException("Unexpected scopeKey parameter");
	}

	private boolean _hasScopeKeyAnnotation(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if ((annotation.annotationType() == PathParam.class) &&
				StringUtil.equals(
					AnnotationUtils.getAnnotationValue(annotation),
					"scopeKey")) {

				return true;
			}
		}

		return false;
	}

	@Context
	private Company _company;

	private final DepotEntryLocalService _depotEntryLocalService;
	private final GroupLocalService _groupLocalService;

	@Context
	private ObjectDefinition _objectDefinition;

	@Context
	private UriInfo _uriInfo;

}