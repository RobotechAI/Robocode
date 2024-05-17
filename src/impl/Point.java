package impl;


import interf.IPoint;

import java.awt.geom.Point2D;


/**
 * Class that implements a point
 */
public class Point implements IPoint
{
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Point2D.Double toPoint2D()
    {
        return new Point2D.Double(x, y);
    }


    @Override
    public String toString() {
        return "("+x+", "+y+")";
    }

}
