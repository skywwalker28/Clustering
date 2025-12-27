package automation.clustering.model;

public class DeliveryPoint {
    private final String address;
    private final int weightKg;

    public DeliveryPoint(String address, int weightKg) {
        this.address = address;
        this.weightKg = weightKg;
    }

    public String getAddress() {
        return address;
    }

    public int getWeightKg() {
        return weightKg;
    }
}
