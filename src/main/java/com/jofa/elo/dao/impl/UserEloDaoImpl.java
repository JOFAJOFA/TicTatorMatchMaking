package com.jofa.elo.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Property;
import org.hibernate.service.ServiceRegistry;
import org.springframework.stereotype.Repository;

import com.jofa.elo.dao.UserEloDao;
import com.jofa.elo.exception.EloNotSavedException;
import com.jofa.elo.model.UserElo;


@Repository("eloDao")
public class UserEloDaoImpl implements UserEloDao<UserElo, String> {

	private Session currentSession;
    private Transaction currentTransaction;
    
    public UserEloDaoImpl() {
    }

    public Session openCurrentSession() {
		currentSession = getSessionFactory().openSession();
		return currentSession;
	}

	public Session openCurrentSessionwithTransaction() {
		currentSession = getSessionFactory().openSession();
		currentTransaction = currentSession.beginTransaction();
		return currentSession;
	}
	
	public void closeCurrentSession() {
		currentSession.close();
	}
	
	public void closeCurrentSessionwithTransaction() {
		currentTransaction.commit();
		currentSession.close();
	}
	
	private static SessionFactory getSessionFactory() {
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().
		    configure().build();
		SessionFactory sessionFactory = new Configuration().buildSessionFactory(serviceRegistry);
		return sessionFactory;
	}

	public Session getCurrentSession() {
		return currentSession;
	}

	public void setCurrentSession(Session currentSession) {
		this.currentSession = currentSession;
	}

	public Transaction getCurrentTransaction() {
		return currentTransaction;
	}

	public void setCurrentTransaction(Transaction currentTransaction) {
		this.currentTransaction = currentTransaction;
	}
	
	@Override
	public void persist(UserElo entity) throws EloNotSavedException {
		currentSession.persist(entity);	
	}
	
	@Override
	public void saveOrUpdate(UserElo entity){
		currentSession.saveOrUpdate(entity);
	}


	@Override
	public void update(UserElo entity) {
		currentSession.update(entity);
		
	}
	
	@Override
	public UserElo findById(Integer id) {
		return (UserElo)currentSession.get(UserElo.class, id);
	}
	
	@Override
	public UserElo findByUserName(String username) {
		@SuppressWarnings("unchecked")
		List<UserElo> elo = currentSession.createCriteria(UserElo.class)
			    .add(Property.forName("username").eq(username))
			    .list();
		return elo.isEmpty() ? null : elo.get(0);
	}


	@Override
	public void delete(UserElo entity) {
		currentSession.delete(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserElo> findAll() {
		return currentSession.createCriteria(UserElo.class).list();
	}

	@Override
	public void deleteAll() {
		// TODO IMPLEMENT
		
	}

	@Override
	public void save(UserElo entity) {
		currentSession.save(entity);		
	}
	
}
