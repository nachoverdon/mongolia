package com.nachoverdon.mongolia.i18n

import info.magnolia.cms.i18n.DefaultI18nContentSupport
import info.magnolia.context.MgnlContext
import info.magnolia.objectfactory.Components
import org.apache.commons.lang3.StringUtils
import java.util.*
import javax.servlet.http.HttpServletRequest

class BrowserDetectedI18nContentSupport : DefaultI18nContentSupport() {
    override fun toI18NURI(uri: String, locale: Locale): String {
        return if (uri.startsWith("/")) "/$locale$uri" else uri
    }

    override fun onDetermineLocale(): Locale {
        val i18nURI = MgnlContext.getAggregationState().getCurrentURI()
        val localeStr = StringUtils.substringBefore(StringUtils.substringAfter(i18nURI, "/"), "/")
        var locale: Locale

        if (localeStr.length == 2) {
            locale = determineLocalFromString(localeStr)

            if (getLocales().contains(locale)) return locale
        }

        locale = getLocaleFromHttpRequest()

        return if (getLocales().contains(locale)) locale else getDefaultLocale()

    }

    // Gets the languages accepted by the browser from the HTTP request and determines the best locale
    // by the weight assigned to the language
    fun getLocaleFromHttpRequest(): Locale {
        val request = Components.getComponent(HttpServletRequest::class.java)
        val acceptLanguage = request.getHeader("Accept-Language")
        val ranges = Locale.LanguageRange.parse(acceptLanguage)
        var language = getDefaultLocale().getLanguage()
        var lastWeight = 0.0

        for (range in ranges) {
            var lang = range.range
            val weight = range.weight

            // Use only language, not the country
            if (lang.length > 2) {
                lang = lang.substring(0, 2)
            }

            // If the key doesn't exist or if the weight is heavier, assign the weight to the lang
            if (weight > lastWeight) {
                lastWeight = weight
                language = lang
            }
        }

        return determineLocalFromString(language)
    }
}