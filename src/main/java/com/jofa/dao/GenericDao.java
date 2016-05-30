package com.jofa.dao;

import java.util.List;

import org.hibernate.Session;

@SuppressWarnings("hiding")
public interface GenericDao<T, Integer>
{

	public Class<T> getPersistentClass();

	public Session getSession();

	public T findById(Integer id);

	public List<T> findAll();

	public T save(T entity);

	public T update(T entity);

	public void delete(T entity);

	public void flush();

	public void clear();

}
