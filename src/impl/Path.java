package impl;


import interf.IPath;
import interf.IPoint;

import java.util.List;

/**
 * Class that models a path (a list of points)
 */
public class Path implements IPath
{
    private List<IPoint> points;



    @Override
    public String toString() {
        return points.stream().map(x -> x.toString()).reduce((s, s2) -> s + ", "+s2).get();
    }

    public List<IPoint> getPoints() {
        return points;
    }

    public void setPoints(List<IPoint> points) {
        this.points = points;
    }
}
