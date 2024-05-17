package utils;

import robocode.AdvancedRobot;
import robocode.Robot;

import java.awt.geom.*;
import java.util.GregorianCalendar;


/**
 * Classe que implementa algumas tarefas frequentes em Robocode
 *
 * @author Davide Carneiro
 * @version 1.0
 */
public final class Utils
{

    private Utils(){}

    /**
     * Devolve a distância às coordenadas dadas
     *
     * @param robot o meu robot
     * @param x coordenada x do alvo
     * @param y coordenada y do alvo
     * @return distância entre o robot e as coordenadas
     * */
    public static double getDistance(Robot robot, double x, double y)
    {
        x -= robot.getX();
        y -= robot.getY();

        return Math.hypot(x, y);
    }

    /**
     * Devolve as coordenadas de um alvo
     *
     * @param robot o meu robot
     * @param bearing ângulo para o alvo, em graus
     * @param distance distância ao alvo
     * @return coordenadas do alvo
     * */
    public static Point2D.Double getEnemyCoordinates(Robot robot, double bearing, double distance){
        double angle = Math.toRadians((robot.getHeading() + bearing) % 360);

        return new Point2D.Double((robot.getX() + Math.sin(angle) * distance), (robot.getY() + Math.cos(angle) * distance));
    }

    /**
     * Foge em linha reta de um determinado alvo
     *
     * @param robot o meu robot
     * @param x coordenada x do alvo
     * @param y coordenada y do alvo
     * @param distance distância que pretende fugir
     * */
    public static void runLineFrom(Robot robot, double x, double y, double distance)
    {
        x -= robot.getX();
        y -= robot.getY();

        double angleToTarget = Math.atan2(x, y);
        double targetAngle = robocode.util.Utils.normalRelativeAngle(angleToTarget - Math.toRadians(robot.getHeading()));
        double turnAngle = Math.atan(Math.tan(targetAngle));
        robot.turnRight(Math.toDegrees(turnAngle));

        if (targetAngle == turnAngle)
            robot.back(distance);
        else
            robot.ahead(distance);
    }

    /**
     * Dirige o robot (RobotSimples) para determinadas coordenadas
     *
     * @param robot o meu robot
     * @param x coordenada x do alvo
     * @param y coordenada y do alvo
     * */
    public static void robotGoTo(Robot robot, double x, double y)
    {
        x -= robot.getX();
        y -= robot.getY();

        double angleToTarget = Math.atan2(x, y);
        double targetAngle = robocode.util.Utils.normalRelativeAngle(angleToTarget - Math.toRadians(robot.getHeading()));
        double distance = Math.hypot(x, y);
        double turnAngle = Math.atan(Math.tan(targetAngle));
        robot.turnRight(Math.toDegrees(turnAngle));
        if (targetAngle == turnAngle)
            robot.ahead(distance);
        else
            robot.back(distance);
    }

    /**
     * Dirige o robot (AdvancedRobot) para determinadas coordenadas
     *
     * @param robot o meu robot
     * @param x coordenada x do alvo
     * @param y coordenada y do alvo
     * */
    public static void advancedRobotGoTo(AdvancedRobot robot, double x, double y)
    {
        x -= robot.getX();
        y -= robot.getY();

        double angleToTarget = Math.atan2(x, y);
        double targetAngle = robocode.util.Utils.normalRelativeAngle(angleToTarget - Math.toRadians(robot.getHeading()));
        double distance = Math.hypot(x, y);
        double turnAngle = Math.atan(Math.tan(targetAngle));
        robot.setTurnRight(Math.toDegrees(turnAngle));
        if (targetAngle == turnAngle)
            robot.setAhead(distance);
        else
            robot.setBack(distance);
        robot.execute();
    }

    /**
     * Devolve a data atual no formato yyyy-MM-dd HH:mm:ss
     *
     * @return a data atual, no formato yyyy-MM-dd HH:mm:ss
     */
    public static String dataAtualFormatada()
    {
        GregorianCalendar gc = new GregorianCalendar();
        String mes = gc.get(GregorianCalendar.MONTH) < 10 ? "0"+gc.get(GregorianCalendar.MONTH) : ""+gc.get(GregorianCalendar.MONTH);
        String dia = gc.get(GregorianCalendar.DAY_OF_MONTH) < 10 ? "0"+gc.get(GregorianCalendar.DAY_OF_MONTH) : ""+gc.get(GregorianCalendar.DAY_OF_MONTH);
        String hora = gc.get(GregorianCalendar.HOUR_OF_DAY) < 10 ? "0"+gc.get(GregorianCalendar.HOUR_OF_DAY) : ""+gc.get(GregorianCalendar.HOUR_OF_DAY);
        String minuto = gc.get(GregorianCalendar.MINUTE) < 10 ? "0"+gc.get(GregorianCalendar.MINUTE) : ""+gc.get(GregorianCalendar.MINUTE);
        String segundo = gc.get(GregorianCalendar.SECOND) < 10 ? "0"+gc.get(GregorianCalendar.SECOND) : ""+gc.get(GregorianCalendar.SECOND);

        return gc.get(GregorianCalendar.YEAR)+"-"+mes+"-"+dia+" "+hora+":"+minuto+":"+segundo;
    }
}
