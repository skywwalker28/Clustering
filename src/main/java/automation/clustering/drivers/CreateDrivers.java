package automation.clustering.drivers;

import static automation.clustering.ors.CreateDrivers.BMM_LAT;
import static automation.clustering.ors.CreateDrivers.BMM_LON;

public class CreateDrivers {
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