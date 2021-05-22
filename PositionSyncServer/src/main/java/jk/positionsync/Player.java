package jk.positionsync;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.Timer;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Player {

	private static final int HEART_BEAT_INTERVAL = 10;

	// Owned by this object
	private int id;
	private Circle circle;

	// Internal use for the timer
	private Timer timer;
	private long lastTrigger = System.currentTimeMillis();
	private String previousStatus = "";

	// Pass by refer about the session and neighbor players
	private WebSocketSession webSocketSession;
	private final List<Player> playerListReference;

	public Player(int id, WebSocketSession webSocketSession, List<Player> playerListReference) {
		this.id = id;
		this.webSocketSession = webSocketSession;
		this.playerListReference = playerListReference;
		circle = new Circle(0, 0);
		startHeartBeat();
	}

	public void setCircleTarget(double x, double y) {
		circle.setTarget(x, y);
	}

	public WebSocketSession getWebsocket() {
		return webSocketSession;
	}

	public boolean checkPlayerMatchSession(WebSocketSession webSocketSession) {
		return this.webSocketSession == webSocketSession;
	}

	public String getLatestStatus() {
		Point curPosition = circle.getCurrentPosition();
		Point targetPosition = circle.getTargetPosition();
		return id + "," + curPosition.x + "," + curPosition.y + "," + targetPosition.x + "," + targetPosition.y;
	}

	public void forceToBroadcastAgain() {
		previousStatus = "";
	}

	private void startHeartBeat() {
		timer = new Timer(HEART_BEAT_INTERVAL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				heartBeat();
				if (!webSocketSession.isOpen()) {
					timer.stop();
					timer = null;
				}
			}
		});
		timer.start();
	}

	private void heartBeat() {
		long now = System.currentTimeMillis();
		double deltaTime = (double) (now - lastTrigger) / 1000;

		// Discard if lag
		if (deltaTime >= 1 || deltaTime < 0)
			deltaTime = 1;
		lastTrigger = now;
		circle.move(deltaTime);
		String statusOfThisPlayer = getLatestStatus();

		// Avoid too much network resource
		if (previousStatus.equals(statusOfThisPlayer))
			return;

		previousStatus = statusOfThisPlayer;

		for (Player otherPlayer : playerListReference) {
			try {
				otherPlayer.getWebsocket().sendMessage(new TextMessage(statusOfThisPlayer));
			} catch (IOException e1) {
				System.out.println("Fail to Send Message from Player: " + id);
			}
		}
	}
}
