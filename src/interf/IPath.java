package interf;

import java.util.List;

/**
 * Interface that implements a path
 */
public interface IPath {
    public List<IPoint> getPoints();
    public void setPoints(List<IPoint> points);
}
