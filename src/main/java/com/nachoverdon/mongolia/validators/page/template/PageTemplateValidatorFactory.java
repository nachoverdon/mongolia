package com.nachoverdon.mongolia.validators.page.template;

import com.vaadin.v7.data.Validator;
import info.magnolia.ui.form.validator.factory.AbstractFieldValidatorFactory;

/**
 * Check PageTemplateValidator for further information.
 *
 * @see com.nachoverdon.mongolia.validators.page.template.PageTemplateValidator
 */
public class PageTemplateValidatorFactory
    extends AbstractFieldValidatorFactory<PageTemplateValidatorDefinition> {
  public PageTemplateValidatorFactory(PageTemplateValidatorDefinition definition) {
    super(definition);
  }

  @SuppressWarnings("deprecation")
  public Validator createValidator() {
    return new PageTemplateValidator(this.definition.getTemplateName(), this.getI18nErrorMessage());
  }
}
