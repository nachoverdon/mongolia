package com.nachoverdon.mongolia.templating;

import com.nachoverdon.mongolia.utils.ImageUtils;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.templating.functions.DamTemplatingFunctions;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import info.magnolia.objectfactory.Components;
import info.magnolia.resourceloader.ResourceOrigin;
import info.magnolia.templating.functions.TemplatingFunctions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Singleton
public class MongoliaTemplatingFunctions {

    public static final String NAME = "mongofn";

    private static final Logger log = LoggerFactory.getLogger(MongoliaTemplatingFunctions.class);

    private I18nContentSupport i18nContentSupport;
    private TemplatingFunctions cmsfn;
    private DamTemplatingFunctions damfn;

    @Inject
    public MongoliaTemplatingFunctions(
            I18nContentSupport i18nContentSupport,
            TemplatingFunctions cmsfn,
            DamTemplatingFunctions damfn
    ) {
        this.i18nContentSupport = i18nContentSupport;
        this.cmsfn = cmsfn;
        this.damfn = damfn;
    }

    /**
     * Returns the date formatted for the product pdf for a given pattern
     *
     * @param pattern https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
     * @return
     */
    public String getTodayDate(String pattern) {
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Returns the String version of the date formatted with the given pattern
     *
     * @param date
     * @param pattern https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
     * @return
     */
    public String formatDate(Date date, String pattern) {
        return DateUtil.format(date, pattern);
    }

    /**
     * See formatDate(Date, String)
     *
     * @param date
     * @return
     */
    public String formatDate(Date date) {
        return formatDate(date, "dd MMM yyyy");
    }

    /**
     * Gets the current locale
     *
     * @return
     */
    public Locale getLocale() {
        return i18nContentSupport.getLocale();
    }

    /**
     * Gets the current lenguage
     *
     * @return
     */
    public String getLanguage() {
        return getLocale().getLanguage();
    }

    /**
     * Gets the URL of the current page.
     *
     * @param withQueryString Optionally gets the query string.
     * @return
     */
    public String currentUrl(boolean withQueryString) {
        String url;

        if (withQueryString) url = MgnlContext.getAggregationState().getOriginalBrowserURL();
        else url = MgnlContext.getContextPath() + MgnlContext.getAggregationState().getOriginalBrowserURI();

        return url;
    }

    /**
     * See currentUrl
     *
     * @return
     */
    public String currentUrl() {
        return currentUrl(true);
    }


    /**
     * Prepares the Node for the current language to access its translated properties without the need to add "_lang".
     * Checks if the Node has already been wrapped before doing so.
     *
     * @param node
     * @return
     */
    public Node wrapForI18n(Node node) {
        return NodeUtil.isWrappedWith(node, I18nNodeWrapper.class) ? node : cmsfn.wrapForI18n(node);
    }

    /**
     * Returns a base64 String of the image from the DAM. To be used on img tags like:
     *      <img src="${mongofn.damImageToBase64(content.image)!}"/>
     *
     * @param id jcr:uuid of the image on the DAM
     * @return The image encoded to base64 or an empty string
     */
    public String damImageToBase64(String id) {
        try {
            Asset asset = damfn.getAsset(id);

            return ImageUtils.imageToBase64(asset.getContentStream(), asset.getMimeType());
        } catch (Exception e) {
            log.error("Unable to encode DAM image to Base 64 for id [" + id + "]", e);
        }

        return StringUtils.EMPTY;
    }

    /**
     * Returns a base64 String of the image from the Resources folder and the given MIME type. Usage on img tags like:
     *      <img src="${mongofn.damImageToBase64(ctx.contextPath + "/.resources/yourmodule/image.png", "image/png")!}"/>
     *
     * @param path Path to the resource image
     * @param mimeType MIME Type of the image https://www.sitepoint.com/mime-types-complete-list/
     * @return The image encoded to base64 or an empty string
     */
    public String resourcesImageToBase64(String path, String mimeType) {
        try {
            ResourceOrigin resourceOrigin = Components.getComponent(ResourceOrigin.class);

            return ImageUtils.imageToBase64(resourceOrigin.getByPath(path).openStream(), mimeType);
        } catch (Exception e) {
            log.error("Unable to encode Resources image to Base 64 for path [" + path + "] and MIME type ["
                    + mimeType + "]", e);
        }

        return StringUtils.EMPTY;
    }

}
