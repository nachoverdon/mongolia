package com.nachoverdon.mongolia.validators.page.template;

import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * @see com.nachoverdon.mongolia.validators.page.template.PageTemplateValidator
 */
public class PageTemplateValidatorDefinition extends ConfiguredFieldValidatorDefinition {
    private String templateName;

    public PageTemplateValidatorDefinition() {
        this.setFactoryClass(PageTemplateValidatorFactory.class);
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}

