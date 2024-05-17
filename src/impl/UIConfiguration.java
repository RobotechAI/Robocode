package impl;

import interf.IPoint;
import interf.IUIConfiguration;

import java.awt.*;
import java.util.List;

/**
 * Class that models the configuration for a given problem
 */
public class UIConfiguration implements IUIConfiguration
{
    private int width;
    private int height;
    private List<Rectangle> obstacles;
    public IPoint start, end;

    public UIConfiguration(int width, int height, List<Rectangle> obstacles) {
        this.width = width;
        this.height = height;
        this.obstacles = obstacles;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<Rectangle> getObstacles() {
        return obstacles;
    }

    public void setObstacles(List<Rectangle> obstacles) {
        this.obstacles = obstacles;
    }

    @Override
    public IPoint getStart() {
        return start;
    }

    @Override
    public void setStart(IPoint iPoint) {
        this.start = iPoint;
    }

    @Override
    public IPoint getEnd() {
        return end;
    }

    @Override
    public void setEnd(IPoint iPoint) {
        this.end = iPoint;
    }


}
