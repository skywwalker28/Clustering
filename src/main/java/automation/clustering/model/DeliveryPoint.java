package automation.clustering.model;

import lombok.Data;

@Data
public class DeliveryPoint {
    private String address;
    private int weightKg;
    private int number;
    private double lat;
    private double lon;

    public DeliveryPoint(String address, int weightKg, int number) {
        this.address = address;
        this.weightKg = weightKg;
        this.number = number;
        this.lat = 0.0;
        this.lon = 0.0;
    }

    public DeliveryPoint(String address, int weightKg, int number, double lat, double lon) {
        this.address = address;
        this.weightKg = weightKg;
        this.number = number;
        this.lat = lat;
        this.lon = lon;
    }
}
