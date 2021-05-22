package jk.positionsync;

import java.awt.Point;

public class Circle {

	public static final double CIRCLE_MOVE_TARGET_SPEED = 100; // This value should match with client

	// Target Position
	private double targetX = 0;
	private double targetY = 0;

	// Current Position
	private double currentX = 0;
	private double currentY = 0;

	public Circle(double initX, double initY) {
		currentX = targetX = initX;
		currentY = targetY = initY;
	}

	public Point getTargetPosition() {
		Point position = new Point();
		position.x = (int) targetX;
		position.y = (int) targetY;
		return position;
	}

	public Point getCurrentPosition() {
		Point position = new Point();
		position.x = (int) currentX;
		position.y = (int) currentY;
		return position;
	}

	public void setTarget(double x, double y) {
		targetX = x;
		targetY = y;
	}

	// Calculate how much pixel to move within this 0.01 second and update new
	// position
	public void move(double deltaTime) {
		moveTo(deltaTime, targetX, targetY, CIRCLE_MOVE_TARGET_SPEED);
	}

	private void moveTo(double deltaTime, double toX, double toY, double speed) {
		double deltaX = toX - currentX;
		double deltaY = toY - currentY;

		// Calculate direction to move
		double direction = Math.atan2(deltaY, deltaX);

		double moveStepSizeX = deltaTime * Math.cos(direction) * speed;
		double moveStepSizeY = deltaTime * Math.sin(direction) * speed;

		// Avoid Over Move X Axis
		if ((moveStepSizeX > 0 && currentX + moveStepSizeX > toX)
				|| (moveStepSizeX < 0 && currentX + moveStepSizeX < toX)) {
			currentX = toX;
		} else {
			currentX += moveStepSizeX;
		}

		// Avoid Over Move Y Axis
		if ((moveStepSizeY > 0 && currentY + moveStepSizeY > toY)
				|| (moveStepSizeY < 0 && currentY + moveStepSizeY < toY)) {
			currentY = toY;
		} else {
			currentY += moveStepSizeY;
		}
	}
}
