package com.tecacet.acl.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes marked with this annotation, participate in Acl Security.
 * Each class must provide a name by which instances of this class will be identified.
 * The name must be unique per class. Each instance of the class will be identified with a combination
 * of it's primary key and this identifier.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AclSecured {
    String name();
}
