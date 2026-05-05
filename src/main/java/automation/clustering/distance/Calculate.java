package automation.clustering.distance;

import java.util.List;

public class Calculate {
    public static double[] calculateApproximateDistance(List<double[]> points) {
        double totalDistance = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            double segmentDistance = calculateHaversineDistance(points.get(i), points.get(i + 1));
            totalDistance += segmentDistance;
        }

        return calculateSegmentTime(totalDistance);
    }

    public static double calculateHaversineDistance(double[] point1, double[] point2) {
        double lat1 = Math.toRadians(point1[0]);
        double lat2 = Math.toRadians(point2[0]);
        double lon1 = Math.toRadians(point1[1]);
        double lon2 = Math.toRadians(point2[1]);

        double dLon = lon2 - lon1;
        double dLat = lat2 - lat1;

        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(lat1) * Math.cos(lat2) *
                                Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = 6371000 * c;

        return distance * 1.3;
    }

    static double[] calculateSegmentTime(double segmentDistanceMeters) {
        double[] result = new double[2];

        double averageSpeedKm = 40.0, distanceKm = segmentDistanceMeters / 1000;
        double timeHours = distanceKm / averageSpeedKm, timeMinutes = timeHours * 60;

        result[0] = segmentDistanceMeters;
        result[1] = Math.round(timeMinutes);

        System.out.println(segmentDistanceMeters);
        System.out.println(timeMinutes);

        return result;
    }

    public static String calculateTime(double minutes) {
        if (minutes < 60) return (int) minutes + " мин";
        double diff = minutes / 60.0;

        int hours = (int) diff;
        double residue = diff % 1;

        int residueMin = (int) (residue * 60);

        return hours + "ч " + residueMin + " мин";
    }
}
