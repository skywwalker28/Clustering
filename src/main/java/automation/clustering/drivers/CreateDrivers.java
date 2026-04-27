package automation.clustering.drivers;

import static automation.clustering.json.CreateStringBuilder.BMM_LAT;
import static automation.clustering.json.CreateStringBuilder.BMM_LON;

public class CreateDrivers {
    public static String ownCreateDrivers(int id, int maxWeight, int maxPoints, String profile) {
        return """
                {
                   "id": %d,
                   "start": [%f, %f],
                   "capacity": [%d, %d],
                   "profile": "%s"
                }
                """.formatted(id, BMM_LON, BMM_LAT, maxWeight, maxPoints, profile);
    }
}