package com.nachoverdon.mongolia.saleslayer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JcrNode
public abstract class AbstractSalesLayerCategory extends AbstractSalesLayerBean {
  @JcrProperty(name = "ID_PARENT")
  private String idParent;
}
