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
public class PathViewer
{
    private UI ui;
    private double fitness;
    private String stringPath;
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

    /**
     * Paints a path in the map. A path is a {@link List} of {@link IPoint}
     * @param path the list of points to paint in the map
     */
    public void paintPath(List<IPoint> path){
        ui.paintPath(path);
    }

    /**
     * Updates the value of fitness to be shown in the map.
     * @param fitness the value of fitness to be shown in the map
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * Updates the string that describes the path being drawn in the map
     * @param stringPath the string that desceibes thepath being drawn
     */
    public void setStringPath(String stringPath) {
        this.stringPath = stringPath;
    }

    private class UI extends JPanel
    {
        private final Color COR_CAMINHO = Color.GRAY;
        private final Color COR_LETRAS = Color.BLACK;
        private final Color COR_PROCESSAMENTO = Color.green;
        private final Color COR_SILO = Color.BLACK;
        private int largura, altura;
        private List<IPoint> path;
        private List<Rectangle> obstacles;
        private int iteration = 0;

        /**
         * Creates new form FormigueiroGUI
         */
        private UI(int largura, int altura, List<Rectangle> obstacles)
        {
            initComponents();
            this.largura = largura;
            this.altura = altura;
            this.path = new ArrayList<>();
            this.obstacles = obstacles;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);

                    System.out.println("("+e.getX()+","+e.getY()+")");
                }
            });
        }

        private void paintPath(List<IPoint> caminho) {
            path = caminho;
            iteration++;
            this.repaint();
        }

        private void clearPath() {
            this.path = new ArrayList<>();
            this.repaint();
        }




        @Override
        public void paintComponent(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, largura, altura);

            for (int i=1;i<path.size();i++)
            {
                drawThickLine(g, path.get(i-1).getX(), path.get(i-1).getY(), path.get(i).getX(), path.get(i).getY(), 2, COR_CAMINHO);
            }

            g.setColor(Color.red);
            obstacles.stream().forEach(x -> g.fillRect((int)x.getX(), (int)x.getY(), (int)x.getWidth(), (int)x.getHeight()));

            g.setColor(Color.green);
            g.drawString("START", conf.getStart().getX(), conf.getStart().getY());
            g.drawString("END", conf.getEnd().getX(), conf.getEnd().getY());

            g.setColor(Color.black);
            g.drawString("Generation "+iteration+" Best Solution: "+stringPath+" ("+fitness+")", 20,20);
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

            int xPoints[] = new int[4];
            int yPoints[] = new int[4];

            xPoints[0] = x1 + dx;
            yPoints[0] = y1 + dy;
            xPoints[1] = x1 - dx;
            yPoints[1] = y1 - dy;
            xPoints[2] = x2 - dx;
            yPoints[2] = y2 - dy;
            xPoints[3] = x2 + dx;
            yPoints[3] = y2 + dy;

            g.fillPolygon(xPoints, yPoints, 4);
        }

        private void initComponents()
        {
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
