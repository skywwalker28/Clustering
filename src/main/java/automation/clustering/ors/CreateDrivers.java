package automation.clustering.ors;

import automation.clustering.model.DeliveryPoint;

import java.util.List;

public class CreateDrivers {
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

    public static StringBuilder getStringBuilderBMM() {
        StringBuilder vehicles = new StringBuilder();
        vehicles.append(ownCreateDrivers(0, 1000, 10));
        vehicles.append(",").append(ownCreateDrivers(1, 1000, 10));
        vehicles.append(",").append(ownCreateDrivers(2, 1000, 10));

        System.out.println(vehicles);
        return vehicles;
    }

    public static String ownCreateDrivers(int id, int maxWeight, int maxPoints) {
        return """
                {
                   "id": %d,
                   "start": [%f, %f],
                   "capacity": [%d, %d],
                   "profile": "driving-car",
                   "time": 32400
                }
                """.formatted(id, BMM_LON, BMM_LAT, maxWeight, maxPoints);
    }
}