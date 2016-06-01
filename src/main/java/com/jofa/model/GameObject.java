package com.jofa.model;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class GameObject
{

	private String gameID;
	
	private String IP;
	
	public GameObject()
	{
		super();
	}
	
	
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
