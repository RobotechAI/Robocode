package sampleRobots;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

public class WriterRobot extends AdvancedRobot {

    /**
     * Classe usada para guardar os dados dos robots inimigos, quando observados
     */
    private class Dados {
        String nome;
        Double distancia;
        Double velocidade;

        public Dados(String nome, Double distancia, Double velocidade) {
            this.nome = nome;
            this.distancia = distancia;
            this.velocidade = velocidade;
        }
    }

    RobocodeFileOutputStream fw;

    //estrutura para manter a informação das balas enquanto não atingem um alvo, a parede ou outra bala 
    //isto porque enquanto a bala não desaparece, não sabemos se atingiu o alvo ou não
    HashMap<Bullet, Dados> balasNoAr = new HashMap<>();

    private int totalTiros = 0;
    private int tirosAcertados = 0;

    private Rectangle2D.Double battlefield;
    private static final int GRID_SIZE = 8;
    private double squareWidth;
    private double squareHeight;

    private int currentRow = 0;
    private int currentCol = 0;

    @Override
    public void run() {

        battlefield = new Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
        squareWidth = battlefield.width / GRID_SIZE;
        squareHeight = battlefield.height / GRID_SIZE;

        try {
            fw = new RobocodeFileOutputStream(this.getDataFile("log_robocode.txt").getAbsolutePath(), true);
            System.out.println("Writing to: " + fw.getName());
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
        Point2D.Double coordinates = utils.Utils.getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        Bullet b = fireBullet(3);

        if (b != null) {
            balasNoAr.put(b, new Dados(event.getName(), event.getDistance(), event.getVelocity()));
            totalTiros++;
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        Dados d = balasNoAr.get(event.getBullet());
        try {
            if (event.getName().equals(event.getBullet().getVictim())) {
                tirosAcertados++;
                fw.write((d.nome + "," + d.distancia + "," + d.velocidade + ",hit\n").getBytes());
            } else {
                fw.write((d.nome + "," + d.distancia + "," + d.velocidade + ",no_hit\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        Dados d = balasNoAr.get(event.getBullet());
        try {
            fw.write((d.nome + "," + d.distancia + "," + d.velocidade + ",no_hit\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        Dados d = balasNoAr.get(event.getBullet());
        try {
            fw.write((d.nome + "," + d.distancia + "," + d.velocidade + ",no_hit\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    public void onDeath(DeathEvent event) {
        finalizarEscrita();
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        finalizarEscrita();
    }

    private void finalizarEscrita() {
        try {
            fw.write(("Total tiros: " + totalTiros + ", Tiros acertados: " + tirosAcertados + "\n").getBytes());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}