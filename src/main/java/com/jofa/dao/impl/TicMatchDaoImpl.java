package com.jofa.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.jofa.dao.TicMatchDao;
import com.jofa.model.TicMatch;


@Repository("ticMatchDao")
public class TicMatchDaoImpl extends GenericDaoImpl<TicMatch, Integer> implements TicMatchDao {

	
	
	private static final Logger LOG = LoggerFactory.getLogger(TicMatchDaoImpl.class);

	private SessionFactory sessionFactory;

	public TicMatchDaoImpl(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public TicMatch findById(Integer id) {
		
		sessionFactory.getCurrentSession().beginTransaction();
		@SuppressWarnings("unchecked")
		List<TicMatch> users = sessionFactory.getCurrentSession().createCriteria(TicMatch.class)
				.add(Property.forName("id").eq(id)).list();
		sessionFactory.getCurrentSession().getTransaction().commit();
		return users.isEmpty() ? null : users.get(0);
	}

	
	
	
	
}
