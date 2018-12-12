package org.prosolo.services.authentication.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used for annotating the session attributes and defining their logical scope/life.
 * It allows specifying when the life of the session attribute ends by specifying the authentication
 * change type which would mean the end of life for the given session attribute.
 *
 * This annotation can than be used by processors to determine when to remove some attribute
 * from the session.
 *
 * @author stefanvuckovic
 * @date 2018-11-02
 * @since 1.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SessionAttributeScope {

    AuthenticationChangeType end();
}
