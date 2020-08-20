package com.nachoverdon.mongolia.validators.page.template;

import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.framework.databinding.validator.ValidatorType;

/**
 * Check PageTemplateValidator for further information.
 *
 * @see com.nachoverdon.mongolia.validators.page.template.PageTemplateValidator
 *
 */
@ValidatorType("pageTemplate")
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

