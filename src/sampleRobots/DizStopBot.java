/*
 * Copyright (c) 2001-2023 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package SampleDizStop;


import robocode.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import robocode.util.Utils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Walls - a sample robot by Mathew Nelson, and maintained by Flemming N. Larsen
 * <p>
 * Moves around the outer edge with the gun facing in.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 */
public class DizStopBot extends AdvancedRobot {

	public int grid = 8;
	private int currentRow = 0;
	private int currentCol = 0;

	private final Object lock = new Object();
	public Object[][] data = new Object[grid][grid];

	private Rectangle2D.Double battlefield;

	private double squareWidth;
	private double squareHeight;
	private double lastEnemyEnergy = 100.0;

	private Map<String, String> enemyPositions = new HashMap<>();
	private Map<String, String> currentEnemyPositions = new HashMap<>();

	private boolean hit = false;

	boolean peek; // Don't turn if there's a robot there
	double moveAmount; // How much to move

	/**
	 * run: Move around the walls
	 */
	public void run() {
		// Set colors
		setBodyColor(Color.black);
		setGunColor(Color.black);
		setRadarColor(Color.orange);
		setBulletColor(Color.cyan);
		setScanColor(Color.cyan);

		battlefield = new Rectangle2D.Double(0, 0, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
		squareWidth = battlefield.width / grid;
		squareHeight = battlefield.height / grid;

		// Initialize the data matrix
		initializeDataMatrix();

		// Create a TimerTask to update the matrix and save to CSV every second
		TimerTask task = new TimerTask() {
			public void run() {
				synchronized (lock) {
					updateAndSaveDataMatrix();
				}
				cleanOldEnemyPositions(); // Limpar posições antigas antes de salvar
			}
		};

		// Schedule the task to run every 2 seconds
		Timer timer = new Timer();
		timer.schedule(task, 0, 1000);

		// Initialize moveAmount to the maximum possible for this battlefield.
		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		// Initialize peek to false
		peek = false;

		// turnLeft to face a wall.
		// getHeading() % 90 means the remainder of
		// getHeading() divided by 90.
		turnLeft(getHeading() % 90);
		ahead(moveAmount);
		// Turn the gun to turn right 90 degrees.
		peek = true;
		turnGunRight(90);
		turnRight(90);

		while (true) {
			for (int i = 0; i < 2; i++) {
				turnRadarRight(360);
				synchronized (lock) {
					moveToNextPosition();
				}
				execute();
			}
			// Look before we turn when ahead() completes.
			peek = true;
			// Move up the wall
			ahead(moveAmount);
			// Don't look now
			peek = false;
			// Turn to the next wall
			turnRight(90);
		}
	}

	/**
	 * initializeDataMatrix: Initialize the data matrix with zeros
	 */
	private void initializeDataMatrix() {
		for (int i = 0; i < grid; i++) {
			for (int j = 0; j < grid; j++) {
				data[i][j] = 0;
			}
		}
	}

	/**
	 * onScannedRobot:  Fire!
	 * @param e
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Adicionar a posição do inimigo ao conjunto de posições de inimigos detectados
		double enemyX = getX() + e.getDistance() * Math.sin(getHeadingRadians() + e.getBearingRadians());
		double enemyY = getY() + e.getDistance() * Math.cos(getHeadingRadians() + e.getBearingRadians());
		double enemyHeading = e.getHeadingRadians();

		// Calcula os indices da matriz
		int row = (int) ((battlefield.height - (enemyY - battlefield.y)) / squareHeight);
		int col = (int) ((enemyX - battlefield.x) / squareWidth);

		// Ensure indices are within bounds
		if (row < 0)
			row = 0;
		if (row >= grid)
			row = grid - 1;
		if (col < 0)
			col = 0;
		if (col >= grid)
			col = grid - 1;

		// Adicionar a posição do inimigo ao conjunto de posições de inimigos detectados
		// na atual iteração
		if (enemyPositions.containsKey(e.getName())) {
			String[] parts = enemyPositions.get(e.getName()).split(",");
			int lastRow = Integer.parseInt(parts[0]);
			int lastCol = Integer.parseInt(parts[1]);
			data[lastRow][lastCol] = 0;
		}

		// Marcar a presença de um bot inimigo na matriz de dados
		data[row][col] = 1;

		// Adicionar a posição do inimigo ao conjunto de posições de inimigos detectados
		enemyPositions.put(e.getName(), row + "," + col);

		// Calcular a variação de energia do inimigo
		double energyDiff = lastEnemyEnergy - e.getEnergy();
		lastEnemyEnergy = e.getEnergy();

		// Se a variação de energia estiver dentro do intervalo de valores de poder de
		// bala, estimamos que ele atirou
		if (energyDiff >= 0.1 && energyDiff <= 4.0) {
			// Calcular a posição estimada da bala
			double bulletSpeed = Rules.getBulletSpeed(energyDiff);
			double time = e.getDistance() / bulletSpeed;

			double bulletX = enemyX + time * bulletSpeed * Math.sin(enemyHeading);
			double bulletY = enemyY + time * bulletSpeed * Math.cos(enemyHeading);

			// Valores calculados da posição estimada da bala
			System.out.println("Estimated Bullet Position - X: " + bulletX + ", Y: " + bulletY);

			// Mapear a posição estimada da bala para a matriz
			int bulletRow = (int) ((battlefield.height - (bulletY - battlefield.y)) / squareHeight);
			int bulletCol = (int) ((bulletX - battlefield.x) / squareWidth);

			// Verificar se a posição estimada está dentro dos limites da matriz
			if (bulletRow >= 0 && bulletRow < grid && bulletCol >= 0 && bulletCol < grid) {
				data[bulletRow][bulletCol] = -1;

				// Imprimir valores calculados da posição estimada da bala
				System.out.println("Estimated Bullet Position - Row: " + bulletRow + ", Col: " + bulletCol);
			} else {
				// Imprimir valores fora dos limites da matriz
				System.out.println("Estimated Bullet Position Out of Bounds");
			}
		}

		fire(2);
	}

	/**
	 * moveToNextPosition: Move to the next position in the matrix
	 */
	private void moveToNextPosition() {
		// Atualizar a posição anterior do robô para 0 na matriz de dados
		if (!data[currentRow][currentCol].equals(1) || data[currentRow][currentCol].equals("X")) {
			data[currentRow][currentCol] = 0;
		}

		double x = battlefield.x + currentCol * squareWidth + squareHeight / 2;
		double y = battlefield.y + currentRow * squareHeight + squareHeight / 2;
		//goTo(x, y);

		// Calculate new row and column
		int newRow = (int) ((battlefield.height - (getY() - battlefield.y)) / squareHeight);
		int newCol = (int) ((getX() - battlefield.x) / squareWidth);

		// Ensure indices are within bounds
		newRow = Math.max(0, Math.min(newRow, grid - 1));
		newCol = Math.max(0, Math.min(newCol, grid - 1));

		// Update current row and column
		currentRow = newRow;
		currentCol = newCol;
	}

	/**private void goTo(double x, double y) {
	 double dx = x - getX();
	 double dy = y - getY();
	 double angleToTarget = Math.atan2(dx, dy);
	 double targetAngle = Utils.normalRelativeAngle(angleToTarget - getHeadingRadians());

	 double distance = Math.hypot(dx, dy);
	 double turnAngle = Math.atan(Math.tan(targetAngle));

	 setTurnRightRadians(turnAngle);
	 if (targetAngle == turnAngle) {
	 setAhead(distance);
	 } else {
	 setBack(distance);
	 }
	 execute();
	 } **/

	/**
	 * cleanOldEnemyPositions: Remove old enemy positions from the data matrix
	 */
	private void cleanOldEnemyPositions() {
		// Criar um novo conjunto para armazenar as posições atuais dos inimigos
		Map<String, String> newEnemyPositions = new HashMap<>();

		// Adicionar as posições atuais dos inimigos detectados na atual iteração
		for (String enemy : enemyPositions.keySet()) {
			if (!currentEnemyPositions.containsKey(enemy)) {
				String[] parts = enemyPositions.get(enemy).split(",");
				int row = Integer.parseInt(parts[0]);
				int col = Integer.parseInt(parts[1]);
				data[row][col] = 0;
			} else {
				newEnemyPositions.put(enemy, enemyPositions.get(enemy));
			}
		}

		// Atualizar o conjunto de posições de inimigos
		enemyPositions = newEnemyPositions;

		// Limpar o conjunto de posições de inimigos detectados na atual iteração
		currentEnemyPositions.clear();
	}

	/**
	 * saveDataMatrixToCSV: Save the data matrix to a CSV file
	 * @param filename
	 */
	private void saveDataMatrixToCSV(String filename) {
		RobocodeFileOutputStream rfos = null;
		BufferedWriter writer = null;

		try {
			rfos = new RobocodeFileOutputStream(getDataFile(filename));
			writer = new BufferedWriter(new OutputStreamWriter(rfos));

			for (int i = 0; i < grid; i++) {
				for (int j = 0; j < grid; j++) {
					writer.write(data[i][j].toString());
					if (j < grid - 1) {
						writer.write(",");
					}
				}
				writer.newLine();
			}

			writer.write("\n");
			writer.write(Boolean.toString(hit));

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (rfos != null) {
					rfos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * updateAndSaveDataMatrix: Update the current position in the matrix and save to CSV
	 */
	private void updateAndSaveDataMatrix() {
		synchronized (lock) {
			data[currentRow][currentCol] = "X";
			saveDataMatrixToCSV("battlefield_data.csv");
		}

	}

	/**
	 * onHitRobot:  Move away a bit.
	 */
	public void onHitRobot(HitRobotEvent e) {
		// If he's in front of us, set back up a bit.
		if (e.getBearing() > -90 && e.getBearing() < 90) {
			back(100);
		} // else he's in back of us, so set ahead a bit.
		else {
			ahead(100);
		}
	}

	/**
	 * onHitBullet: hit = true
	 */
	public void onBulletHit(BulletHitEvent event) {
		out.println("I hit " + event.getName() + "!");
		hit = true;
	}

	/**
	 * onBulletMissed: hit = false
	 */
	public void onBulletMissed(BulletMissedEvent event) {
		out.println("Drat, I missed.");
		hit = false;
	}
}
