package automation.clustering.json;

import automation.clustering.model.DeliveryPoint;

import java.util.List;
import static automation.clustering.drivers.CreateDrivers.ownCreateDrivers;

public class CreateStringBuilder {
    private static final int MAX_WEIGHT = 550;
    public static final double BMM_LAT = 55.592605;
    public static final double BMM_LON = 37.747183;

    public static StringBuilder getStringBuilder(
            List<double[]> coordinates, List<DeliveryPoint> weights) {

        StringBuilder jobs = new StringBuilder();
        for (int i = 0; i < coordinates.size(); i++) {

            double[] c = coordinates.get(i);
            if (c == null) continue;
            int weight = weights.get(i).getWeightKg();

            jobs.append("""
                {
                  "id": %d,
                  "location": [%f, %f],
                  "delivery": [%d, 1]
                }
                """.formatted(
                    i,
                    c[1], c[0],
                    weight
            ));

            if (i < coordinates.size() - 1) jobs.append(",");
        }
        return jobs;
    }

    public static StringBuilder getStringBuilderBMM(int neededVehicles) {
        StringBuilder vehicles = new StringBuilder();

        for (int i = 0; i < neededVehicles; i++) {
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

            if (i < neededVehicles - 1) vehicles.append(",");
        }

//        vehicles.append(ownCreateDrivers(0, 1000, 10, "driving-car"));
//        vehicles.append(",").append(ownCreateDrivers(1, 600, 10, "driving-car"));
//        vehicles.append(",").append(ownCreateDrivers(2, 350, 10, "driving-car"));

        System.out.println(vehicles);
        return vehicles;
    }
}