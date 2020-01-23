package com.nachoverdon.mongolia.validators.page.template;

import com.nachoverdon.mongolia.utils.QueryUtils;
import com.vaadin.v7.data.validator.AbstractStringValidator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.query.Query;

/**
 * A simple validator that is supposed to used on link fields that use the website workspace and the pages app that
 * checks if the selected page has the same template as the one specified in the {@link #templateName} variable.
 *
 * Usage on dialog:
 *
 * <pre>{@code
 *      - name: pageWithSpecificTemplate
 *        fieldType: link
 *        targetWorkspace: website
 *        appName: pages
 *        # It supports BaseIdentifierToPathConverter.
 *        identifierToPathConverter:
 *          class: info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter
 *        required: true
 *        validators:
 *          - name: url
 *            class: com.nachoverdon.mongolia.validators.PageTemplateValidatorDefinition
 *            templateName: your-module:pages/cool_template
 *            errorMessage: Wrong template, plase select a Cool Template page
 *
 * }</pre>
 *
 */
public class PageTemplateValidator extends AbstractStringValidator {
    private String templateName;

    public PageTemplateValidator(String templateName, String errorMessage) {
        super(errorMessage);
        this.templateName = templateName;
    }

    /**
     * Checks if the page is of the same type as the one specified in {@link #templateName}
     *
     * @param value The selected page. Can be a path or a uuid.
     * @return true if the value is valid
     */
    @Override
    protected boolean isValidValue(String value) {
        try {
            // First, check if the value is a path or a uuid
            String condition = "jcr:" + (isPath(value) ? "path" : "uuid") + " = " + QueryUtils.quoteSQL2Value(value);

            // Search a page with the given template and path
            String sql = "SELECT * FROM " + NodeTypes.Page.NAME + " WHERE " + NodeTypes.Renderable.TEMPLATE + " = "
                    + QueryUtils.quoteSQL2Value(templateName) + " AND " + condition;

            // If it exists then its valid
            return QueryUtils.searchSingle(RepositoryConstants.WEBSITE, sql, Query.SQL) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the value is a path by just checking if the first character is a "/"
     *
     * @param value The selected page. Can be a path or a uuid.
     * @return true if the value is a path
     */
    protected boolean isPath(String value) {
        return value.startsWith("/");
    }

}
