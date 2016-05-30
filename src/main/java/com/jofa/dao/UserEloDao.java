package com.jofa.dao;

import com.jofa.model.UserElo;

public interface UserEloDao  extends GenericDao<UserElo, Integer> {	
	
	public UserElo findByUserName(String id);	

	public UserElo findById(Integer id);
	
}
