package com.nachoverdon.mongolia.validators.page.template;

import com.vaadin.v7.data.Validator;
import info.magnolia.ui.form.validator.factory.AbstractFieldValidatorFactory;

/**
 * @see com.nachoverdon.mongolia.validators.page.template.PageTemplateValidator
 */
public class PageTemplateValidatorFactory extends AbstractFieldValidatorFactory<PageTemplateValidatorDefinition> {
    public PageTemplateValidatorFactory(PageTemplateValidatorDefinition definition) {
        super(definition);
    }

    public Validator createValidator() {
        return new PageTemplateValidator(((PageTemplateValidatorDefinition)this.definition).getTemplateName(), this.getI18nErrorMessage());
    }
}
