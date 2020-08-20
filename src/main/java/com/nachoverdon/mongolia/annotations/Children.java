package com.nachoverdon.mongolia.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Children {
  /**
   * The type of the node.
   *
   * @return the type of the node.
   */
  Class<?> typeOf();
}
