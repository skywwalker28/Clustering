package automation.clustering.ors;

import automation.clustering.model.DeliveryPoint;

import java.util.List;
import static automation.clustering.drivers.CreateDrivers.ownCreateDrivers;

public class CreateDrivers {
    private static final int MAX_WEIGHT = 550;
    public static final double BMM_LAT = 55.592605;
    public static final double BMM_LON = 37.747183;

    public static StringBuilder getStringBuilder(List<double[]> coordinates, List<DeliveryPoint> weights) {

        StringBuilder jobs = new StringBuilder();
        for (int i = 0; i < coordinates.size(); i++) {

            if (coordinates.get(i) == null) continue;
            int weight = weights.get(i).getWeightKg();

            jobs.append("""
                {
                  "id": %d,
                  "location": [%f, %f],
                  "amount": [%d, 1],
                  "service": 900
                }
                """.formatted(i, coordinates.get(i)[1], coordinates.get(i)[0], weight));

            if (i < coordinates.size() - 1) jobs.append(",");
        }
        return jobs;
    }

    public static StringBuilder getStringBuilderBMM(int neededVehicles) {
        StringBuilder vehicles = new StringBuilder();

//        for (int i = 0; i < neededVehicles; i++) {
//            vehicles.append("""
//                    {
//                      "id": %d,
//                      "start": [%f, %f],
//                      "capacity": [%d, %d],
//                      "profile": "driving-car"
//                    }
//                    """.formatted(i, BMM_LON, BMM_LAT, MAX_WEIGHT, BuildORS.MAX_POINTS_PER_DRIVER));
//
//            if (i < neededVehicles - 1) vehicles.append(",");
//        }

        vehicles.append(ownCreateDrivers(0, 1000, 10));
        vehicles.append(",").append(ownCreateDrivers(1, 1000, 10));
        vehicles.append(",").append(ownCreateDrivers(2, 1000, 10));

        System.out.println(vehicles);
        return vehicles;
    }

}