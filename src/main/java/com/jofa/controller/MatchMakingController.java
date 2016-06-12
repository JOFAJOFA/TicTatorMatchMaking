package com.jofa.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.jofa.dao.TicMatchDao;
import com.jofa.dao.UserEloDao;
import com.jofa.model.GameObject;
import com.jofa.model.TicMatch;
import com.jofa.model.UserElo;

@EnableScheduling
@RestController
@RequestMapping("/")
public class MatchMakingController
{
	// States that a user can have
	private enum STATES
	{
		ONILINE, LOOKINGFORGAME, INGAME
	};

	
	private static final Logger log = Logger.getLogger(MatchMakingController.class.getName());

	// Load balancer ip
	private static final String LoadBalancer_URL = "http://146.185.163.158:8081/TicTatorGame/addGame";

	@Autowired
	private TicMatchDao ticMatchDao;
	@Autowired
	private UserEloDao userEloDao;

	private Map<String, STATES> onlineUsers = new HashMap<String, STATES>();
	private Map<String, UserElo> usersLFG = new HashMap<String, UserElo>();

	// match id, ip
	private Map<String, String> matchIdsAndIPs = new HashMap<String, String>();

	// player name, match id
	private Map<String, String> playersAndmatchIds = new HashMap<String, String>();

	// create new entry in the UserElo table (when we register we are going to
	// call this method from controller
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/register/{username}", method = RequestMethod.POST)
	public ResponseEntity AddToUserEloTable(@PathVariable String username)
	{
		// 800 is the starting elo for every player
		userEloDao.save(new UserElo(username, 800, 0, 0, 0));
		// log.log(Level.INFO, "USER  HAS BEEN SAVED");

		return new ResponseEntity(HttpStatus.OK);
	}

	// ------------------- CHANGE USER STATES ---------------------------

	// Changes user's state to online
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/userStateONLINE/{username}", method = RequestMethod.POST)
	public ResponseEntity AddToOnlineUserList(@PathVariable String username)
	{
		onlineUsers.put(username, STATES.ONILINE);

		 log.log(Level.INFO, "SET TO ONLINE:" + userEloDao.findByUserName(username).toString());

		return new ResponseEntity(HttpStatus.OK);
	}

	// Changes user's state to LFG and adds user to the usersLFG list
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/userStateLFG/{username}", method = RequestMethod.POST)
	public ResponseEntity ChangeUserStateToTLFG(@PathVariable String username)
	{

		if (!usersLFG.containsKey(username) && onlineUsers.containsKey(username))
		{
			onlineUsers.put(username, STATES.LOOKINGFORGAME);

			usersLFG.put(username, userEloDao.findByUserName(username));

			 log.log(Level.INFO, "SET TO LFG:" + userEloDao.findByUserName(username).toString());
		}

		return new ResponseEntity(HttpStatus.OK);
	}

	// Changes user's state to in game
	public void ChangeUserStateToInGame(String username)
	{
		onlineUsers.put(username, STATES.INGAME);
		 log.log(Level.INFO, username + "  SET TO IN GAME");
	}

	// remove players from online/userslfg lists
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/logout/{username}", method = RequestMethod.POST)
	public ResponseEntity RemoveUserFromList(@PathVariable String username)
	{

		if (onlineUsers.containsKey(username))
		{
			onlineUsers.remove(username);
			 log.log(Level.INFO, username + " REMOVED FROM ONLINE MAP");
		}

		if (usersLFG.containsKey(username))
		{
			usersLFG.remove(username);
			 log.log(Level.INFO, username + " REMOVED FROM LFG LIST");
		}

		String matchId = playersAndmatchIds.get(username);
		matchIdsAndIPs.remove(matchId);

		List<String> keys = new ArrayList<String>(playersAndmatchIds.keySet());
		for (String key : keys)
		{

			if (playersAndmatchIds.get(key) == matchId)
			{
				playersAndmatchIds.remove(key);
			}
		}

		return new ResponseEntity(HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/getInfo", method = RequestMethod.POST)
	public void getInfo()
	{

		Iterator it = usersLFG.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pair = (Map.Entry) it.next();
			 log.log(Level.INFO, "userelo:  " + ((Map.Entry<String, UserElo>) it.next()).getValue());
		}

		
		List<String> keys = new ArrayList<String>(onlineUsers.keySet());
		for (String key : keys)
		{
			 log.log(Level.INFO, "userStateMAP :  " + onlineUsers.get(key));
		}

		keys = new ArrayList<String>(matchIdsAndIPs.keySet());
		for (String key : keys)
		{
			 log.log(Level.INFO, "matchIdsAndIPs :  " + matchIdsAndIPs.get(key));
		}

		keys = new ArrayList<String>(playersAndmatchIds.keySet());
		for (String key : keys)
		{
			 log.log(Level.INFO, "playersAndmatchIds :  " + playersAndmatchIds.get(key));
		}
	}
	// --------------------- SCHEDULED JOB -> PAIRING OF USERS------------------

	@SuppressWarnings("unchecked")
	// pairs up two users to and forwards them to the sendUserToGameService
	// method
	@Scheduled(fixedRate = 5000)
	public void pairUp()
	{
		if (usersLFG.size() > 1)
		{
			// log.log(Level.INFO, "START OLD LIST --------------------------------------------------");

			// log.log(Level.INFO, "LFG LIST SIZE:" + usersLFG.size());

		/*	Iterator it = usersLFG.entrySet().iterator();
			while (it.hasNext())
			{
				
				 log.log(Level.INFO, "userelo:  " + ((Map.Entry<String, UserElo>) it.next()).getValue());
			}*/
						
			Iterator i = usersLFG.entrySet().iterator();
			while (i.hasNext())
			{
				Map.Entry<String, UserElo> entry = (Map.Entry<String, UserElo>)i.next();
				UserElo player1 = entry.getValue();
				
				
				Iterator j = usersLFG.entrySet().iterator();
				while(j.hasNext())
				{
					Map.Entry<String, UserElo> entrySecond = (Map.Entry<String, UserElo>)j.next();
					UserElo player2 = entrySecond.getValue();
					
					if (player1 != player2 && player1 != null && player2 != null)
					{
						if (Math.abs(player1.getElo() - player2.getElo()) < 100)
						{
							try
							{
								SendUserNamesToGameService(LoadBalancer_URL, player1.getUsername(),player2.getUsername());

								ChangeUserStateToInGame(player1.getUsername());
								ChangeUserStateToInGame(player2.getUsername());

								usersLFG.put(entry.getKey(), null);
								usersLFG.put(entrySecond.getKey(), null);
								
								break;

							} catch (HttpClientErrorException ex)
							{
								System.out.println(ex.toString());
							}
						}
					}
				}
			}
			
			ArrayList<String> keys = new ArrayList<String>();
			
			Iterator itn = usersLFG.entrySet().iterator();
			while (itn.hasNext())
			{
				Map.Entry<String,UserElo> pair = (Map.Entry<String,UserElo>) itn.next();
				
				if(pair.getValue() == null)
				{
					keys.add(pair.getKey());
				}
			}
			
			for (String key : keys)
			{
				usersLFG.remove(key);
			}
		
			usersLFG.remove(null);

			// log.log(Level.INFO, "START NEW LIST --------------------------------------------------");
			// log.log(Level.INFO, "LFG LIST SIZE:" + usersLFG.size());

			/*Iterator id = usersLFG.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry pair = (Map.Entry) id.next();
				// log.log(Level.INFO, "userelo:  " + ((Map.Entry<String, UserElo>) id.next()).getValue());
			}	*/		
			// log.log(Level.INFO, "END NEW LIST --------------------------------------------------");
		}
	}

	// Sends a json object to load balancer
	@SuppressWarnings("unchecked")
	public void SendUserNamesToGameService(String URL, String player1, String player2)
	{
		try
		{
			UUID gameId = java.util.UUID.randomUUID();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			org.json.simple.JSONObject simpleJson = new JSONObject();
			simpleJson.put("gameId", gameId.toString());
			simpleJson.put("player1", player1);
			simpleJson.put("player2", player2);

			HttpEntity<String> entity = new HttpEntity<String>(simpleJson.toJSONString(), headers);

			 log.log(Level.INFO, simpleJson.toJSONString());

			RestTemplate restTemplate = new RestTemplate();

			playersAndmatchIds.put(player1, gameId.toString());
			playersAndmatchIds.put(player2, gameId.toString());

			 log.log(Level.INFO, playersAndmatchIds.values());

			matchIdsAndIPs.put(gameId.toString(), null);

			// log.log(Level.INFO, matchIdsAndIPs.values());

			restTemplate.postForEntity(URL, entity, String.class);

			System.out.println("PLAYERS HAVE BEENT SENT TO THE GAME SERVICE");
		} catch (HttpClientErrorException ex)
		{

			System.out.println("FAILED TO CONNECT REASON : ");
			System.out.println(ex.toString());
		}
	}

	// --------------- AJAX CALL METHODS/IP FROM GS -----------

	// Ajax method to check if we have an ip
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/getGame/{username}", method = RequestMethod.GET)
	public ResponseEntity<GameObject> getGame(@PathVariable String username)
	{
		if (playersAndmatchIds.get(username) != null && onlineUsers.containsKey(username))
		{
			if (matchIdsAndIPs.get(playersAndmatchIds.get(username)) != null && onlineUsers.containsKey(username))
			{
				GameObject gO = new GameObject(playersAndmatchIds.get(username),
						matchIdsAndIPs.get(playersAndmatchIds.get(username)));

				 log.log(Level.INFO, "COTROLLER RECEIVED  MATCH ID: " + gO.getGameID() + "  IP:" + gO.getIP());

				return new ResponseEntity<GameObject>(gO, HttpStatus.OK);
			}
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	// Ajax method to check if we have an ip
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/getMatchIp/{username}", method = RequestMethod.GET)
	public ResponseEntity getMatchIp(@PathVariable String username)
	{
		if (matchIdsAndIPs.get(playersAndmatchIds.get(username)) != null && onlineUsers.containsKey(username))
		{

			return new ResponseEntity<>(matchIdsAndIPs.get(playersAndmatchIds.get(username)), HttpStatus.OK);
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/setIp/{gameID}", method = RequestMethod.POST)
	public ResponseEntity setIP(HttpServletRequest request, @PathVariable String gameID)
	{
		 log.log(Level.INFO, "IP RECEIVED FROM GS: "+"GAME ID: " + gameID + "    IP: " + request.getRemoteAddr().toString());

		if (matchIdsAndIPs.containsKey(gameID))
		{
			matchIdsAndIPs.put(gameID, request.getRemoteAddr());
		}

		return new ResponseEntity(HttpStatus.OK);
	}

	// ---------------------- GAME IS OVER -> ELO STUFF -----------------------

	// When the game ends we receive a ticMatch object from the game.We insert
	// it to the table then we update the user stats.
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/gameResult", method = RequestMethod.POST)
	public ResponseEntity gameResult(HttpServletRequest request) throws IOException, ParseException
	{

		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String sCurrentLine;
		String jsonString = "";
		while ((sCurrentLine = br.readLine()) != null)
		{
			jsonString += sCurrentLine;
		}

		JSONObject json = (JSONObject) new JSONParser().parse(jsonString);

		UserElo winner = new UserElo(), loser = new UserElo();
		boolean draw = false;

		switch (json.get("result").toString())
		{
		case "draw":
			draw = true;
			winner = userEloDao.findByUserName(json.get("player1").toString());
			loser = userEloDao.findByUserName(json.get("player2").toString());

			break;
		case "playerX":
			winner = userEloDao.findByUserName(json.get("player1").toString());
			loser = userEloDao.findByUserName(json.get("player2").toString());

			break;
		case "playerO":
			loser = userEloDao.findByUserName(json.get("player1").toString());
			winner = userEloDao.findByUserName(json.get("player2").toString());

			break;
		default:
			break;
		}

		 log.log(Level.INFO, "loser: " + loser.toString());
		 log.log(Level.INFO, "WINNER: " + winner.toString());

		updateUsers(winner, loser, draw);

		String player1 = json.get("player1").toString();
		String player2 = json.get("player2").toString();

		String matchId = playersAndmatchIds.get(json.get("player2").toString());

		matchIdsAndIPs.remove(matchId);
		playersAndmatchIds.remove(player1);
		playersAndmatchIds.remove(player2);

		onlineUsers.put(player1, STATES.ONILINE);
		onlineUsers.put(player2, STATES.ONILINE);

		return new ResponseEntity(HttpStatus.OK);
	}

	private void updateUsers(UserElo winner, UserElo loser, boolean draw)
	{
		onlineUsers.put(winner.getUsername(), STATES.ONILINE);
		onlineUsers.put(loser.getUsername(), STATES.ONILINE);

		 log.log(Level.INFO, "USERS CHANGED BACK TO ONLINE");

		if (draw)
		{
			updateDrawsForPlayer(winner);
			updateDrawsForPlayer(loser);

			 log.log(Level.INFO, "DRAWS UPDATED IN DB");

		} else
		{
			recalculateEloRating(winner, loser);
		}

		TicMatch match = new TicMatch(winner, loser, draw, null);

		 log.log(Level.INFO, match.toString());

		ticMatchDao.save(match);
	}

	// constant for ELO calculation
	static final int K = 32;

	/* ELO CALCULATION */
	public void recalculateEloRating(UserElo winner, UserElo loser)
	{
		winner.incrementWins();
		loser.incrementLosses();

		int winnerElo = winner.getElo();
		int loserElo = loser.getElo();

		int averageElo = (winnerElo + loserElo) / 2;

		winner.setElo(winnerElo + EloChange(averageElo, winnerElo, true));
		loser.setElo(loserElo - EloChange(averageElo, loserElo, false));

		userEloDao.update(winner);
		userEloDao.update(loser);
	}

	private int EloChange(int averageElo, int playerElo, boolean isVictory)
	{
		int res = (int) Math.round(ChanceToWin(averageElo, playerElo) * K);
		return isVictory ? K - res : res;
	}

	private double ChanceToWin(int averageElo, int playerElo)
	{
		return 1 / (1 + Math.pow(10.0, (averageElo - (double) playerElo) / 400));
	}

	// update draw count for player
	public void updateDrawsForPlayer(UserElo player)
	{
		player.incrementDraws();
		userEloDao.update(player);
	}
}
