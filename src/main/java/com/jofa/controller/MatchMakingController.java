package com.jofa.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.jofa.dao.TicMatchDao;
import com.jofa.dao.UserEloDao;
import com.jofa.model.DummyMatch;
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

	// Load balancer ip
	private static final String LoadBalancer_URL = "http://192.168.220.129:8081/TicTatorGame-1.0-SNAPSHOT/addGame";

	@Autowired
	private TicMatchDao ticMatchDao;
	@Autowired
	private UserEloDao userEloDao;

	private Map<String, STATES> onlineUsers = new HashMap<String, STATES>();
	private ArrayList<UserElo> usersLFG = new ArrayList<UserElo>();

	// match id, ip
	private Map<String, String> matchIdsAndIPs = new HashMap<String, String>();

	// player name, match id
	private Map<String, String> playersAndmatchIds = new HashMap<String, String>();

	// Changes user's state to online
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/AddToOnlineUsers/{username}", method = RequestMethod.GET)
	public ResponseEntity AddToOnlineUserList(@PathVariable String username)
	{
		onlineUsers.put(username, STATES.ONILINE);
		return new ResponseEntity(HttpStatus.OK);
	}

	// Changes user's state to in game
	public void ChangeUserStateToInGame(String username)
	{
		onlineUsers.put(username, STATES.INGAME);
	}

	// Changes user's state to LFG and adds user to the usersLFG list
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/ChangeUserStatetToLFG/{username}", method = RequestMethod.GET)
	public ResponseEntity ChangeUserStateToTLFG(@PathVariable String username)
	{

		onlineUsers.put(username, STATES.LOOKINGFORGAME);
		usersLFG.add(userEloDao.findByUserName(username));

		return new ResponseEntity(HttpStatus.OK);
	}

	// create new entry in the UserElo table (when we register we are going to
	// call this method from controller
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/AddToUserEloTable/{username}", method = RequestMethod.GET)
	public ResponseEntity AddToUserEloTable(@PathVariable String username)
	{
		// 800 is the starting elo for every player
		userEloDao.save(new UserElo(username, 800, 0, 0, 0));
		return new ResponseEntity(HttpStatus.OK);
	}

	// When the game ends we receive a ticMatch object from the game.We insert
	// it to the table then we update the user stats.
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/GameHasEnded/", method = RequestMethod.POST)
	public ResponseEntity GameHasEnded(@RequestBody DummyMatch match)
	{
		System.out.println("YEEY IM HERE FAGS");
		UserElo winner = new UserElo(), loser = new UserElo();
		boolean draw = false;

		switch (match.getResult())
		{
		case "DRAW":
			draw = true;
			winner = userEloDao.findByUserName(match.getPlayer1());
			loser = userEloDao.findByUserName(match.getPlayer2());
			break;
		case "PLAYER_1":
			winner = userEloDao.findByUserName(match.getPlayer1());
			loser = userEloDao.findByUserName(match.getPlayer2());
			break;
		case "PLAYER_2":
			loser = userEloDao.findByUserName(match.getPlayer1());
			winner = userEloDao.findByUserName(match.getPlayer2());
			break;
		}

		ticMatchDao.save(new TicMatch(winner, loser, draw, null));

		if (draw)
		{
			updateDrawsForPlayer(winner);
			updateDrawsForPlayer(loser);
		} else
		{
			recalculateEloRating(winner, loser);
		}

		return new ResponseEntity(HttpStatus.OK);
	}

	// remove players from online/userslfg lists
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/RemoveUserFromList/{username}", method = RequestMethod.GET)
	public ResponseEntity RemoveUserFromList(@PathVariable String username)
	{
		onlineUsers.remove(username);
		usersLFG.remove(username);
		return new ResponseEntity(HttpStatus.OK);
	}

	@Scheduled(fixedRate = 5000)
	public void pairUp()
	{

		System.out.println("IN PAIR UP");
		if (usersLFG.size() > 1)
		{

			System.out.println("START OLD LIST --------------------------------------------------");
			for (UserElo userElo : usersLFG)
			{
				System.out.println(userElo.getUsername() + " : " + userElo.getElo());
			}
			System.out.println("END OLD LIST --------------------------------------------------");

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

							try
							{
								SendUserNamesToGameService(LoadBalancer_URL, player1.getUsername(),
										player2.getUsername());

								ChangeUserStateToInGame(player1.getUsername());
								ChangeUserStateToInGame(player2.getUsername());

								usersLFG.remove(player1);
								usersLFG.remove(player2);

								i = i - 2 < 0 ? 0 : i - 2;
								break;

							} catch (HttpClientErrorException ex)
							{
								System.out.println(ex.toString());
							}
						}
					}
				}
			}
			System.out.println("START NEW LIST --------------------------------------------------");
			for (UserElo userElo : usersLFG)
			{
				System.out.println(userElo.getUsername() + " : " + userElo.getElo());
			}
			System.out.println("END OLD LIST --------------------------------------------------");

		}
	}

	// TEST THIS WHEN FERENC IS READY
	@SuppressWarnings("unchecked")
	public void SendUserNamesToGameService(String URL, String player1, String player2)
	{
		UUID gameId = java.util.UUID.randomUUID();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		org.json.simple.JSONObject simpleJson = new JSONObject();
		simpleJson.put("gameId", gameId.toString());
		simpleJson.put("player1", player1);
		simpleJson.put("player2", player2);

		playersAndmatchIds.put(player1, gameId.toString());
		playersAndmatchIds.put(player2, gameId.toString());

		matchIdsAndIPs.put(gameId.toString(), null);

		HttpEntity<String> entity = new HttpEntity<String>(simpleJson.toJSONString(), headers);

		System.out.println(simpleJson.toJSONString());

		RestTemplate restTemplate = new RestTemplate();
		try
		{
			ResponseEntity response = restTemplate.postForEntity(URL, entity, String.class);

			HttpHeaders contentType = response.getHeaders();

			System.out.println(contentType.toString());

			System.out.println("PLAYERS HAVE BEENT SENT TO THE GAME SERVICE");
		} catch (HttpClientErrorException ex)
		{
			System.out.println("FAILED TO CONNECT REASON : ");
			System.out.println(ex.toString());
		}
	}

	// Ajax method to check if we have an ip
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/gimmeMatchID/{username}", method = RequestMethod.GET)
	public ResponseEntity gimmeMatchID(@PathVariable String username)
	{
		if (playersAndmatchIds.get(username) != null)
		{
			return new ResponseEntity<>(playersAndmatchIds.get(username), HttpStatus.OK);
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	// Ajax method to check if we have an ip
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/gimmeIP/{username}", method = RequestMethod.GET)
	public ResponseEntity gimmeIP(@PathVariable String username)
	{
		if (matchIdsAndIPs.get(playersAndmatchIds.get(username)) != null)
		{
			return new ResponseEntity<>(matchIdsAndIPs.get(playersAndmatchIds.get(username)), HttpStatus.OK);
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/setIP", method = RequestMethod.GET)
	public ResponseEntity gimmeIP(@RequestBody String gameID, String IP)
	{
		matchIdsAndIPs.put(gameID, IP);
		return new ResponseEntity(HttpStatus.OK);
	}

	// update draw count for player
	public void updateDrawsForPlayer(UserElo player)
	{
		player.incrementDraws();
		userEloDao.update(player);
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
}
