package com.jofa.elo.dao;

import java.io.Serializable;
import java.util.List;

public interface EloDao <T, Id extends Serializable> {

	public void persist(T entity);
	
	public void save(T entity);
	
	public void update(T entity);
	
	public T findByUserName(String id);
	
	public void delete(T entity);
	
	public List<T> findAll();
	
	public void deleteAll();

	public void saveOrUpdate(T entity);
	
}
