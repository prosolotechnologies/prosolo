/**
 * 
 */
package org.prosolo.core.spring.deadlock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author "Nikola Milikic"
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DeadlockRetry {
	
	int maxTries() default 10;
    
    int tryIntervalMillis() default 1000;
}
