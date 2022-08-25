package com.liferay.translation.translator.deepl.internal.configuration.persistence.listener;

import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListenerException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.translation.translator.deepl.internal.configuration.DeepLTranslatorConfiguration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Dictionary;

/**
 * @author Yasuyuki Takeo
 */
@Component(
    immediate = true,
    property = "model.class.name=com.liferay.translation.translator.deepl.internal.configuration.DeepLTranslatorConfiguration",
    service = ConfigurationModelListener.class
)
public class DeepLTranslatorConfigurationModelListener implements ConfigurationModelListener {
  @Override
  public void onBeforeSave(String pid, Dictionary<String, Object> properties)
      throws ConfigurationModelListenerException {

    boolean enabled = GetterUtil.getBoolean(properties.get("enabled"));
    String authKey = GetterUtil.getString(properties.get("authKey"));
    String url = GetterUtil.getString(properties.get("url"));

    if (enabled) {
      if (authKey.isEmpty() || url.isEmpty())
        throw new ConfigurationModelListenerException(
            _language.get(
                LocaleThreadLocal.getThemeDisplayLocale(),
                "the-auth-key-and-url-must-be-configured"),
            DeepLTranslatorConfiguration.class, getClass(),
            properties);
    }
  }

  @Reference
  private Language _language;
}
