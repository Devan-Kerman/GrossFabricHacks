package net.devtech.grossfabrichacks.synthesis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation interface marks a method as an initializer.<br>
 * It is renamed to <b>{@code <init>}</b>, or <b>{@code <clinit>}</b> if it is static.<br>
 * All <b>{@code @Initializer}</b>-annotated methods are aggregated<br>
 * into a single initializer in the order in which they were declared.<br>
 * {@code static} methods become static initializers and non-{@code static} method become instance initializers.
 */
@Target(ElementType.METHOD)
public @interface Initializer {}
