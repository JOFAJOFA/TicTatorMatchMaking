package com.jofa.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jofa.elo.model.UserElo;
import com.jofa.elo.service.UserEloService;
import com.jofa.match.model.TicMatch;
import com.jofa.match.service.TicMatchService;

@RestController
@RequestMapping("/")
public class MatchMakingController {

	private enum STATES {
		ONILINE, LOOKINGFORGAME, INGAME
	};

	@Autowired
	private static TicMatchService matchService = new TicMatchService();
	@Autowired
	private static UserEloService eloService = new UserEloService();

	private Map<String, STATES> onlineUsers = new HashMap<String, STATES>();

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/AddToOnlineUsers/{username}", method = RequestMethod.GET)
	public ResponseEntity AddToOnlineUserList(@PathVariable String username) {

		onlineUsers.put(username, STATES.ONILINE);
		return new ResponseEntity(HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/ChangeUserStatetToLFG/{username}", method = RequestMethod.GET)
	public ResponseEntity ChangeUserStateToTLFG(@PathVariable String username) {

		onlineUsers.put(username, STATES.LOOKINGFORGAME);
		return new ResponseEntity(HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/AddToEloTable/{username}", method = RequestMethod.GET)
	public ResponseEntity AddToEloTable(@PathVariable String username) {

		// 800 is the starting elo for every player
		eloService.persist(new UserElo(username, 800, 0, 0, 0));
		return new ResponseEntity(HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/GameHasEnded/", method = RequestMethod.POST)
	public ResponseEntity GameHasEnded(@RequestBody TicMatch match) {
		
		System.out.println(match.toString());
		
		matchService.persist(match);
		
		//We need the commented out stuff if we only send an "empty" UserElo object with only a user name set.
		// If we send the whole object we don't need the commented out stuff
		
		UserElo winner =/* eloService.findByUserName(*/match.getUserEloByWUsername();//.getUsername());
		UserElo loser = /* eloService.findByUserName(*/match.getUserEloByLUsername();//.getUsername());

		if(match.isDraw())
		{
			updateDrawsForPlayer(winner);
			updateDrawsForPlayer(loser);
		}
		else
		{
			eloService.recalculateEloRating(winner,loser);
		}
		
		return new ResponseEntity(HttpStatus.OK);
	}

	private void updateDrawsForPlayer(UserElo player) {
		
		player.incrementDraws(); 
		eloService.update(player);
	}
	
	/*@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/TEST/{username}", method = RequestMethod.GET)
	public ResponseEntity TEST(@PathVariable String username) {
		
		Object b = new Object();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> entity = new HttpEntity<Object>(b, headers);
		
		RestTemplate asd = new RestTemplate();
		asd.getForObject("http://192.168.220.129:8079/" ,HttpEntity.class,entity);
		
		return null;
		
	}*/

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/RemoveUserFromList/{username}", method = RequestMethod.GET)
	public ResponseEntity RemoveUserFromList(@PathVariable String username) {

		onlineUsers.remove(username);
		return new ResponseEntity(HttpStatus.OK);
	}

}
