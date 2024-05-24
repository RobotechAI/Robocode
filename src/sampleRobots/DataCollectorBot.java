package sampleRobots;

import robocode.*; 
import java.awt.geom.Rectangle2D;
import robocode.util.Utils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * DataCollectorBot - a robot by (your name here)
 */
public class DataCollectorBot extends AdvancedRobot {

    public int grid = 8;
    public Object[][] data = new Object[grid][grid];
    private Rectangle2D.Double battlefield;
    private double squareWidth;
    private double squareHeight;
    private int currentRow = 0;
    private int currentCol = 0;

    /**
     * run: DataCollectorBot's default behavior
     */
    public void run() {

        battlefield = new Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
        squareWidth = battlefield.width / grid;
        squareHeight = battlefield.height / grid;

        // Initialize the data matrix
        initializeDataMatrix();

        while (true) {
            turnGunRight(90);
            turnGunRight(90);
            moverParaProximaPosicao();
            execute();

            saveDataMatrixToCSV("battlefield_data.csv");
        }
    }

    private void initializeDataMatrix() {
        for (int i = 0; i < grid; i++) {
            for (int j = 0; j < grid; j++) {
                data[i][j] = 0;
            }
        }
    }

    private void moverParaProximaPosicao() {
        double x = battlefield.x + currentCol * squareWidth + squareWidth / 2;
        double y = battlefield.y + currentRow * squareHeight + squareHeight / 2;
        goTo(x, y);

        // Update the matrix for the robot's current position
        data[currentRow][currentCol] = "X";

        currentCol++;
        if (currentCol >= grid) {
            currentCol = 0;
            currentRow++;
            if (currentRow >= grid) {
                currentRow = 0;
            }
        }
    }

    private void goTo(double x, double y) {
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
    }

    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        double x = getX();
        double y = getY();
        int row = (int)((y - battlefield.x) / squareHeight);
        int col = (int)((x - battlefield.y) / squareWidth);

        if (data[row][col] instanceof Integer) {
            data[row][col] = (Integer)data[row][col] + 1;
        } else if (data[row][col] instanceof String) {
            data[row][col] = 1;
        }

        fire(1);
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        double x = getX();
        double y = getY();
        int row = (int)((y - battlefield.x) / squareHeight);
        int col = (int)((x - battlefield.y) / squareWidth);

        data[row][col] = -1; // Mark the position with -1 when hit by a bullet

        back(10);
    }

    /**
     * onHitWall: What to do when you hit a wall
     */
    public void onHitWall(HitWallEvent e) {
        back(20);
    }

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
}
