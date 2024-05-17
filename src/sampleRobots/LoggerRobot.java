package sampleRobots;

import robocode.*;

import java.awt.geom.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class LoggerRobot extends AdvancedRobot {

    private class Dados{
        String nome;
        Double distancia;

        public Dados(String nome, Double distancia) {
            this.nome = nome;
            this.distancia = distancia;
        }
    }

    RobocodeFileOutputStream fw;

    HashMap<Bullet, Dados> balasNoAr = new HashMap<>();

    @Override
    public void run()
    {
        super.run();

        try {
            fw = new RobocodeFileOutputStream(this.getDataFile("log_robocode.txt").getAbsolutePath(), true);
            System.out.println("Writing to: "+fw.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
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

        Point2D.Double coordinates = utils.Utils.getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        System.out.println("Enemy "+event.getName()+" spotted at "+coordinates.x+","+coordinates.y+"\n");
        Bullet b = fireBullet(3);

        if (b!=null){
            System.out.println("Firing at "+event.getName());
            balasNoAr.put(b, new Dados(event.getName(), event.getDistance()));
        }
        else
            System.out.println("Cannot fire right now..."); 

    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        super.onBulletHit(event);
        Dados d = balasNoAr.get(event.getBullet());
        try
        {
            //testar se acertei em quem era suposto
            if (event.getName().equals(event.getBullet().getVictim()))
                fw.write((d.nome+","+d.distancia+",hit\n").getBytes());
            else
                fw.write((d.nome+","+d.distancia+",no_hit\n").getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);
        Dados d = balasNoAr.get(event.getBullet());
        try {
            fw.write((d.nome+","+d.distancia+",no_hit\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        super.onBulletHitBullet(event);
        Dados d = balasNoAr.get(event.getBullet());
        try {
            fw.write((d.nome+","+d.distancia+",no_hit\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
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

    

    
}
