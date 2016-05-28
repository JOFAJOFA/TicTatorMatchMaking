package com.jofa.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.jofa.elo.model.UserElo;
import com.jofa.elo.service.UserEloService;
import com.jofa.match.model.TicMatch;
import com.jofa.match.service.TicMatchService;

@Component("controller")
@RestController
@RequestMapping("/")
public class MatchMakingController
{
	//States that a user can have
	private enum STATES
	{
		ONILINE, LOOKINGFORGAME, INGAME
	};

	//Load balancer ip
	private static final String LoadBalancer_URL = "http://192.168.220.129:8079";

	@Autowired
	private static TicMatchService matchService = new TicMatchService();
	@Autowired
	private static UserEloService eloService = new UserEloService();

	private Map<String, STATES> onlineUsers = new HashMap<String, STATES>();
	private ArrayList<UserElo> usersLFG = new ArrayList<UserElo>();

	//Changes user's state to online
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/AddToOnlineUsers/{username}", method = RequestMethod.GET)
	public ResponseEntity AddToOnlineUserList(@PathVariable String username)
	{
		onlineUsers.put(username, STATES.ONILINE);
		return new ResponseEntity(HttpStatus.OK);
	}
	
	//Changes user's state to in game
	public void ChangeUserStateToInGame(String username)
	{
		onlineUsers.put(username, STATES.INGAME);
	}

	//Changes user's state to LFG and adds user to the usersLFG list
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/ChangeUserStatetToLFG/{username}", method = RequestMethod.GET)
	public ResponseEntity ChangeUserStateToTLFG(@PathVariable String username)
	{

		onlineUsers.put(username, STATES.LOOKINGFORGAME);

		usersLFG.add(eloService.findByUserName(username));

		return new ResponseEntity(HttpStatus.OK);
	}

	//create new entry in the UserElo table (when we register we are going to call this method from controller
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/AddToUserEloTable/{username}", method = RequestMethod.GET)
	public ResponseEntity AddToUserEloTable(@PathVariable String username)
	{
		// 800 is the starting elo for every player
		eloService.persist(new UserElo(username, 800, 0, 0, 0));
		return new ResponseEntity(HttpStatus.OK);
	}

	//When the game ends we receive a ticMatch object from the game.We insert it to the table then we update the user stats.
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/GameHasEnded/", method = RequestMethod.POST)
	public ResponseEntity GameHasEnded(@RequestBody TicMatch match)
	{
		matchService.persist(match);

		// We need the commented out stuff if we only send an "empty" UserElo
		// object with only a user name set.
		// If we send the whole object we don't need the commented out stuff

		UserElo winner = /* eloService.findByUserName( */match.getUserEloByWUsername();// .getUsername());
		UserElo loser = /* eloService.findByUserName( */match.getUserEloByLUsername();// .getUsername());

		if (match.isDraw()) {
			eloService.updateDrawsForPlayer(winner);
			eloService.updateDrawsForPlayer(loser);
		} else {
			eloService.recalculateEloRating(winner, loser);
		}

		return new ResponseEntity(HttpStatus.OK);
	}

	//QUARTZ runs it every 5s to change  the interval go to ->mvc-dispatcher-servlet.xml 
	public void pairUp()
	{
		System.out.println("IN PAIR UP");
		if (usersLFG.size() > 1) {
			for (int i = 0; i < usersLFG.size() - 1; i++)
			{
								
				for (int j = 0; j < usersLFG.size(); j++)
				{
					if (i != j)
					{
						if (Math.abs(usersLFG.get(i).getElo() - usersLFG.get(j).getElo()) < 100)
						{							
							UserElo player1 = usersLFG.get(i);
							UserElo player2 = usersLFG.get(j);
							
							ChangeUserStateToInGame(player1.getUsername());
							ChangeUserStateToInGame(player2.getUsername());

							usersLFG.remove(player1);		
							usersLFG.remove(player2);	
							
							i = i - 2 < 0 ? 0 : i - 2;

							matchService.SendUserEloObjectsToGameService(LoadBalancer_URL, player1, player2);
							
							break;
						}
					}
				}
			}
		}
	}
	
	//remove players from online/userslfg lists
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/RemoveUserFromList/{username}", method = RequestMethod.GET)
	public ResponseEntity RemoveUserFromList(@PathVariable String username)
	{
		onlineUsers.remove(username);
		usersLFG.remove(username);
		return new ResponseEntity(HttpStatus.OK);
	}
}
