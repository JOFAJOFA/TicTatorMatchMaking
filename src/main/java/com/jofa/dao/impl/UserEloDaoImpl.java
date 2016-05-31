package com.jofa.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.jofa.dao.UserEloDao;
import com.jofa.model.UserElo;


@Repository("userEloDao")
public class UserEloDaoImpl extends GenericDaoImpl<UserElo, Integer> implements UserEloDao
{

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(UserEloDaoImpl.class);

	private SessionFactory sessionFactory;

	public UserEloDaoImpl(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	@Override
	public UserElo findByUserName(String username)
	{
		sessionFactory.getCurrentSession().beginTransaction();
		@SuppressWarnings("unchecked")
		List<UserElo> users = sessionFactory.getCurrentSession().createCriteria(UserElo.class)
				.add(Property.forName("username").eq(username)).list();
		sessionFactory.getCurrentSession().getTransaction().commit();
		return users.isEmpty() ? null : users.get(0);
	}

	
	@Override
	public UserElo findById(Integer id) {
		
		sessionFactory.getCurrentSession().beginTransaction();
		@SuppressWarnings("unchecked")
		List<UserElo> users = sessionFactory.getCurrentSession().createCriteria(UserElo.class)
				.add(Property.forName("id").eq(id)).list();
		sessionFactory.getCurrentSession().getTransaction().commit();
		return users.isEmpty() ? null : users.get(0);
	}
}
