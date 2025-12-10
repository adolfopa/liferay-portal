/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.journal.web.internal.messaging;

import com.liferay.data.engine.rest.resource.v2_0.DataDefinitionResource;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.journal.web.internal.constants.JournalDestinationNames;
import com.liferay.journal.web.internal.util.ImportDataDefinitionHelper;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.DestinationConfiguration;
import com.liferay.portal.kernel.messaging.DestinationFactory;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.MapUtil;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@Component(
	property = "destination.name=" + JournalDestinationNames.IMPORT_AND_OVERRIDE_DATA_DEFINITION,
	service = MessageListener.class
)
public class ImportAndOverrideDataDefinitionMessageListener
	extends BaseMessageListener {

	@Activate
	protected void activate(BundleContext bundleContext) {
		_importDataDefinitionHelper = new ImportDataDefinitionHelper(
			_dataDefinitionResourceFactory, _ddmStructureLocalService,
			_userLocalService);

		DestinationConfiguration destinationConfiguration =
			new DestinationConfiguration(
				DestinationConfiguration.DESTINATION_TYPE_SERIAL,
				JournalDestinationNames.IMPORT_AND_OVERRIDE_DATA_DEFINITION);

		Destination destination = _destinationFactory.createDestination(
			destinationConfiguration);

		_serviceRegistration = bundleContext.registerService(
			Destination.class, destination,
			MapUtil.singletonDictionary(
				"destination.name", destination.getName()));
	}

	@Deactivate
	protected void deactivate() {
		_importDataDefinitionHelper = null;

		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
		}
	}

	@Override
	protected void doReceive(Message message) throws Exception {
		_importDataDefinitionHelper.importAndOverride(
			message.getLong("userId"), message.getLong("dataDefinitionId"),
			message.getString("json"));
	}

	@Reference
	private DataDefinitionResource.Factory _dataDefinitionResourceFactory;

	@Reference
	private DDMStructureLocalService _ddmStructureLocalService;

	@Reference
	private DestinationFactory _destinationFactory;

	private ImportDataDefinitionHelper _importDataDefinitionHelper;
	private ServiceRegistration<Destination> _serviceRegistration;

	@Reference
	private UserLocalService _userLocalService;

}