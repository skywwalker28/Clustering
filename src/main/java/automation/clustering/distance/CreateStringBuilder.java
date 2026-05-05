package automation.clustering.distance;

import java.util.List;

public class CreateStringBuilder {

    public static String createJsonBuilder(List<double[]> points) {
        StringBuilder result = new StringBuilder("{\"coordinates\":[");
        for (int i = 0; i < points.size(); i++) {
            result.append("[").append(points.get(i)[1]).append(",").append(points.get(i)[0]).append("]");

            if (i < points.size() - 1) result.append(",");
        }

        return result.append("],\"instructions\":\"false\"}").toString();
    }
}
