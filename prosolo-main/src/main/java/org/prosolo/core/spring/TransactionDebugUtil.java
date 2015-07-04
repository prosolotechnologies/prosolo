/**
 * 
 */
package org.prosolo.core.spring;

import java.lang.reflect.InvocationTargetException;

/**
 * @author "Nikola Milikic"
 *
 */
public class TransactionDebugUtil {

	private static final boolean transactionDebugging = true;
	private static final boolean verboseTransactionDebugging = true;

	public static void showTransactionStatus(String message) {
	}

	// Some guidance from: http://java.dzone.com/articles/monitoring-declarative-transac?page=0,1
	public static boolean transactionActive() {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			Class<?> tsmClass = contextClassLoader.loadClass("org.springframework.transaction.support.TransactionSynchronizationManager");
			Boolean isActive = (Boolean) tsmClass.getMethod("isActualTransactionActive", null).invoke(null, null);

			return isActive;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		// If we got here it means there was an exception
		throw new IllegalStateException("ServerUtils.transactionActive was unable to complete properly");
	}
	
	public static String getTransactionName() {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			Class<?> tsmClass = contextClassLoader.loadClass("org.springframework.transaction.support.TransactionSynchronizationManager");
			String transactionName = (String) tsmClass.getMethod("getCurrentTransactionName", null).invoke(null, null);
			
			return transactionName;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		// If we got here it means there was an exception
		throw new IllegalStateException("ServerUtils.getTransactionName was unable to complete properly");
	}

	public static void transactionRequired(String message) {
		// Are we debugging transactions?
		if (!transactionDebugging) {
			// No, just return
			return;
		}

		// Are we doing verbose transaction debugging?
		if (verboseTransactionDebugging) {
			// Yes, show the status before we get to the possibility of throwing an exception
			showTransactionStatus(message);
		}

		// Is there a transaction active?
		if (!transactionActive()) {
			// No, throw an exception
			throw new IllegalStateException("Transaction required but not active [" + message + "]");
		}
	}
}
