package com.jofa.match.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.springframework.stereotype.Repository;

import com.jofa.match.dao.TicMatchDao;
import com.jofa.match.exception.MatchNotSavedException;
import com.jofa.match.model.TicMatch;


@Repository("matchDao")
public class TicMatchDaoImpl implements TicMatchDao<TicMatch, String> {

	private Session currentSession;
    private Transaction currentTransaction;
    
    public TicMatchDaoImpl() {
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
	public void persist(TicMatch entity) throws MatchNotSavedException {
		currentSession.persist(entity);
	}
	
	@Override
	public void saveOrUpdate(TicMatch entity){
		currentSession.saveOrUpdate(entity);
	}


	@Override
	public void update(TicMatch entity) {
		currentSession.update(entity);
		
	}

	@Override
	public TicMatch findById(Integer id) {
		return (TicMatch)currentSession.get(TicMatch.class, id);
	}

	@Override
	public void delete(TicMatch entity) {
		currentSession.delete(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TicMatch> findAll() {
		return currentSession.createCriteria(TicMatch.class).list();
	}

	@Override
	public void deleteAll() {
		// TODO IMPLEMENT
		
	}

	@Override
	public void save(TicMatch entity) {
		currentSession.save(entity);		
	}


}
