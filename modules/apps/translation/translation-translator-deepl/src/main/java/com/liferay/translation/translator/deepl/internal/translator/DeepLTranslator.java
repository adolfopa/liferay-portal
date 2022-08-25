package com.liferay.translation.translator.deepl.internal.translator;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.translation.translator.Translator;
import com.liferay.translation.translator.TranslatorPacket;
import com.liferay.translation.translator.deepl.internal.configuration.DeepLTranslatorConfiguration;
import com.liferay.translation.translator.deepl.internal.constants.DeepLConstants;
import com.liferay.translation.translator.deepl.internal.model.SupportedLanguage;
import com.liferay.translation.translator.deepl.internal.model.TranslateResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.*;

/**
 * @author Yasuyuki Takeo
 */
@Component(
    configurationPid = "com.liferay.translation.translator.deepl.internal.configuration.DeepLTranslatorConfiguration",
    service = Translator.class
)
public class DeepLTranslator implements Translator {
  @Override
  public boolean isEnabled(long companyId) throws ConfigurationException {
    DeepLTranslatorConfiguration
        deepLTranslatorConfiguration =
        _configurationProvider.getCompanyConfiguration(
            DeepLTranslatorConfiguration.class, companyId);

    return deepLTranslatorConfiguration.enabled() &&
        !Validator.isBlank(deepLTranslatorConfiguration.authKey()) &&
        !Validator.isBlank(deepLTranslatorConfiguration.url());
  }

  private String _getLanguageCode(String languageId) {
    List<String> list = StringUtil.split(languageId, CharPool.UNDERLINE);

    return list.get(0);
  }

  protected Boolean _verifyLanguage(List<String> deepLLanguages, List<String> compareLanguages) {
    for (String lang : compareLanguages) {
      if (Collections.disjoint(deepLLanguages, Arrays.asList(lang))) {
        _log.error("DeepL does not support " + lang + ". Abort processing translation.");
        return false;
      }
    }
    return true;
  }

  protected List<String> _getSupportedLanguages(
      DeepLTranslatorConfiguration deepLTranslatorConfiguration) {
    List<SupportedLanguage> supportedLanguages
        = null;
    try {
      supportedLanguages = _deepLClient.verifySupportedLanguage(
          deepLTranslatorConfiguration.authKey(),
          DeepLConstants.TARGET, DeepLConstants.SUPPORTED_LANGUAGE_INQ_URL
      );
    } catch (IOException e) {
      _log.error("Failed to call supported language list." + System.lineSeparator() + e.getLocalizedMessage());
      return new ArrayList<>();
    }

    List<String> languages = new ArrayList<>();
    supportedLanguages.forEach(sl -> {
      languages.add(sl.language);
    });
    return languages;
  }

  @Override
  public TranslatorPacket translate(TranslatorPacket translatorPacket) throws PortalException {
    if (!isEnabled(translatorPacket.getCompanyId())) {
      return translatorPacket;
    }

    DeepLTranslatorConfiguration
        deepLTranslatorConfiguration =
        _configurationProvider.getCompanyConfiguration(
            DeepLTranslatorConfiguration.class, translatorPacket.getCompanyId());

    Map<String, String> translatedFieldsMap = new HashMap<>();

    String sourceLanguageCode = _getLanguageCode(
        translatorPacket.getSourceLanguageId()).toUpperCase();
    String targetLanguageCode = _getLanguageCode(
        translatorPacket.getTargetLanguageId()).toUpperCase();

    List<String> supportedLanguages = _getSupportedLanguages(deepLTranslatorConfiguration);
    // Verify languages are supported or return.
    if (!_verifyLanguage(
        supportedLanguages, Arrays.asList(targetLanguageCode))
    ) {
      _log.error("No target language available for " + targetLanguageCode + ". Supported languages are: " + String.join(",", supportedLanguages));
      return translatorPacket;
    }

    Map<String, String> fieldsMap = translatorPacket.getFieldsMap();

    fieldsMap.forEach(
        (key, value) -> translatedFieldsMap.put(
            key,
            _translate(
                deepLTranslatorConfiguration.url(),
                deepLTranslatorConfiguration.authKey(),
                value, sourceLanguageCode, targetLanguageCode)));

    return new TranslatorPacket() {
      @Override
      public long getCompanyId() {
        return translatorPacket.getCompanyId();
      }

      @Override
      public Map<String, String> getFieldsMap() {
        return translatedFieldsMap;
      }

      @Override
      public String getSourceLanguageId() {
        return translatorPacket.getSourceLanguageId();
      }

      @Override
      public String getTargetLanguageId() {
        return translatorPacket.getTargetLanguageId();
      }
    };
  }

  protected String _translate(
      String url, String authKey, String text, String sourceLanguageCode, String targetLanguageCode) {

    if (Validator.isBlank(text)) {
      return text;
    }

    TranslateResponse translateResponse = null;
    try {
      translateResponse = _deepLClient.execute(
          authKey,
          text,
          sourceLanguageCode,
          targetLanguageCode,
          url
      );
    } catch (IOException e) {
      _log.error("IOException. Return original text: " + e.getLocalizedMessage());
      return text;
    }

    return translateResponse.translations.get(0).text;
  }

  @Activate
  @Modified
  protected void activate(Map<String, Object> properties) {
    _deepLTranslatorConfiguration = ConfigurableUtil.createConfigurable(
        DeepLTranslatorConfiguration.class, properties);
  }

  private static final Log _log = LogFactoryUtil.getLog(
      DeepLTranslator.class);

  private volatile DeepLTranslatorConfiguration _deepLTranslatorConfiguration;

  @Reference
  private DeepLClient _deepLClient;

  @Reference
  private ConfigurationProvider _configurationProvider;
}
