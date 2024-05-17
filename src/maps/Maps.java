package maps;

import interf.IUIConfiguration;
import impl.UIConfiguration;
import impl.Point;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Class that models several maps that can be used to benchmark the genetic algorithms
 */
public class Maps
{
    /**
     * Returns configurations (maps and starting and finishing locations) for testing the Genetic Algorithm, in the form of a {@link IUIConfiguration}
     *
     * @param n the number of the configuration to get ([0 .. 10])
     * @return the configuration corresponding to n, in the form of a {@link IUIConfiguration}
     * @throws Exception when n is not in the interval [0 .. 10]
     */
    public static IUIConfiguration getMap(int n) throws Exception
    {
        UIConfiguration conf;
        List<Rectangle> obst = new ArrayList<>();

        switch (n)
        {
            case 0:
                obst.add(new Rectangle(50, 450,500, 50));
                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(500, 550));
                break;
            case 1:
                obst.add(new Rectangle(50, 450,500, 50));
                obst.add(new Rectangle(500, 50,50, 400));
                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(500, 550));
                break;
            case 2:
                obst.add(new Rectangle(0, 450,500, 50));
                obst.add(new Rectangle(450, 50,50, 400));
                obst.add(new Rectangle(500, 50,50, 50));
                obst.add(new Rectangle(550, 150,50, 50));
                //obst.add(new Rectangle(500, 250,50, 50));
                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(500, 550));
                break;
            case 3:
                obst.add(new Rectangle(0, 450,500, 50));
                obst.add(new Rectangle(450, 50,50, 400));
                obst.add(new Rectangle(500, 50,50, 50));
                obst.add(new Rectangle(550, 150,50, 50));
                obst.add(new Rectangle(500, 350,50, 50));
                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(500, 550));
                break;
            case 4:
                obst.add(new Rectangle(0, 300,280, 25));
                obst.add(new Rectangle(320, 300,250, 25));
                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(50, 550));
                break;
            case 5:
                obst.add(new Rectangle(0, 300,290, 25));
                obst.add(new Rectangle(310, 300,250, 25));
                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(50, 550));
                break;
            case 6:
                obst.add(new Rectangle(0, 300,290, 50));
                obst.add(new Rectangle(310, 300,250, 50));
                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(50, 550));
                break;
            case 7:
                Random r = new Random(12345);
                for (int i=0;i<600;i+=2)
                    if(r.nextDouble() > 0.1)
                        obst.add(new Rectangle(i, (int)((Math.sin(Math.toRadians(i)))*100 + 300),4,4));

                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(50, 550));
                break;
            case 8:
                r = new Random(12345);
                for (int i=0;i<600;i+=2)
                    if(r.nextDouble() > 0.1)
                        obst.add(new Rectangle(i, (int)((Math.sin(Math.toRadians(i)))*100 + 300),4,4));

                for (int i=0;i<600;i+=2)
                    if(r.nextDouble() > 0.3)
                        obst.add(new Rectangle(i, (int)((Math.sin(Math.toRadians(i)))*100 + 150),4,4));

                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(50, 550));
                break;
            case 9:
                r = new Random(12345);
                for (int i=0;i<600;i+=2)
                    if(r.nextDouble() > 0.1)
                        obst.add(new Rectangle(i, (int)((Math.sin(Math.toRadians(i)))*100 + 300),4,4));

                for (int i=0;i<600;i+=2)
                    if(r.nextDouble() > 0.1)
                        obst.add(new Rectangle(i, (int)((Math.sin(Math.toRadians(i)))*100 + 150),4,4));

                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(50, 550));
                break;
            case 10:
                r = new Random(12345);
                for (int i=0;i<600;i+=2)
                    if(r.nextDouble() > 0.2)
                        obst.add(new Rectangle(i, (int)((Math.sin(Math.toRadians(i)))*100 + 300),4,4));

                for (int i=0;i<600;i+=2)
                    if(r.nextDouble() > 0.2)
                        obst.add(new Rectangle(i, (int)((Math.sin(Math.toRadians(i)))*100 + 150),4,4));

                for (int i=0;i<600;i+=2)
                    if(r.nextDouble() > 0.2)
                        obst.add(new Rectangle(i, (int)((Math.sin(Math.toRadians(i)))*100 + 450),4,4));

                conf = new UIConfiguration(600, 600, obst);
                conf.setStart(new Point(50, 50));
                conf.setEnd(new Point(300, 550));
                break;
            default: throw new Exception("Argumento inválido");
        }

        return conf;
    }
}
