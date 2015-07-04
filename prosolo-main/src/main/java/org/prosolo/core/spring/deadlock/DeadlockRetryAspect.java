/**
 * 
 */
package org.prosolo.core.spring.deadlock;

import java.lang.reflect.Method;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Component;

/**
 * This Aspect will cause methods to retry if there is a notion of a deadlock.
 *
 * <emf>Note that the aspect implements the Ordered interface so we can set the
 * precedence of the aspect higher than the transaction advice (we want a fresh
 * transaction each time we retry).</emf>
 *
 * @author Jelle Victoor
 * @version 04-jul-2011 handles deadlocks
 */
@Aspect
@Component
public class DeadlockRetryAspect implements Ordered {
    
	private static Logger logger = Logger.getLogger(DeadlockRetryAspect.class);

	public static final String DEADLOCK_MSG = "Encountered a deadlock situation. Please re-run your command.";
     
    @Around(value = "@annotation(deadlockRetry)", argNames = "deadlockRetry")
    public Object invoke(final ProceedingJoinPoint pjp, final DeadlockRetry deadlockRetry) throws Throwable {
        final Integer maxTries = deadlockRetry.maxTries();
        long tryIntervalMillis = deadlockRetry.tryIntervalMillis();
         
        Object target = pjp.getTarget();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
         
        int count = 0;
         
        do {
            try {
                count++;
                Object result = pjp.proceed();
                return result;
            } catch (Throwable e) {
            	logger.error("Catched exception "+ e.getClass() +" invoked on method '" + method.getName() + 
            			"' on class'" + target.getClass() + "' count " + count, e);
              
            	if (!isDeadLock(e)) {
                    throw new RuntimeException(e);
                }
                 
                if (tryIntervalMillis > 0) {
                    try {
                        Thread.sleep(tryIntervalMillis);
                    } catch (InterruptedException ie) {
                        logger.warn("Deadlock retry thread interrupted", ie);
                    }
                }
            }
        }
        while (count <= maxTries);
         
        //gets here only when all attempts have failed
        throw new RuntimeException("DeadlockRetryMethodInterceptor failed to successfully execute target "
                + " due to deadlock in all retry attempts",
                new DeadlockDataAccessException("Created by DeadlockRetryMethodInterceptor"));
    }
    
    private static boolean isDeadLock(Throwable throwable) {
        boolean isDeadLock = false;
         
        Throwable[] causes = ExceptionUtils.getThrowables(throwable);
        
        for (Throwable cause : causes) {
            if (cause instanceof CannotAcquireLockException || 
            		(cause.getMessage() != null && (cause.getMessage().contains("LockAcquisitionException") || 
            				cause.getMessage().contains(DEADLOCK_MSG)))) {
                isDeadLock = true;
                return isDeadLock;
            }
        }
         
        return isDeadLock;
    }
     
    @Override
    public int getOrder() {
        return -1;
    }
}
