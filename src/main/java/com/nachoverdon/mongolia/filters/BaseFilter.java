package com.nachoverdon.mongolia.filters;

import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the BaseFilter class. To use it, extend from it and override its methods. Then add the
 * path to your extended filter on your Magnolia configuration on:
 * <pre>{@code
 *  config:
 *    server:
 *      filters:
 *        cms:
 *          yourFilter:
 *            class: your.package.YourFilter
 *            enabled: true
 * }</pre>
 * <p>
 * There are 2 methods that you have to override.
 * {@link #checkCondition}
 *      Checks whether it should proceed with the action.
 *
 * {@link #action}
 *      Will be triggered when the condition is met.
 * </p>
 *
 */
public abstract class BaseFilter extends OncePerRequestAbstractMgnlFilter {
  protected FilterParameters filterParameters = null;

  @Override
  public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    filterParameters = new FilterParameters(request, response, chain);

    if (checkCondition()) {
      action();
    }

    filterParameters = null;
    chain.doFilter(request, response);
  }

  /**
   * Checks if the request should be processed.
   *
   * @return true if it should be processed.
   */
  protected abstract boolean checkCondition();

  /**
   * Action to be performed when the condition is met.
   *
   */
  protected abstract void action();

}
