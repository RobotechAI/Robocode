package sampleRobots;

import robocode.*;

import java.awt.geom.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.*;

/**
 * This Robot uses the model provided to guess whether it will hit or miss an enemy.
 * This is a very basic model, trained specifically on the following enemies: Corners, Crazy, SittingDuck, Walls.
 * It is not expected to do great...
 */
public class IntelligentRobot extends AdvancedRobot {


    EasyPredictModelWrapper model;

    @Override
    public void run()
    {
        super.run();

        System.out.println("Reading model from folder: "+getDataDirectory());
        try{
            //load the model
            //TODO: be sure to change the path to the model!
            //you will need to crate the corresponding .data folder in the package of your robot's class, and copy the model there
            model = new EasyPredictModelWrapper(MojoModel.load("/Users/davidecarneiro/Desktop/TP_IA_2024_Resources/bin/sampleRobots/IntelligentRobot.data/drf_hit_1.zip"));
        }
        catch(IOException ex){
            ex.printStackTrace();
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

        RowData row = new RowData();
        row.put("name", event.getName());
        row.put("distance", event.getDistance());
        row.put("velocity", event.getVelocity());

        try {
            BinomialModelPrediction p = model.predictBinomial(row);
            System.out.println("Will I hit? ->" + p.label);

            //if the model predicts I will hit...
            if(p.label.equals("hit"))
                this.fire(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
