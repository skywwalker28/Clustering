package automation.clustering.drivers;

import static automation.clustering.ors.CreateStringBuilder.BMM_LAT;
import static automation.clustering.ors.CreateStringBuilder.BMM_LON;

public class CreateDrivers {
    public static String ownCreateDrivers(int id, int maxWeight, int maxPoints, String profile) {
        return """
                {
                   "id": %d,
                   "start": [%f, %f],
                   "capacity": [%d, %d],
                   "profile": "%s",
                   "time": 32400
                }
                """.formatted(id, BMM_LON, BMM_LAT, maxWeight, maxPoints, profile);
    }
}