package CircleSyncClient;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JFrame;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

public class Main {

	// Temporary Unique Identifier for Your Circle
	private int yourId = (int) (Math.random() * 1000000);

	private DrawerPanel drawerPanel = new DrawerPanel();

	private WebSocketClient webSocketClient;

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		handleJFrame(); // Handle JFrame UI Rendering
		handleMouseEvent(); // Handle mouse event
		communicateWithServer(); // Handle connection to websocket
	}

	private void handleJFrame() {
		// Init JFrame
		JFrame frame = new JFrame("Circle Sync");

		// Add thing to render
		frame.add(drawerPanel);
		frame.setVisible(true);

		// Adjust Size
		frame.setPreferredSize(new Dimension(300, 300));
		frame.setResizable(false);
		frame.pack();

		// Disconnect websocket connection on close
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (webSocketClient != null && webSocketClient.isOpen())
					webSocketClient.close();
			}
		});
	}

	private void handleMouseEvent() {
		// Mouse control circle position
		drawerPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				if (webSocketClient != null && webSocketClient.isOpen())
					webSocketClient.send(e.getX() + "," + e.getY());
			}

		});
	}

	private void communicateWithServer() {
		// Listen to Server
		try {
			webSocketClient = new WebSocketClient(new URI("ws://localhost:8080/web-socket?id=" + yourId),
					new Draft_6455()) {
				@Override
				public void onMessage(String message) {
					String[] messages = message.split(",");
					int id = Integer.parseInt(messages[0]);
					double amendX = Double.parseDouble(messages[1]);
					double amendY = Double.parseDouble(messages[2]);
					double targetX = Double.parseDouble(messages[3]);
					double targetY = Double.parseDouble(messages[4]);
					if (id == yourId)
						id = DrawerPanel.YOUR_ID; // Specifically identify yourself in the panel
					drawerPanel.syncCircleById(id, amendX, amendY, targetX, targetY);
				}

				@Override
				public void onOpen(ServerHandshake handshake) {
					System.out.println("Opened Websocket Connection");
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					System.out.println("Closed Websocket Connection");
				}

				@Override
				public void onError(Exception ex) {
					System.out.println("Websocket Error");
				}

			};
			webSocketClient.connect();
		} catch (URISyntaxException e1) {
			System.out.print("Fail to Connect Server");
		}
	}
}