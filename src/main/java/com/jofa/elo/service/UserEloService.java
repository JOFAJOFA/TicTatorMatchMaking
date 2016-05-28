package com.jofa.elo.service;

import java.util.List;

import com.jofa.elo.dao.impl.UserEloDaoImpl;
import com.jofa.elo.model.UserElo;

public class UserEloService {

	private static UserEloDaoImpl eloDaoImpl;

	public UserEloService() {
		eloDaoImpl = new UserEloDaoImpl();
	}

	public void persist(UserElo entity) {
		eloDaoImpl.openCurrentSessionwithTransaction();
		eloDaoImpl.persist(entity);
		eloDaoImpl.closeCurrentSessionwithTransaction();
	}

	public void save(UserElo entity) {
		eloDaoImpl.openCurrentSessionwithTransaction();
		eloDaoImpl.save(entity);
		eloDaoImpl.closeCurrentSessionwithTransaction();
	}

	public void update(UserElo entity) {
		eloDaoImpl.openCurrentSessionwithTransaction();
		eloDaoImpl.update(entity);
		eloDaoImpl.closeCurrentSessionwithTransaction();
	}

	public UserElo findById(Integer id) {
		eloDaoImpl.openCurrentSession();
		UserElo book = eloDaoImpl.findById(id);
		eloDaoImpl.closeCurrentSession();
		return book;
	}
	
	public UserElo findByUserName(String username) {
		eloDaoImpl.openCurrentSessionwithTransaction();
		UserElo user = eloDaoImpl.findByUserName(username);
		eloDaoImpl.closeCurrentSessionwithTransaction();
		return user;
	}
	

	public void delete(String username) {
		eloDaoImpl.openCurrentSessionwithTransaction();
		UserElo book = eloDaoImpl.findByUserName(username);
		eloDaoImpl.delete(book);
		eloDaoImpl.closeCurrentSessionwithTransaction();
	}

	public List<UserElo> findAll() {
		eloDaoImpl.openCurrentSession();
		List<UserElo> books = eloDaoImpl.findAll();
		eloDaoImpl.closeCurrentSession();
		return books;
	}

	public void deleteAll() {
		eloDaoImpl.openCurrentSessionwithTransaction();
		eloDaoImpl.deleteAll();
		eloDaoImpl.closeCurrentSessionwithTransaction();
	}

	public UserEloDaoImpl eloDao() {
		return eloDaoImpl;
	}

	public void SaveOrUpdate(UserElo user) {

		eloDaoImpl.openCurrentSessionwithTransaction();
		eloDaoImpl.saveOrUpdate(user);
		eloDaoImpl.closeCurrentSessionwithTransaction();
	}

	//constant for ELO calculation
	static final int K = 32;
	
	/* ELO CALCULATION */
	public void recalculateEloRating(UserElo winner,UserElo loser) 
	{				
		winner.incrementWins();
		loser.incrementLosses();
		
		int winnerElo = winner.getElo();
		int loserElo = loser.getElo();
		
		int averageElo = (winnerElo + loserElo) / 2;

		winner.setElo(winnerElo + EloChange(averageElo, winnerElo, true));
		loser.setElo(loserElo - EloChange(averageElo, loserElo, false));
		
		update(winner);
		update(loser);
	}

	public int EloChange(int averageElo, int playerElo, boolean isVictory) {
		int res = (int) Math.round(ChanceToWin(averageElo, playerElo) * K);
		return isVictory ? K - res : res;
	}

	public double ChanceToWin(int averageElo, int playerElo) {
		return 1 / (1 + Math.pow(10.0, (averageElo - (double) playerElo) / 400));
	}
}