package com.jofa.dao;

import com.jofa.model.TicMatch;

public interface TicMatchDao extends GenericDao<TicMatch, Integer>  {
	
	public TicMatch findById(Integer id);
}
