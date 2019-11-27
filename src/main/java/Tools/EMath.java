package Tools;

/**
 * @author Dakafak
 */
public class EMath {

    public static double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(
                Math.pow(x2 - x1, 2) +
                        Math.pow(y2 - y1, 2)
        );
    }

    public static double getManhattanDistance(double x1, double y1, double x2, double y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    public static int fastfloor(double x) {
        return x > 0 ? (int) x : (int) x - 1;
    }

    public static int fastceil(double x) {
        return x > 0 ? (int) x + 1 : (int) x;
    }

    /**
     * Checks if the first rectangle contains any points of the second rectangle
     *
     * @param x1
     * @param y1
     * @param width1
     * @param height1
     * @param x2
     * @param y2
     * @param width2
     * @param height2
     * @return
     */
    public static boolean intersects(double x1, double y1, double width1, double height1,
                                     double x2, double y2, double width2, double height2) {
        if (contains(x1, y1, width1, height1, x2, y2)) {
            return true;
        }

        if (contains(x1, y1, width1, height1, x2 + width2, y2)) {
            return true;
        }

        if (contains(x1, y1, width1, height1, x2, y2 + height2)) {
            return true;
        }

        if (contains(x1, y1, width1, height1, x2 + width2, y2 + height2)) {
            return true;
        }

        return false;
    }

    /**
     * Checks both rectangle intersects to see if either rectangle intesects with the other
     *         this is needed if the first rectangle in intersects is smaller than the first rectangle
     *
     * @param x1
     * @param y1
     * @param width1
     * @param height1
     * @param x2
     * @param y2
     * @param width2
     * @param height2
     * @return
     */
    public static boolean fullIntersects(double x1, double y1, double width1, double height1,
                                         double x2, double y2, double width2, double height2) {
        if (intersects(x1, y1, width1, height1, x2, y2, width2, height2) || intersects(x2, y2, width2, height2, x1, y1, width1, height1)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a rectangle contains a point
     *
     * @param x1
     * @param y1
     * @param width
     * @param height
     * @param x2
     * @param y2
     * @return
     */
    public static boolean contains(double x1, double y1, double width, double height, double x2, double y2) {
        if (x2 >= x1 && y2 >= y1 && x2 <= x1 + width && y2 <= y1 + height) {
            return true;
        } else {
            return false;
        }
    }

    public static double getAngleDegInt(int m1x, int m1y, int m2x, int m2y) {
        double degrees = Math.toDegrees(Math.atan2(m1y - m2y, m2x - m1x));
        return degrees;
    }

    public static double getAngleRad(double m1x, double m1y, double m2x, double m2y) {
        double degrees = (Math.atan2(m1y - m2y, m2x - m1x));
        return degrees;
    }

}
