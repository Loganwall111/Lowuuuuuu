package org.jspecify.annotations;

// Compile-time stub: declaration-only (no TYPE_USE target). javac cannot
// attach the real JSpecify type annotations from raw vanilla class files
// ("Cannot attach type annotations" error), but mod source needs the
// annotation classes to exist. These stubs satisfy the symbol lookups and
// are never packaged into the jar.
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.PACKAGE, ElementType.LOCAL_VARIABLE})
public @interface Nullable {
}
