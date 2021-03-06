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
import java.util.Date;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Singleton
@Slf4j
public class MongoliaTemplatingFunctions {

  public static final String NAME = "mongofn";

  private final I18nContentSupport i18nContentSupport;
  private final TemplatingFunctions cmsfn;
  private final DamTemplatingFunctions damfn;

  /**
   * A set of utilities to be used from Freemarker.
   *
   * @param i18nContentSupport Allows translations and helps knowing which language to use.
   * @param cmsfn Magnolia most basic templating functions to handle nodes, pages and more...
   * @param damfn Templating functions to deal with assets.
   */
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
   * Returns today's date formatted with the given pattern.
   * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html">SimpleDataFormat</a>
   *
   * @param pattern Date pattern
   * @return Today's date formated.
   */
  public String getTodayDate(String pattern) {
    return formatDate(new Date(), pattern);
  }

  /**
   * Returns today's date formatted as dd/MMM/yyyy.
   * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html">SimpleDataFormat</a>
   *
   * @return Today's date formated.
   */
  public String getTodayDate() {
    return formatDate(new Date());
  }

  /**
   * Returns the String version of the date formatted with the given pattern.
   * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html">SimpleDataFormat</a>
   *
   * @param date A Date object
   * @param pattern https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
   * @return The date formatted.
   */
  public String formatDate(Date date, String pattern) {
    return DateUtil.format(date, pattern);
  }

  /**
   * See {@link #formatDate(Date, String)}.
   *
   * @param date A Date object
   * @return The date formatted.
   */
  public String formatDate(Date date) {
    return formatDate(date, "dd/MMM/yyyy");
  }

  /**
   * Gets the current locale.
   *
   * @return The current Locale
   */
  public Locale getLocale() {
    return i18nContentSupport.getLocale();
  }

  /**
   * Gets the current lenguage.
   *
   * @return The language from the current Locale
   */
  public String getLanguage() {
    return getLocale().getLanguage();
  }

  /**
   * Gets the URL of the current page.
   *
   * @param withQueryString Optionally gets the query string.
   * @return The current URL
   */
  public String currentUrl(boolean withQueryString) {
    return withQueryString
        ? MgnlContext.getAggregationState().getOriginalBrowserURL()
        : MgnlContext.getContextPath() + MgnlContext.getAggregationState().getOriginalBrowserURI();
  }

  /**
   * See {@link #currentUrl(boolean)}.
   *
   * @return The current URL
   */
  public String currentUrl() {
    return currentUrl(true);
  }


  /**
   * Prepares the Node for the current language to access its translated properties without the need
   * to add "_lang".
   * Checks if the Node has already been wrapped before doing so.
   *
   * @param node A Node with translatable fields
   * @return The translated Node
   */
  public Node wrapForI18n(Node node) {
    return NodeUtil.isWrappedWith(node, I18nNodeWrapper.class) ? node : cmsfn.wrapForI18n(node);
  }

  /**
   * Returns a base64 String of the image from the DAM. To be used on img tags like:
   * <pre>{@code
   *      <img src="${mongofn.damImageToBase64(content.image)!}"/>
   * }</pre>
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
   * Returns a base64 String of the image from the Resources folder and the given MIME type. Usage
   * on img tags like:
   * <pre>{@code
   *      <img src="${mongofn.damImageToBase64(ctx.contextPath + "/.resources/yourmodule/image.png",
   *      "image/png")!}"/>
   * }</pre>
   *
   * @param path Path to the resource image
   * @param mimeType MIME Type of the image https://www.sitepoint.com/mime-types-complete-list/
   * @return The image encoded to base64 or an empty string
   */
  public String resourcesImageToBase64(String path, String mimeType) {
    try {
      ResourceOrigin<?> resourceOrigin = Components.getComponent(ResourceOrigin.class);

      return ImageUtils.imageToBase64(resourceOrigin.getByPath(path).openStream(), mimeType);
    } catch (Exception e) {
      log.error("Unable to encode Resources image to Base64 for path [" + path + "] and MIME type ["
          + mimeType + "]", e);
    }

    return StringUtils.EMPTY;
  }

}
