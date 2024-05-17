package maps;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import interf.IPoint;
import interf.IUIConfiguration;
import viewer.PathViewer;

public class PathDrawingSample {
    public static IUIConfiguration conf;
    public static int bestIntersections = 0;

    public static void main(String args[]) throws InterruptedException, Exception {

        int map_id = 9;

        conf = Maps.getMap(map_id);

        int populationSize = 100;
        int generations = 2000;
        double mutationRate = 0.05;

        List<List<Point>> population = initializePopulation(populationSize, conf);
        List<Point> bestSolution = null;
        double bestFitness = Double.MAX_VALUE;

        for (int generation = 0; generation < generations; generation++) {
            List<Double> fitnessValues = population.stream().map(path -> fitness(path, conf))
                    .collect(Collectors.toList());
            List<List<Point>> selected = select(population, fitnessValues);
            List<List<Point>> offspring = crossover(selected);
            mutate(offspring, mutationRate, conf);

            population = offspring;

            for (int i = 0; i < population.size(); i++) {
                List<Point> currentPath = population.get(i);
                double fitness = fitness(currentPath, conf);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSolution = currentPath;
                    bestIntersections = calculateIntersections(currentPath, conf);
                }
            }

            if (bestFitness == 0) {
                break;
            }
        }

        if (bestSolution != null) {
            System.out.println("Fitness: " + bestFitness);
            System.out.println("Intersections: " + bestIntersections);
            PathViewer pv = new PathViewer(conf);
            pv.setFitness(bestFitness);
            pv.setStringPath(bestSolution.toString());
            List<IPoint> iPointSolution = bestSolution.stream().map(p -> new IPoint() {
                @Override
                public int getX() {
                    return p.x;
                }

                @Override
                public int getY() {
                    return p.y;
                }

                @Override
                public String toString() {
                    return "(" + getX() + ", " + getY() + ")";
                }
            }).collect(Collectors.toList());
            pv.paintPath(iPointSolution);
        } else {
            System.out.println("Nenhuma solução válida encontrada.");
        }
    }

    public static List<List<Point>> initializePopulation(int populationSize, IUIConfiguration conf) {
        List<List<Point>> population = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < populationSize; i++) {
            List<Point> path = new ArrayList<>();

            path.add(new Point(conf.getStart().getX(), conf.getStart().getY()));
            int size = rand.nextInt(5) + 1;
            for (int j = 0; j < size; j++) {
                Point p;
                do {
                    p = new Point(rand.nextInt(conf.getWidth()), rand.nextInt(conf.getHeight()));
                } while (isPointInObstacle(p, conf));
                path.add(p);
            }

            path.add(new Point(conf.getEnd().getX(), conf.getEnd().getY()));
            population.add(path);
        }
        return population;
    }

    public static boolean isPointInObstacle(Point p, IUIConfiguration conf) {
        for (Rectangle obstacle : conf.getObstacles()) {
            if (obstacle.contains(p)) {
                return true;
            }
        }
        return false;
    }

    public static double fitness(List<Point> path, IUIConfiguration conf) {
        double distance = 0;
        int intersections = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);
            distance += p1.distance(p2);

            Line2D.Double line = new Line2D.Double(p1, p2);
            for (Rectangle obstacle : conf.getObstacles()) {
                if (obstacle.intersectsLine(line)) {
                    intersections++;
                }
            }
        }

        return distance + intersections * 10000; 
    }

    public static int calculateIntersections(List<Point> path, IUIConfiguration conf) {
        int intersections = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);

            Line2D.Double line = new Line2D.Double(p1, p2);
            for (Rectangle obstacle : conf.getObstacles()) {
                if (obstacle.intersectsLine(line)) {
                    intersections++;
                }
            }
        }

        return intersections;
    }

    public static List<List<Point>> select(List<List<Point>> population, List<Double> fitnessValues) {
        List<List<Point>> selected = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < population.size(); i++) {
            int idx1 = rand.nextInt(population.size());
            int idx2 = rand.nextInt(population.size());
            if (fitnessValues.get(idx1) < fitnessValues.get(idx2)) {
                selected.add(population.get(idx1));
            } else {
                selected.add(population.get(idx2));
            }
        }
        return selected;
    }

    public static List<List<Point>> crossover(List<List<Point>> selected) {
        List<List<Point>> offspring = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < selected.size(); i += 2) {
            List<Point> parent1 = selected.get(i);
            List<Point> parent2 = selected.get((i + 1) % selected.size());
            int crossoverPoint = rand.nextInt(Math.min(parent1.size(), parent2.size()));

            List<Point> child1 = new ArrayList<>(parent1.subList(0, crossoverPoint));
            child1.addAll(parent2.subList(crossoverPoint, parent2.size()));

            List<Point> child2 = new ArrayList<>(parent2.subList(0, crossoverPoint));
            child2.addAll(parent1.subList(crossoverPoint, parent1.size()));

            offspring.add(child1);
            offspring.add(child2);
        }
        return offspring;
    }

    public static void mutate(List<List<Point>> population, double mutationRate, IUIConfiguration conf) {
        Random rand = new Random();
        for (List<Point> path : population) {
            if (rand.nextDouble() < mutationRate) {
                int mutationPoint = rand.nextInt(path.size() - 2) + 1;
                Point newPoint;
                do {
                    newPoint = new Point(rand.nextInt(conf.getWidth()), rand.nextInt(conf.getHeight()));
                } while (isPointInObstacle(newPoint, conf));
                path.set(mutationPoint, newPoint);
            }
        }
    }
}
