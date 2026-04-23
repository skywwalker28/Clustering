package automation.clustering.json;

import java.util.List;

public class CreateStringBuilder {
    private static final int MAX_WEIGHT = 550;
    private static final double BMM_LAT = 55.592605;
    private static final double BMM_LON = 37.747183;

    public static StringBuilder getStringBuilder(List<double[]> coordinates, List<Integer> weights) {
        StringBuilder jobs = new StringBuilder();
        for (int i = 1; i <= coordinates.size(); i++) {
            double[] c = coordinates.get(i);
            int weight = weights.get(i);

            jobs.append("""
                {
                  "id": %d,
                  "location": [%f, %f],
                  "amount": [%d, 1]
                }
                """.formatted(
                    i,
                    c[1], c[0],
                    weight
            ));

            if (i <= coordinates.size() - 1) jobs.append(",");
        }
        return jobs;
    }

    public static StringBuilder getStringBuilderBMM(int neededVehicles) {
        StringBuilder vehicles = new StringBuilder();

        for (int i = 1; i <= neededVehicles; i++) {
            vehicles.append("""
                    {
                      "id": %d,
                      "start": [%f, %f],
                      "capacity": [%d, %d],
                      "profile": "driving-car"
                    }
                    """.formatted(i,
                    BMM_LON, BMM_LAT,
                    MAX_WEIGHT, BuildORS.MAX_POINTS_PER_DRIVER));

            if (i <= neededVehicles - 1) vehicles.append(",");
        }

        return vehicles;
    }
}
