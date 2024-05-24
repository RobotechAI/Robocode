package sampleRobots;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

public class WriterRobot extends AdvancedRobot {

    private class Dados {
        String nome;
        Double distancia;
        Double velocidade;
        Double energia;

        public Dados(String nome, Double distancia, Double velocidade, Double energia) {
            this.nome = nome;
            this.distancia = distancia;
            this.velocidade = velocidade;
            this.energia = energia;
        }
    }

    HashMap<Bullet, Dados> balasNoAr = new HashMap<>();

    private int totalTiros = 0;

    private Rectangle2D.Double battlefield;
    private static final int GRID_SIZE = 8;
    private double squareWidth;
    private double squareHeight;

    private int currentRow = 0;
    private int currentCol = 0;

    private int[][] gridCounts = new int[GRID_SIZE][GRID_SIZE];

    RobocodeFileOutputStream fw;
    public PrintStream out;

    @Override
    public void run() {

        battlefield = new Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
        squareWidth = battlefield.width / GRID_SIZE;
        squareHeight = battlefield.height / GRID_SIZE;

        try {
            RobocodeFileOutputStream fw = new RobocodeFileOutputStream(
                    this.getDataFile("dataset.csv").getAbsolutePath(), true);
            out.println("Writing to: " + fw.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            moverParaProximaPosicao();
            execute();
        }
    }

    private void moverParaProximaPosicao() {
        double x = battlefield.x + currentCol * squareWidth + squareWidth / 2;
        double y = battlefield.y + currentRow * squareHeight + squareHeight / 2;
        goTo(x, y);

        currentCol++;
        if (currentCol >= GRID_SIZE) {
            currentCol = 0;
            currentRow++;
            if (currentRow >= GRID_SIZE) {
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

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);
        System.out.println("Enemy spotted: "+event.getName());

        Point2D.Double coordinates = getEnemyCoordinates(event.getBearing(), event.getDistance());

        int gridX = (int) ((coordinates.x - battlefield.x) / squareWidth);
        int gridY = (int) ((coordinates.y - battlefield.y) / squareHeight);
        if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
            gridCounts[gridY][gridX] = Math.min(2, gridCounts[gridY][gridX] + 1);
        }

        Bullet b = fireBullet(3);
        if (b != null) {
            balasNoAr.put(b, new Dados(event.getName(), event.getDistance(), event.getVelocity(), event.getEnergy()));
            totalTiros++;
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        balasNoAr.remove(event.getBullet());
        atualizarMatriz();
        guardarMatriz();
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        balasNoAr.remove(event.getBullet());
        atualizarMatriz();
        guardarMatriz();
    }

    private Point2D.Double getEnemyCoordinates(double bearing, double distance) {
        double angle = Math.toRadians((getHeading() + bearing) % 360);
        double enemyX = (getX() + Math.sin(angle) * distance);
        double enemyY = (getY() + Math.cos(angle) * distance);
        return new Point2D.Double(enemyX, enemyY);
    }

    private void atualizarMatriz() {
        // Limpa a matriz antes de atualizar
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                gridCounts[i][j] = 0;
            }
        }

        // Atualiza a posição do robô principal
        int gridX = (int) ((getX() - battlefield.x) / squareWidth);
        int gridY = (int) ((getY() - battlefield.y) / squareHeight);
        if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
            gridCounts[gridY][gridX] = 'X';
        }

        // Atualiza a matriz com as balas
        for (Bullet b : balasNoAr.keySet()) {
            int bulletX = (int) ((b.getX() - battlefield.x) / squareWidth);
            int bulletY = (int) ((b.getY() - battlefield.y) / squareHeight);
            if (bulletX >= 0 && bulletX < GRID_SIZE && bulletY >= 0 && bulletY < GRID_SIZE) {
                gridCounts[bulletY][bulletX] = -1;
            }
        }
    }

    private synchronized void guardarMatriz() {
        if (fw != null) {
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = 0; j < GRID_SIZE; j++) {
                        sb.append(gridCounts[i][j]);
                        if (j < GRID_SIZE - 1) {
                            sb.append(",");
                        }
                    }
                }
                sb.append("\n");

                fw.write(sb.toString().getBytes());
                fw.flush();

                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDeath(DeathEvent event) {
        fecharArquivo();
    }

    @Override
    public void onWin(WinEvent event) {
        fecharArquivo();
    }

    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        fecharArquivo();
    }

    private void fecharArquivo() {
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
