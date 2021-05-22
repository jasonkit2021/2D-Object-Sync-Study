package CircleSyncClient;

import java.awt.Color;
import java.awt.Point;

public class Circle {

	private static final double CIRCLE_MOVE_AMEND_SPEED = 1000;
	private static final double CIRCLE_MOVE_TARGET_SPEED = 100; // This value should match with server
	private static final double CIRCLE_MOVE_AMEND_POSITION_ALLOW_ERROR = 3;
	private static final double CIRCLE_MOVE_TARGET_POSITION_ALLOW_ERROR = 0.001;

	private enum State {
		IDLE, GO_TO_AMEND_POS, GO_TO_TARGET_POS
	};

	private State state = State.IDLE;

	private int id = 0;

	// Target Position
	private double targetX = 0;
	private double targetY = 0;

	// Amended Position
	private double amendX = 0;
	private double amendY = 0;

	// Current Position
	private double currentX = 0;
	private double currentY = 0;

	// Basic Property
	private int width = 30;
	private int height = 30;
	private Color color = Color.GREEN;

	public Circle(int id, Color color, double initX, double initY) {
		this.id = id;
		this.color = color;
		currentX = targetX = amendX = initX;
		currentY = targetY = amendY = initY;
	}

	public int getId() {
		return id;
	}

	public Point getPosition() {
		Point position = new Point();
		position.x = (int) currentX;
		position.y = (int) currentY;
		return position;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Color getColor() {
		return color;
	}

	public void setAmend(double x, double y) {
		state = State.GO_TO_AMEND_POS;
		amendX = x;
		amendY = y;
	}

	public void setTarget(double x, double y) {
		state = State.GO_TO_AMEND_POS;
		targetX = x;
		targetY = y;
	}

	// Calculate how much pixel to move within this 0.01 second and update new
	// position
	public void move(double deltaTime) {
		if (state == State.IDLE) {
			return;
		}

		if (state == State.GO_TO_AMEND_POS) {
			boolean isReachAmend = moveTo(deltaTime, amendX, amendY, CIRCLE_MOVE_AMEND_SPEED,
					CIRCLE_MOVE_AMEND_POSITION_ALLOW_ERROR);
			if (isReachAmend)
				state = State.GO_TO_TARGET_POS;
		}

		if (state == State.GO_TO_TARGET_POS) {
			boolean isReachTarget = moveTo(deltaTime, targetX, targetY, CIRCLE_MOVE_TARGET_SPEED,
					CIRCLE_MOVE_TARGET_POSITION_ALLOW_ERROR);
			if (isReachTarget)
				state = State.IDLE;
		}
	}

	private boolean moveTo(double deltaTime, double toX, double toY, double speed, double allowError) {
		double deltaX = toX - currentX;
		double deltaY = toY - currentY;

		// If this circle is close enough to the destination
		if (Math.abs(deltaX) < allowError && Math.abs(deltaY) < allowError) {
			return true;
		} else {
			/*
			 * if (state == State.GO_TO_AMEND_POS)
			 * System.out.println("Position amendment is required");
			 */
		}

		// Calculate direction to move
		double direction = Math.atan2(deltaY, deltaX);

		double moveStepSizeX = deltaTime * Math.cos(direction) * speed;
		double moveStepSizeY = deltaTime * Math.sin(direction) * speed;

		boolean isReachX = false, isReachY = false;

		// Avoid Over Move X Axis
		if ((moveStepSizeX > 0 && currentX + moveStepSizeX > toX)
				|| (moveStepSizeX < 0 && currentX + moveStepSizeX < toX)) {
			currentX = toX;
			isReachX = true;
		} else {
			currentX += moveStepSizeX;
		}

		// Avoid Over Move Y Axis
		if ((moveStepSizeY > 0 && currentY + moveStepSizeY > toY)
				|| (moveStepSizeY < 0 && currentY + moveStepSizeY < toY)) {
			currentY = toY;
			isReachY = true;
		} else {
			currentY += moveStepSizeY;
		}

		return isReachX && isReachY;
	}
}
