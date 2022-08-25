package com.liferay.translation.translator.deepl.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Yasuyuki Takeo
 */
@ExtendedObjectClassDefinition(
        category = "translation",
        scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
        id = "com.liferay.translation.translator.deepl.internal.configuration.DeepLTranslatorConfiguration",
        localization = "content/Language",
        name = "deepl-translator-configuration-name"
)
public interface DeepLTranslatorConfiguration {
    @Meta.AD(
            deflt = "false",
            description = "enabled-description[deepl-translation]",
            name = "enabled", required = false
    )
    public boolean enabled();

    @Meta.AD(
            deflt = "", description = "auth-key-description",
            name = "auth-key", required = false
    )
    public String authKey();

    @Meta.AD(
            deflt = "https://api-free.deepl.com/v2/translate", description = "url-description",
            name = "url", required = false
    )
    public String url();
}