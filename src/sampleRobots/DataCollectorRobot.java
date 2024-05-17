package sampleRobots;

import robocode.*;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class DataCollectorRobot extends AdvancedRobot {

    private class BattleData {
        String enemyName;
        double enemyDistance;
        double enemyBearing;
        double myX;
        double myY;
        double enemyX;
        double enemyY;
        double bulletPower;

        public BattleData(String enemyName, double enemyDistance, double enemyBearing,
                double myX, double myY, double enemyX, double enemyY, double bulletPower) {
            this.enemyName = enemyName;
            this.enemyDistance = enemyDistance;
            this.enemyBearing = enemyBearing;
            this.myX = myX;
            this.myY = myY;
            this.enemyX = enemyX;
            this.enemyY = enemyY;
            this.bulletPower = bulletPower;
        }
    }

    RobocodeFileOutputStream fw;
    HashMap<Bullet, BattleData> bulletsInAir = new HashMap<>();

    @Override
    public void run() {
        super.run();

        try {
            fw = new RobocodeFileOutputStream(this.getDataFile("battle_data.csv").getAbsolutePath(), true);
            System.out.println("Writing to: " + fw.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            //lógica de movimento do robô
            setAhead(100);
            setTurnLeft(100);
            Random rand = new Random();
            setAllColors(new Color(rand.nextInt(3), rand.nextInt(3), rand.nextInt(3)));
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        double bulletPower = 3; //potência da bala
        Bullet b = fireBullet(bulletPower);

        if (b != null) {
            double absoluteBearing = getHeadingRadians() + event.getBearingRadians();
            double enemyX = getX() + event.getDistance() * Math.sin(absoluteBearing);
            double enemyY = getY() + event.getDistance() * Math.cos(absoluteBearing);

            BattleData data = new BattleData(event.getName(), event.getDistance(), event.getBearing(),
                    getX(), getY(), enemyX, enemyY, bulletPower);
            bulletsInAir.put(b, data);
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        super.onBulletHit(event);
        BattleData data = bulletsInAir.get(event.getBullet());
        if (data != null) {
            writeData(data, "hit");
            bulletsInAir.remove(event.getBullet());
        }
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);
        BattleData data = bulletsInAir.get(event.getBullet());
        if (data != null) {
            writeData(data, "missed");
            bulletsInAir.remove(event.getBullet());
        }
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        super.onBulletHitBullet(event);
        BattleData data = bulletsInAir.get(event.getBullet());
        if (data != null) {
            writeData(data, "bullet_hit");
            bulletsInAir.remove(event.getBullet());
        }
    }

    @Override
    public void onDeath(DeathEvent event) {
        super.onDeath(event);
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        super.onRoundEnded(event);
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeData(BattleData data, String hitStatus) {
        try {
            String line = String.format("%s,%f,%f,%f,%f,%f,%f,%f,%s\n",
                    data.enemyName, data.enemyDistance, data.enemyBearing,
                    data.myX, data.myY, data.enemyX, data.enemyY, data.bulletPower, hitStatus);
            fw.write(line.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
