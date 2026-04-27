package automation.clustering.test;

import java.util.Arrays;

import static automation.clustering.geocoding.BaseCoordinates.getBaseCoordinate;

public class TestClass {
    public static void main(String[] args) {
        double[] coordinate = getBaseCoordinate("Курский Вокзал, Земляной Вал 29, стр 1");
        System.out.println(Arrays.toString(coordinate));
    }
}