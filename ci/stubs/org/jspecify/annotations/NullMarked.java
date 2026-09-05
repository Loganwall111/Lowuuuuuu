package org.jspecify.annotations;

// Compile-time stub (see Nullable.java).
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.MODULE, ElementType.PACKAGE, ElementType.TYPE})
public @interface NullMarked {
}
