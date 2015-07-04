package org.prosolo.core.persistance;

import java.io.Serializable;
import java.util.List;

/**
 * Generic Data Access Object. Contains various get methods for querying the
 * storage mechanism for specific objects. 
 */
public interface PersistenceManager<Type> {
    
	Type currentManager();
	Type openSession();
	
	/**
     * Gets all objects which type is <code>searchForType</code>. If there are 
     * not any object of that type, returns empty list. Supertypes may be used.
     * If <code>Object.class</code> was provided as a search parameter, the 
     * resulting list will contain all managed objects.
     * 
     * @param searchForType type of object that will be searched for
     * @return a list of all objects that are of certain type. The list is 
     * empty if there are not any object of that type 
     */
    <T> List<T> get(Class<T> searchForType);
    
    <T> List<T> get(Class<T> searchForType, int firstResult, int maxResults);
    
    /**
     * Gets the concrete object that has the provided id and a concrete type. 
     * If such object does not exist, returns null. 
     * 
     * @param searchForType type of object that will be searched for
     * @param id id of object
     * @return concrete object of a given type and id. <code>null</code> if such
     * object does not exist.
     */
    <T> T get(Serializable id, Class<T> searchForType);
    
    /**
     * Gets all objects which type is <code>searchForType</code> and match the
     * example object. If there are not any object that match, returns empty 
     * list.
     * 
     * @param searchForType type of object that will be searched for
     * @param example example object that searched object will be matched to
     * @return a list of all objects that are of certain type and match the
     * example object. The list is empty if there are not any object that match 
     */
    <T> List<T> get(Class<T> searchForType, T example);
    
    <T> List<T> get(Class<T> searchForType, T example, int firstResult, int maxResults);
    
    /**
     * Gets all objects that match the example object. If there are not any 
     * object that match, returns empty list.
     * 
     * @param example example object that searched object will be matched to
     * @return a list of all objects that match the
     * example object. The list is empty if there are not any object that match 
     */
    <T> List<T> get(T example);

    <T> List<T> get(T example, int firstResult, int maxResults);
    
    /**
     * Persists the object.
     * @param object the object that is going to be persisted
     */
    void save(Object object);
    
    <T> T merge(T object);
    
    <T> T refresh(T object);

    /**
     * Removes the object from the persistance mechanism.
     * @param object the object that is going to be deleted
     */
    void delete(Object object);

//    /**
//     * Removes the object with the given id from the persistance mechanism.
//     * @param id the id of the object that is going to be deleted 
//     */
//    void delete(Serializable id);

    /**
     * Deletes all objects of certain type from the persistent storage
     * mechanism. To delete all possible objects,
     * pass <code>java.lang.Object</code> as the parameter.
     * @param clazz the type of objects that are going to be deleted.
     */
	void deleteAll(Class<?> clazz);
    
    /**
     * Performs writing to the storage mechanism explicitly. Transparent storage
     * mechanism should decide when to do the writing. due to the performance 
     * reasons, usually at the end of the successful transaction.
     * Thus, this method should not be called explicitly unless we want to 
     * utilize the constraint checking provided by the storage mechanism at
     * some point inside a transaction.
     */
    void flush();

    void clear();
    
    public <T> T write(T input);
	
	public void update(Object o);
	
	public List<?> runQuery(String queryString);
	void fullCacheClear();
	void delete(Object object, boolean inNewSession);
	void save(Object object, boolean inNewSession);

}
