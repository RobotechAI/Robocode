package sampleRobots;

import robocode.*;
import java.awt.geom.Rectangle2D;
import robocode.util.Utils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class DataCollectorBot extends AdvancedRobot {
    public int grid = 8;
    private int currentRow = 0;
    private int currentCol = 0;

    private final Object lock = new Object();
    public Object[][] data = new Object[grid][grid];

    private Rectangle2D.Double battlefield;

    private double squareWidth;
    private double squareHeight;

    private Map<String, String> enemyPositions = new HashMap<>();
    private Map<String, String> currentEnemyPositions = new HashMap<>();

    /**
     * run: DataCollectorBot's default behavior
     */
    public void run() {
        setAdjustRadarForRobotTurn(true);
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

        // Robot main loop
        while (true) {
            turnRadarRight(360);
            synchronized (lock) {
                moverParaProximaPosicao();
            }
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
        // Atualizar a posição anterior do robô para 0 na matriz de dados
        if (!data[currentRow][currentCol].equals(1) || data[currentRow][currentCol].equals("X")) {
            data[currentRow][currentCol] = 0;
        }

        double x = battlefield.x + currentCol * squareWidth + squareWidth / 2;
        double y = battlefield.y + currentRow * squareHeight + squareHeight / 2;
        goTo(x, y);

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
        int row = (int) ((battlefield.height - (scannedY - battlefield.y)) / squareHeight);
        int col = (int) ((scannedX - battlefield.x) / squareWidth);

        // Ensure indices are within bounds
        if (row < 0)
            row = 0;
        if (row >= grid)
            row = grid - 1;
        if (col < 0)
            col = 0;
        if (col >= grid)
            col = grid - 1;

        // Adicionar a posição do inimigo ao conjunto de posições de inimigos detectados na atual iteração
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

        fire(1);
    }

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
     * onHitWall: What to do when you hit a wall
     */
    public void onHitWall(HitWallEvent e) {
        back(20);
    }

    /**
     * onHitRobot: What to do when you collide with another robot
     */
    public void onHitRobot(HitRobotEvent e) {
        // If the other robot is in front of us, move back a bit and turn right
        if (e.getBearing() > -90 && e.getBearing() <= 90) {
            back(100);
            turnRight(90);
        }
        // If the other robot is behind us, move forward a bit and turn right
        else {
            ahead(100);
            turnRight(90);
        }
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
        synchronized (lock) {
            data[currentRow][currentCol] = "X";
            saveDataMatrixToCSV("battlefield_data.csv");
        }
    }
}
