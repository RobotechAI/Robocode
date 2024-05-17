package interf;

import java.awt.*;
import java.util.List;
/**
 *
 * Class that models the map in which we need to find a solution, including its size and the obstacles.
 */
public interface IUIConfiguration
{
    /**
     * Returns the width of the map.
     * @return the width of the map.
     */
    int getWidth();

    /**
     * Changes the width of the map.
     * @param width the new value
     */
    void setWidth(int width);

    /**
     * Returns the height of the map.
     * @return the height of the map
     */
    int getHeight();

    /**
     * Changes the height of the map.
     * @param height the new value
     */
    void setHeight(int height);

    /**
     * Returns a list of the obstacles that exist in the map. Each obstacle is represented by a {@link Rectangle}.
     * @return a list of the obstacles that exist in the map
     */
    List<Rectangle> getObstacles();

    /**
     * Changes the list of obstacles that exist in the map.
     * @param obstacles the new list of obstacles that exist in the map
     */
    void setObstacles(List<Rectangle> obstacles);
    
    /**
     * Returns the starting point for this specific problem
     * 
     * @return the starting point
     */
    public IPoint getStart();

    /**
     * Changes the starting point for this specific problem
     * 
     * @param start the new starting point
     */
    public void setStart(IPoint start);

    /**
     * Returns the finishing point for this specific problem
     
     * @return the finishing point
     */
    public IPoint getEnd();

    /**
     * Changes the starting point for this specific problem
     * 
     * @param end the new finishing point
     */
    public void setEnd(IPoint end);
}
