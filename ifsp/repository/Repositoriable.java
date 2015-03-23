/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ifsp.dao;


import java.io.Serializable;
import java.util.List;

/**
 *
 * @author vFreitas
 * @param <K>
 * @param <E>
 */
public interface Repositoriable<K , E>
{
    void save(E entity);
    
    void merge(E entity);
    
    void remove(E entity);
    
    E getById(K pk);
    
    List<E> getAll();
    
    List<E> getAllNamedQuery(String namedQuery);
    
    E getUniqueByRestriction(String namedQuery, String parameter, String value);
    
    E getUniqueByRestriction(String namedQuery, String parameter, Object value);
    
    List<E> getByRestriction(String namedQuery, String parameter, Object value);
    
    List<E> getByRestriction(String namedQuery, String parameter, String value);
}
