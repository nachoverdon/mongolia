package com.nachoverdon.mongolia.i18n;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 * This class is used to automatically detect the client's browser language when no language is specified in the URL.
 *
 * To use this class, simply add it to your configuration:
 * <pre>{@code
 *      modules:
 *          site:
 *              config:
 *                  site:
 *                      i18n:
 *                          class: com.nachoverdon.mongolia.i18n.BrowserDetectedI18nContentSupport
 *                          enabled: true
 * }</pre>
 *
 */
public class BrowserDetectedI18nContentSupport extends DefaultI18nContentSupport {

    @Override
    protected String toI18NURI(String uri, Locale locale) {
        return uri.startsWith("/") ? "/" + locale.toString() + uri : uri;
    }

    /**
     * Determines the Locale using the URL (like http://example.com/en/) or with the Accept-Language HTTP request
     * header.
     *
     * @return The determined Locale
     */
    @Override
    protected Locale onDetermineLocale() {
        String i18nURI = MgnlContext.getAggregationState().getCurrentURI();
        String localeStr = StringUtils.substringBefore(StringUtils.substringAfter(i18nURI, "/"), "/");
        Locale locale;

        if (localeStr.length() == 2) {
            locale = determineLocalFromString(localeStr);

            if (getLocales().contains(locale))
                return locale;
        }

        locale = getLocaleFromHttpRequest();

        return getLocales().contains(locale) ? locale : getDefaultLocale();
    }

    /**
     * Gets the languages accepted by the browser from the HTTP request and determines the best locale by the weight
     * assigned to the language
     *
     * @return The determined Locale
     */
    protected Locale getLocaleFromHttpRequest() {
        HttpServletRequest request = Components.getComponent(HttpServletRequest.class);
        String acceptLanguage = request.getHeader("Accept-Language");
        List<Locale.LanguageRange> ranges = (acceptLanguage == null)
                ? Locale.LanguageRange.parse("en-US,en;q=0.5")
                : Locale.LanguageRange.parse(acceptLanguage);
        String language = getDefaultLocale().getLanguage();
        double lastWeight = 0.0;

        for (Locale.LanguageRange range: ranges) {
            String lang = range.getRange();
            double weight = range.getWeight();

            // Use only language, not the country
            if (lang.length() > 2)
                lang = lang.substring(0, 2);


            // If the key doesn't exist or if the weight is heavier, assign the weight to the lang
            if (weight > lastWeight) {
                lastWeight = weight;
                language = lang;
            }
        }

        return determineLocalFromString(language);
    }
}