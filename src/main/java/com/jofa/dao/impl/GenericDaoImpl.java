package com.jofa.dao.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jofa.dao.GenericDao;

/**
 * If you get an error you might want to start a transaction and commit it after
 * success.
 **/

@SuppressWarnings("hiding")
public abstract class GenericDaoImpl<T, Integer extends Serializable> implements GenericDao<T, Integer>
{

	private static final Logger LOG = LoggerFactory.getLogger(GenericDaoImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	private Class<T> persistentClass;

	@SuppressWarnings("unchecked")
	public GenericDaoImpl()
	{
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	@Override
	public Session getSession()
	{
		return sessionFactory.getCurrentSession();
	}

	@Override
	public Class<T> getPersistentClass()
	{
		return persistentClass;
	}

	@Override
	public T findById(Integer id)
	{
		try
		{
			sessionFactory.getCurrentSession().beginTransaction();
			T entity = (T) getSession().get(getPersistentClass(), id);
			sessionFactory.getCurrentSession().getTransaction().commit();
			return entity;
		} catch (HibernateException e)
		{
			LOG.error(e.toString());
			return null;
		}
	}

	@Override
	public List<T> findAll()
	{
		return findByCriteria();
	}

	@Override
	public T update(T entity)
	{
		try
		{
			getSession().update(entity);
		} catch (HibernateException e)
		{
			LOG.error(e.toString());
		}
		return entity;
	}

	@Override
	public T save(T entity)
	{
		try
		{
			sessionFactory.getCurrentSession().beginTransaction();
			getSession().save(entity);
			sessionFactory.getCurrentSession().getTransaction().commit();
		} catch (HibernateException e)
		{
			LOG.error(e.toString());
		}
		return entity;
	}

	@Override
	public void delete(T entity)
	{
		try
		{
			getSession().delete(entity);
		} catch (HibernateException e)
		{
			LOG.error(e.toString());
		}

	}

	@Override
	public void flush()
	{
		getSession().flush();
	}

	@Override
	public void clear()
	{
		getSession().clear();
	}

	@SuppressWarnings("unchecked")
	public List<T> findByCriteria(Criterion... criterion)
	{
		Criteria crit = getSession().createCriteria(getPersistentClass());
		crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		for (Criterion c : criterion)
		{
			crit.add(c);
		}

		return crit.list();
	}

}
