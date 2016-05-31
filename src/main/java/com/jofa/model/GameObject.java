package com.jofa.model;

public class GameObject
{

	private String gameID;
	
	private String IP;
	
	public GameObject(String gameID, String iP)
	{
		this.gameID = gameID;
		this.IP = iP;
	}

	public String getGameID()
	{
		return gameID;
	}

	public void setGameID(String gameID)
	{
		this.gameID = gameID;
	}

	public String getIP()
	{
		return IP;
	}

	public void setIP(String iP)
	{
		IP = iP;
	}

}
