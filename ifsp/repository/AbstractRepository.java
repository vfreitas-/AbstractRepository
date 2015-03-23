package ifsp.repository;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * 
 * AbstractRepository abstract class, to use when you can't inject the EntityManager
 * - Lack of a trully Java EE Container(@PersistentContext)
 * - You can't use CDI(@ViewScoped)  
 * 
 * If you need a more complex EntityManager scope management, use a trully
 * Java EE Container(jBoss, TomEE, Glassfish) or Dependency Injection.
 * 
 * Usage:
 * public class UserRepository extends AbstractRepository<Long, User> implements Serializable
 * {
 *      //empty
 * }
 * 
 * ...
 * UserRepository rep = new UserRepository();
 * rep.save(user);
 * ...
 * 
 * Don't forget to implement Serializable in every entity and children dao,
 * the hibernate 'll be happy =)
 * 
 * @author Vitor Freitas - github(vFreitas)
 * @param <K> Type of the entity ID(Key).
 * @param <E> Type of the Entity.  
 */
abstract class AbstractRepository<K , E> implements Repositoriable<K ,E>, Serializable
{   
    /* Name of the persistence unit to use*/
    private static final String UNIT_NAME = "site";
    /* Factory to create entity managers */
    private static final EntityManagerFactory factory 
            = Persistence.createEntityManagerFactory(UNIT_NAME);
    
    /* TransactionScoped EntityManager */
    private EntityManager em;
    
    /* The entity class type */
    protected Class<E> entityClass;
    
    /**
     * Builder, it gets the second(entity) parameterized type and
     * sets to entityClass variable.
     */
    public AbstractRepository()
    {
        ParameterizedType genericSuperClass 
                = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<E>) genericSuperClass.getActualTypeArguments()[1];
    }
    
    /**
     * It can be used within children daos in the same package 
     * for especific queries
     * @return An instance of the EntityManager
     */
    protected EntityManager getEntityManager()
    {
        if(em == null) 
            em = factory.createEntityManager();
        else
            if(!em.isOpen())
                em = factory.createEntityManager();
        return em;
    }
    
    /* Finalize the connection within the database */
    protected void closeEntityManager()
    {
        this.em.close();
    }

    /**
     * 
     * @return The entity class type instance variable
     */
    protected Class<E> getEntityClassType()
    {
        return this.entityClass;
    }
    
    /**
     * Persist an entity into the database
     * - Creates a new instance of the EntityTransaction
     * - Initiate it, persist, commit and finally closes the EntityManager
     * - Catches and rollback any errors on the transaction.
     * @param entity E object to persist in the database
     */
    @Override
    public void save(E entity) 
    {
        EntityTransaction trx = getEntityManager().getTransaction();
        try 
        {
            trx.begin();
            getEntityManager().persist(entity);
            trx.commit();
        } 
        catch (Exception e) 
        {
            if(trx != null && trx.isActive())
                trx.rollback();
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
    }

    /**
     * Remove an entity from the database
     * - Creates a new instance of the EntityTransaction
     * - Initiate it, merge, commit and finally closes the EntityManager
     * - Catches and rollback any errors on the transaction.
     * @param entity E object to merge in the database
     */
    @Override
    public void merge(E entity) 
    {
        EntityTransaction trx = getEntityManager().getTransaction();
        try 
        {
            trx.begin();
            getEntityManager().merge(entity);
            trx.commit();
        } 
        catch (Exception e) 
        {
            if(trx != null && trx.isActive())
                trx.rollback();
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
    }

    /**
     * Remove an entity into the database
     * - Creates a new instance of the EntityTransaction
     * - Initiate it, remove, commit and finally closes the EntityManager
     * - Catches and rollback any errors on the transaction.
     * @param entity The E entity to remove from database
     */
    @Override
    public void remove(E entity) 
    {
        EntityTransaction trx = getEntityManager().getTransaction();
        try 
        {
            trx.begin();
            getEntityManager().remove(entity);
            trx.commit();
        } 
        catch (Exception e) 
        {
            if(trx != null && trx.isActive())
                trx.rollback();
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
    }

    /**
     * Gets an entity by its ID
     * - It gets a new instance of the EntityManager
     * - It finds an E entity with the given id
     * @param id ID of the E entity
     * @return an E type object
     */
    @Override
    public E getById(K id) 
    {
        E result = null;
        try 
        {
            result = (E) getEntityManager().find(getEntityClassType(), id);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
        return result;
    }

    /**
     * It gets a list of E objects
     * - It gets a new instance of the EntityManager
     * - Finds all E objects 
     * @return A list of E objects
     */
    @Override
    public List<E> getAll() 
    {
        List<E> resultList = null;
        try 
        {
            resultList = (List<E>) getEntityManager()
                    .createQuery("SELECT e FROM " + getEntityClassType().getSimpleName() + " e")
                    .getResultList(); 
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
        return resultList;
    }

    /**
     * It gets a list of E objects with the results of the given named query.
     * - It gets a new instance of the EntityManager
     * - Create the named query 
     * - And get the result list
     * @param namedQuery Name of the named query
     * @return A list of E objects
     */
    @Override
    public List<E> getAllNamedQuery(String namedQuery) 
    {
        List<E> resultList = null;
        try
        {   
            resultList = (List<E>) getEntityManager().createNamedQuery(namedQuery)
                .getResultList();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
        return resultList;
    }

    /**
     *  It gets an E object with the result of the given named query
     * with the parameter.
     * - It gets a new instance of the EntityManager
     * - Create the named query 
     * - Set the parameter
     * - And get the single result
     * @param namedQuery Name of the named query
     * @param parameter Name of the parameter setted on the named query
     * @param value String value of the parameter
     * @return An E object
     */
    @Override
    public E getUniqueByRestriction(String namedQuery, String parameter, String value) 
    {
        E result = null;
        try
        {   
            result = (E) getEntityManager().createNamedQuery(namedQuery)
                .setParameter(parameter, value)
                .getSingleResult();  
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
        return result;
    }

    /**
     * It gets an E object with the result of the given named query
     * with the parameter.
     * - It creates a new instance of the EntityManager
     * - Create the named query 
     * - Set the parameter
     * - And get the single result
     * @param namedQuery Name of the named query
     * @param parameter Name of the parameter setted on the named query
     * @param value Object value of the parameter
     * @return An E object
     */
    @Override
    public E getUniqueByRestriction(String namedQuery, String parameter, Object value) 
    {
        E result = null;
        try
        {   
            result = (E) getEntityManager().createNamedQuery(namedQuery)
                .setParameter(parameter, value)
                .getSingleResult();  
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
        return result;
    }

    /**
     * It gets a list of E objects with the results of the given named query
     * with the parameter.
     * - It gets a new instance of the EntityManager
     * - Create the named query 
     * - Set the parameter
     * - And get the result list
     * @param namedQuery Name of the named query
     * @param parameter Name of the parameter setted on the named query
     * @param value Object value of the parameter
     * @return A list of E objects
     */
    @Override
    public List<E> getByRestriction(String namedQuery, String parameter, Object value) 
    {
        List<E> resultList = null;
        try
        {   
            resultList = (List<E>) getEntityManager().createNamedQuery(namedQuery)
                .setParameter(parameter, value)
                .getResultList();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
        return resultList;
    }

    /**
     * It gets a list of E objects with the results of the given named query
     * with the parameter.
     * - It gets a new instance of the EntityManager
     * - Create the named query 
     * - Set the parameter
     * - And get the result list
     * @param namedQuery Name of the named query
     * @param parameter Name of the parameter setted on the named query
     * @param value String value of the parameter
     * @return A list of E objects
     */
    @Override
    public List<E> getByRestriction(String namedQuery, String parameter, String value) 
    {
        List<E> resultList = null;
        try
        {   
            resultList = (List<E>) getEntityManager().createNamedQuery(namedQuery)
                .setParameter(parameter, value)
                .getResultList();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeEntityManager();
        }
        return resultList;
    }
    
}
