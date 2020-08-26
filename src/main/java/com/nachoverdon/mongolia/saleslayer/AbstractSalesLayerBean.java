package com.nachoverdon.mongolia.saleslayer;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

@Data
@NoArgsConstructor
@JcrNode
public abstract class AbstractSalesLayerBean {
  @JcrPath
  private String nodePath;

  @JcrName
  @JcrProperty(name = "ID")
  private String id;

  @JcrProperty(name = "STATUS")
  private String status;
}
