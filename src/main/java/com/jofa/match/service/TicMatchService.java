package com.jofa.match.service;

import java.util.List;

import com.jofa.match.dao.impl.TicMatchDaoImpl;
import com.jofa.match.model.TicMatch;

public class TicMatchService {

	private static TicMatchDaoImpl matchDaoImpl;

	public TicMatchService() {
		matchDaoImpl = new TicMatchDaoImpl();
	}

	public void persist(TicMatch entity) {
		matchDaoImpl.openCurrentSessionwithTransaction();
		matchDaoImpl.persist(entity);
		matchDaoImpl.closeCurrentSessionwithTransaction();
	}

	public void save(TicMatch entity) {
		matchDaoImpl.openCurrentSessionwithTransaction();
		matchDaoImpl.save(entity);
		matchDaoImpl.closeCurrentSessionwithTransaction();
	}

	public void update(TicMatch entity) {
		matchDaoImpl.openCurrentSessionwithTransaction();
		matchDaoImpl.update(entity);
		matchDaoImpl.closeCurrentSessionwithTransaction();
	}

	public TicMatch findById(Integer id) {
		matchDaoImpl.openCurrentSession();
		TicMatch book = matchDaoImpl.findById(id);
		matchDaoImpl.closeCurrentSession();
		return book;
	}

	public void delete(int id) {
		matchDaoImpl.openCurrentSessionwithTransaction();
		TicMatch book = matchDaoImpl.findById(id);
		matchDaoImpl.delete(book);
		matchDaoImpl.closeCurrentSessionwithTransaction();
	}

	public List<TicMatch> findAll() {
		matchDaoImpl.openCurrentSession();
		List<TicMatch> books = matchDaoImpl.findAll();
		matchDaoImpl.closeCurrentSession();
		return books;
	}

	public void deleteAll() {
		matchDaoImpl.openCurrentSessionwithTransaction();
		matchDaoImpl.deleteAll();
		matchDaoImpl.closeCurrentSessionwithTransaction();
	}

	public TicMatchDaoImpl matchDao() {
		return matchDaoImpl;
	}

	public void SaveOrUpdate(TicMatch match) {

		matchDaoImpl.openCurrentSessionwithTransaction();
		matchDaoImpl.saveOrUpdate(match);
		matchDaoImpl.closeCurrentSessionwithTransaction();
	}
}