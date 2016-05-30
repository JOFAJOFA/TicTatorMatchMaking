package com.jofa.model;

public class DummyMatch
{
	
	private String player1;
	private String player2;
	private String gameID;
	private String result;
	
	public DummyMatch(String player1, String player2, String gameID, String result)
	{
		this.player1 = player1;
		this.player2 = player2;
		this.gameID = gameID;
		this.result = result;
	}
	
	public String getPlayer1()
	{
		return player1;
	}
	public void setPlayer1(String player1)
	{
		this.player1 = player1;
	}
	public String getPlayer2()
	{
		return player2;
	}
	public void setPlayer2(String player2)
	{
		this.player2 = player2;
	}
	public String getGameID()
	{
		return gameID;
	}
	public void setGameID(String gameID)
	{
		this.gameID = gameID;
	}
	public String getResult()
	{
		return result;
	}
	public void setResult(String result)
	{
		this.result = result;
	}
	

}
