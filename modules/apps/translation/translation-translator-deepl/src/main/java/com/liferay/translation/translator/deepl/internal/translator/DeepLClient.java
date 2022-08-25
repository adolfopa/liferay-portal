package com.liferay.translation.translator.deepl.internal.translator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.url.URLBuilder;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.translation.translator.deepl.internal.constants.DeepLConstants;
import com.liferay.translation.translator.deepl.internal.model.SupportedLanguage;
import com.liferay.translation.translator.deepl.internal.model.TranslateResponse;
import com.liferay.translation.translator.deepl.internal.util.JSONUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * @author Yasuyuki Takeo
 */
@Component(
    immediate = true,
    service = DeepLClient.class
)
public class DeepLClient {

  /**
   * Translate text
   */
  public TranslateResponse execute(String authKey, String text, String sourcelang, String targetLang, String url)
      throws IOException {
    String rawRes = _fetch(authKey, text, sourcelang, targetLang, url);

    return JSONUtil.toObject(rawRes, TranslateResponse.class);
  }

  protected String _fetch(
      String authKey, String text, String sourcelang, String targetLang, String url) throws IOException {

    // The API document is here
    // https://www.deepl.com/ja/docs-api/translating-text/example/

    // Build request
    Http.Options options = new Http.Options();

    options.setLocation(
        URLBuilder.create(
            url
        ).addParameter(
            DeepLConstants.AUTH_KEY, authKey
        ).build());

    options.addHeader(
        HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED);
    options.addPart(DeepLConstants.AUTH_KEY, authKey);
    options.addPart(DeepLConstants.TEXT, text);
    options.addPart(DeepLConstants.SOURCE_LANG, sourcelang);
    options.addPart(DeepLConstants.TARGET_LANG, targetLang);
    options.setMethod(Http.Method.POST);

    // Fetch data
    String ret = _http.URLtoString(options);
    Http.Response response = options.getResponse();

    Response.Status status = Response.Status.fromStatusCode(response.getResponseCode());

    if (status == Response.Status.OK) {
      return ret;
    } else if (status == Response.Status.TOO_MANY_REQUESTS) {
      _log.info("TOO_MANY_REQUESTS. Retry after a while");
      return "";
    }

    return ret;
  }

  /**
   * Get supported languages
   */
  public List<SupportedLanguage> verifySupportedLanguage(
      String authKey, String target, String url) throws IOException {
    String rawRes = _verifySupportedLanguage(authKey, target, url);
    return JSONUtil.toObject(rawRes, (new TypeReference<List<SupportedLanguage>>() {
    }));
  }

  protected String _verifySupportedLanguage(
      String authKey, String target, String url) throws IOException {

    // The API document is here
    // https://www.deepl.com/docs-api/other-functions/listing-supported-languages

    // Build request
    Http.Options options = new Http.Options();

    options.setLocation(
        URLBuilder.create(
            url
        ).addParameter(
            DeepLConstants.AUTH_KEY, authKey
        ).build());

    options.addHeader(
        HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED);
    options.addPart(DeepLConstants.AUTH_KEY, authKey);
    options.addPart(DeepLConstants.TARGET, target);
    options.setMethod(Http.Method.POST);

    // Fetch data
    String ret = _http.URLtoString(options);
    Http.Response response = options.getResponse();

    Response.Status status = Response.Status.fromStatusCode(response.getResponseCode());

    if (status == Response.Status.OK) {
      return ret;
    } else if (status == Response.Status.TOO_MANY_REQUESTS) {
      _log.info("TOO_MANY_REQUESTS. Retry after a while");
      return "";
    }

    return ret;
  }

  @Reference
  private Http _http;

  private static final Log _log = LogFactoryUtil.getLog(
      DeepLClient.class);
}
