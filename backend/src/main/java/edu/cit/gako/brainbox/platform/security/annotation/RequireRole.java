package edu.cit.gako.brainbox.platform.security.annotation;

import edu.cit.gako.brainbox.modules.user.entity.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    UserRole value();
}
