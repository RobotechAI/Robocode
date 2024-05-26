package example;

import robocode.*;
import java.awt.geom.Rectangle2D;
import robocode.util.Utils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

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

        battlefield = new Rectangle2D.Double(0,0, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
        squareWidth = battlefield.width / grid;
        squareHeight = battlefield.height / grid;

        // Initialize the data matrix
        initializeDataMatrix();

        // Create a TimerTask to update the matrix and save to CSV every 2 seconds
        TimerTask task = new TimerTask() {
            public void run() {
                updateAndSaveDataMatrix();
            }
        };

        // Schedule the task to run every 2 seconds
        Timer timer = new Timer();
        timer.schedule(task, 0, 1000);

        // Robot main loop
        while (true) {
            turnGunRight(90);
            turnGunRight(90);
            moverParaProximaPosicao();
            execute();
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
        int prevRow = currentRow;
        int prevCol = currentCol;
    
        double x = battlefield.x + currentCol * squareWidth + squareWidth / 2;
        double y = battlefield.y + currentRow * squareHeight + squareHeight / 2;
        goTo(x, y);
    
        // Calculate new row and column
        int newRow = (int)((battlefield.height - (getY() - battlefield.y)) / squareHeight);
        int newCol = (int)((getX() - battlefield.x) / squareWidth);
    
        // Ensure indices are within bounds
        if (newRow < 0) newRow = 0;
        if (newRow >= grid) newRow = grid - 1;
        if (newCol < 0) newCol = 0;
        if (newCol >= grid) newCol = grid - 1;
    
        // Reset the previous position if it was marked as "X"
        if (data[prevRow][prevCol] instanceof String && data[prevRow][prevCol].equals("X")) {
            data[prevRow][prevCol] = 0;
        }
    
        // Mark the new position of the robot if it's not already marked
        if (!(data[newRow][newCol] instanceof String)) {
            data[newRow][newCol] = "X";
        }
    
        // Update current row and column
        currentRow = newRow;
        currentCol = newCol;
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
        // Calculate the position of the scanned robot
        double angle = Math.toRadians(getHeading() + e.getBearing());
        double scannedX = getX() + e.getDistance() * Math.sin(angle);
        double scannedY = getY() + e.getDistance() * Math.cos(angle);
    
        // Calculate the matrix indices
        int row = (int)((battlefield.height - (scannedY - battlefield.y)) / squareHeight);
        int col = (int)((scannedX - battlefield.x) / squareWidth);
    
        // Ensure indices are within bounds
        if (row < 0) row = 0;
        if (row >= grid) row = grid - 1;
        if (col < 0) col = 0;
        if (col >= grid) col = grid - 1;
    
        // Update the matrix only if there is no robot already registered in that square
        if (!(data[row][col] instanceof String)) {
            if (data[row][col] instanceof Integer) {
                data[row][col] = (Integer)data[row][col] + 1;
            } else {
                data[row][col] = 1;
            }
        }
    
        fire(1);
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        double x = getX();
        double y = getY();
        int row = (int)((y - battlefield.y) / squareHeight);
        int col = (int)((x - battlefield.x) / squareWidth);

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

    private void updateAndSaveDataMatrix() {
        updateMatrixValues();
        saveDataMatrixToCSV("battlefield_data.csv");
    }

    private void updateMatrixValues() {
        for (int i = 0; i < grid; i++) {
            for (int j = 0; j < grid; j++) {
                // Check if the square is empty and update the value
                if (!(data[i][j] instanceof String) && !(data[i][j] instanceof Integer)) {
                    data[i][j] = 0;
                }
                // Update the value to -1 if there are no more bullets in the square
                if (data[i][j] instanceof Integer && (Integer)data[i][j] == -1) {
                    data[i][j] = 0;
                }
            }
        }
    }
}
