package CircleSyncClient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

public class DrawerPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public static final int YOUR_ID = 0;
	
	private List<Circle> circleList = new CopyOnWriteArrayList<>();
	private long lastTrigger = System.currentTimeMillis();
	
	public DrawerPanel() {
		
		lastTrigger = System.currentTimeMillis();
		
		Timer timer = new Timer(10, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				long now = System.currentTimeMillis();
				double deltaTime = (double)(now - lastTrigger) / 1000;
				
				//	Discard if lag
				if (deltaTime >= 1 || deltaTime < 0)
					deltaTime = 1;
				lastTrigger = now;
				
				for (Circle circle : circleList) {
					//circle.setTarget(x, y);
					circle.move(deltaTime);
				}
				repaint();
			}
		});
		timer.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (Circle circle : circleList) {
			g.setColor(circle.getColor());
			
			Point position = circle.getPosition();
			int width = circle.getWidth();
			int height = circle.getHeight();
			g.fillOval(position.x - width / 2, position.y - height / 2, width, height);
		}
	}
	
	public void syncCircleById(int id, double amendX, double amendY, double targetX, double targetY) {
		for (Circle circle : circleList) {
			if (circle.getId() == id) {
				circle.setAmend(amendX, amendY);
				circle.setTarget(targetX, targetY);
				
				//	The circle session is closed
				if (targetX < 0 || targetY < 0)
					circleList.remove(circle);
				
				return;
			}
		}
		
		//	New Circle
		Color color = Color.RED;
		if (id == YOUR_ID)
			color = Color.BLUE;
		Circle newCircle = new Circle(id, color, targetX, targetY);
		circleList.add(newCircle);
		syncCircleById(id, amendX, amendY, targetX, targetY);
	}
}