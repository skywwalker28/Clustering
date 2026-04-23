package automation.clustering.model;

public class DeliveryPoint {
    private final String address;
    private final int weightKg;
    private final int number;

    public DeliveryPoint(String address, int weightKg, int number) {
        this.address = address;
        this.weightKg = weightKg;
        this.number = number;
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
}
