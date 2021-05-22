package jk.positionsync;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class PositionSyncWebSocketHandler extends TextWebSocketHandler {

	private final List<Player> players = new CopyOnWriteArrayList<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("Session Added");

		// Extract id e.g. "123" from URL e.g. "ws://localhost:8080/web-socket?id=123"
		int id = Integer.parseInt(session.getUri().toString().split("id=")[1]);
		Player newPlayer = new Player(id, session, players);
		players.add(newPlayer);

		// Force all players to broadcast the information of themselves again
		// Otherwise, the new player does not know the existing players
		for (Player player : players) {
			player.forceToBroadcastAgain();
		}

		super.afterConnectionEstablished(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		System.out.println("Session Disconnected");

		// Find the player according to the session
		String statusOfRemovedPlayer = "";
		for (Player player : players) {
			if (player.checkPlayerMatchSession(session)) {
				player.setCircleTarget(-1, -1); // -ve to represent destroyed
				statusOfRemovedPlayer = player.getLatestStatus();
				players.remove(player);
			}
		}

		// Broadcast the removed player
		for (Player otherPlayer : players) {
			try {
				otherPlayer.getWebsocket().sendMessage(new TextMessage(statusOfRemovedPlayer));
			} catch (IOException e1) {
				System.out.println("Fail to Broadcase Message");
			}
		}

		super.afterConnectionClosed(session, status);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		super.handleTextMessage(session, message);

		// Player sends new mouse control direction to server
		players.forEach(player -> {
			String[] params = message.getPayload().split(",");
			Double targetX = Double.parseDouble(params[0]);
			Double targetY = Double.parseDouble(params[1]);
			if (player.checkPlayerMatchSession(session)) {
				player.setCircleTarget(targetX, targetY);
			}
		});
	}
}