package com.jofa.match.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import com.jofa.elo.model.UserElo;

@XmlRootElement
@Entity
@Table(name = "tic_match", catalog = "db_match")
public class TicMatch implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private UserElo userEloByLUsername;
	private UserElo userEloByWUsername;
	private boolean draw;
	private Date matchDate;

	public TicMatch() {
	}

	public TicMatch(UserElo userEloByLUsername, UserElo userEloByWUsername, boolean draw, Date matchDate) {
		this.userEloByLUsername = userEloByLUsername;
		this.userEloByWUsername = userEloByWUsername;
		this.draw = draw;
		this.matchDate = matchDate;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "l_username", nullable = false)
	public UserElo getUserEloByLUsername() {
		return this.userEloByLUsername;
	}

	public void setUserEloByLUsername(UserElo userEloByLUsername) {
		this.userEloByLUsername = userEloByLUsername;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "w_username", nullable = false)
	public UserElo getUserEloByWUsername() {
		return this.userEloByWUsername;
	}

	public void setUserEloByWUsername(UserElo userEloByWUsername) {
		this.userEloByWUsername = userEloByWUsername;
	}

	@Column(name = "draw", nullable = false)
	public boolean isDraw() {
		return this.draw;
	}

	public void setDraw(boolean draw) {
		this.draw = draw;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "match_date", /*nullable = false,*/ length = 19,columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP",insertable=false, updatable=false)
	public Date getMatchDate() {
		return this.matchDate;
	}

	public void setMatchDate(Date matchDate) {
		this.matchDate = matchDate;
	}
	
	@Override
	public String toString()
	{
		return "["+this.getId()+ " | " + this.getMatchDate()+ " | " + this.getUserEloByWUsername().getUsername()+ " | " + this.getUserEloByLUsername().getUsername()+" | " + this.draw+"]";
	}
	
	
}
