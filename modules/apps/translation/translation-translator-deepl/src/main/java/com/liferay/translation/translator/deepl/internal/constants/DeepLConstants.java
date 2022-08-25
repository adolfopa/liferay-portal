package com.liferay.translation.translator.deepl.internal.constants;

/**
 * @author Yasuyuki Takeo
 */
public class DeepLConstants {
    public static final int CONNECTION_TIMEOUT = 15;
    public static final int READ_TIMEOUT = 15;
    public static final int CALL_TIMEOUT = 30;
    public static final int MAX_IDLE_CONNECTIONS = 200;
    public static final int KEEP_ALIVE_DURATION = 120;

    public static final String AUTH_KEY = "auth_key";
    public static final String TEXT = "text";

    public static final String SOURCE_LANG = "source_lang";
    public static final String TARGET_LANG = "target_lang";

    /**
     * Verifying the supported languages
     */
    public static final String TARGET = "target";

    public static final String SUPPORTED_LANGUAGE_INQ_URL = "https://api-free.deepl.com/v2/languages";

}
