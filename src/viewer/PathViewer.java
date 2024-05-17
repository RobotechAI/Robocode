package viewer;

import interf.IPoint;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import interf.IUIConfiguration;


/**
 * Class that shows a path in a GUI
 */
public class PathViewer {
    private UI ui;
    private double fitness;
    private String stringPath;
    private int generation;
    private IUIConfiguration conf;

    public PathViewer(IUIConfiguration conf) {
        ui = new UI(conf.getWidth(), conf.getHeight(), conf.getObstacles());
        JFrame frame = new JFrame("PathViewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(ui);
        frame.setSize(conf.getWidth(), conf.getHeight());
        frame.setVisible(true);
        this.conf = conf;
    }

    public void paintPath(List<IPoint> path) {
        ui.paintPath(path);
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public void setStringPath(String stringPath) {
        this.stringPath = stringPath;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    private class UI extends JPanel {
        private final Color COR_CAMINHO = Color.GRAY;
        private final Color COR_LETRAS = Color.BLACK;
        private int largura, altura;
        private List<IPoint> path;
        private List<Rectangle> obstacles;

        private UI(int largura, int altura, List<Rectangle> obstacles) {
            initComponents();
            this.largura = largura;
            this.altura = altura;
            this.path = new ArrayList<>();
            this.obstacles = obstacles;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    System.out.println("(" + e.getX() + "," + e.getY() + ")");
                }
            });
        }

        private void paintPath(List<IPoint> caminho) {
            path = caminho;
            this.repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.white);
            g.fillRect(0, 0, largura, altura);

            for (int i = 1; i < path.size(); i++) {
                drawThickLine(g, path.get(i - 1).getX(), path.get(i - 1).getY(), path.get(i).getX(), path.get(i).getY(), 2, COR_CAMINHO);
            }

            g.setColor(Color.red);
            obstacles.forEach(x -> g.fillRect((int) x.getX(), (int) x.getY(), (int) x.getWidth(), (int) x.getHeight()));

            g.setColor(Color.green);
            g.drawString("START", conf.getStart().getX(), conf.getStart().getY());
            g.drawString("END", conf.getEnd().getX(), conf.getEnd().getY());

            g.setColor(COR_LETRAS);
            g.drawString("Generation: " + generation + " Best Solution: " + stringPath + " (Fitness: " + fitness + ")", 20, 20);
        }

        private void drawThickLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, Color c) {
            g.setColor(c);
            int dX = x2 - x1;
            int dY = y2 - y1;
            double lineLength = Math.sqrt(dX * dX + dY * dY);
            double scale = (double) (thickness) / (2 * lineLength);
            double ddx = -scale * (double) dY;
            double ddy = scale * (double) dX;
            ddx += (ddx > 0) ? 0.5 : -0.5;
            ddy += (ddy > 0) ? 0.5 : -0.5;
            int dx = (int) ddx;
            int dy = (int) ddy;

            int xPoints[] = {x1 + dx, x1 - dx, x2 - dx, x2 + dx};
            int yPoints[] = {y1 + dy, y1 - dy, y2 - dy, y2 + dy};
            g.fillPolygon(xPoints, yPoints, 4);
        }

        private void initComponents() {
            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGap(0, 400, Short.MAX_VALUE)
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGap(0, 300, Short.MAX_VALUE)
            );
        }
    }
}
