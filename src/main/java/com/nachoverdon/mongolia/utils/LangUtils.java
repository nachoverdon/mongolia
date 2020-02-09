package com.nachoverdon.mongolia.utils;

import info.magnolia.context.MgnlContext;

public class LangUtils {
    public static final String DEFAULT_LANG = "en";

    public static String getLanguage() {
        return MgnlContext.getAggregationState().getLocale().getLanguage();
    }
}
