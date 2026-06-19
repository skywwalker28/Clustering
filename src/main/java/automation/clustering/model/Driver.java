package automation.clustering.model;

import lombok.Data;

@Data
public class Driver {
    private int driverIndex;
    private DriverData driverData;


    @Data
    public static class DriverData {
        private String vehicleNumber;
        private String driverName;
        private String phone;
        private String carrier;
        private String tariff;
    }
}
