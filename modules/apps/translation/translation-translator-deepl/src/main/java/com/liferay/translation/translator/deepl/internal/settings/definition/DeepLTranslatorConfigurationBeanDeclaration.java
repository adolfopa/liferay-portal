package com.liferay.translation.translator.deepl.internal.settings.definition;

import com.liferay.portal.kernel.settings.definition.ConfigurationBeanDeclaration;
import com.liferay.translation.translator.deepl.internal.configuration.DeepLTranslatorConfiguration;
import org.osgi.service.component.annotations.Component;

/**
 * @author Yasuyuki Takeo
 */
@Component(service = ConfigurationBeanDeclaration.class)
public class DeepLTranslatorConfigurationBeanDeclaration implements ConfigurationBeanDeclaration {

    @Override
    public Class<?> getConfigurationBeanClass() {
        return DeepLTranslatorConfiguration.class;
    }

}