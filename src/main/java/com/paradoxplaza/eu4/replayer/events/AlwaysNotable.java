package com.paradoxplaza.eu4.replayer.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Events marked with this annotation are processed even if not in {@link com.paradoxplaza.eu4.replayer.ReplayerController#notableEvents}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Inherited
public @interface AlwaysNotable {

}
