package automation.clustering.model;

public class DeliveryPoint {
    private final String address;
    private final int weightKg;
    private final int number;
    private double lat;
    private double lon;

    public DeliveryPoint(String address, int weightKg, int number) {
        this.address = address;
        this.weightKg = weightKg;
        this.number = number;
        this.lat = 0.0;
        this.lon = 0.0;
    }

    public String getAddress() {
        return address;
    }
    public int getWeightKg() {
        return weightKg;
    }
    public int getNumber() {
        return number;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
