package automation.clustering.geocoding;

import automation.clustering.model.DeliveryPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static automation.clustering.geocoding.ConnectDaData.connectionToDaData;
import static automation.clustering.geocoding.GetLatLon.parseJsonAndGetLatLon;
import static automation.clustering.addresses.BaseCoordinates.getBaseCoordinate;
import static automation.clustering.optimization.CleanAddress.cleanAddress;

public class GeocodingAddresses {

    public static List<double[]> getCoordinates(List<DeliveryPoint> allAddresses) throws Exception {
        List<double[]> allCoordinates = new ArrayList<>();

        for (DeliveryPoint current : allAddresses) {
            System.out.println(
                    "Адрес: " + "\u001B[32m(" + current.getNumber() + ") " + current.getAddress() + "\u001B[0m");

            double[] coordinate = getBaseCoordinate(current.getAddress());
            if (coordinate == null) {
                System.out.println("Адрес не найден! Надо геокодировать...");
                String address = current.getAddress();
                coordinate = parseJsonAndGetLatLon(connectionToDaData(cleanAddress(address)));
            }
            allCoordinates.add(coordinate);

            if (coordinate != null) {
                current.setLat(coordinate[0]);
                current.setLon(coordinate[1]);
            }

            if (coordinate != null) System.out.println("координаты: " + Arrays.toString(coordinate) + "\n");
            else System.err.println("Не найден адресс" + "\n");
        }

        return allCoordinates;
    }

    public static String geoResult(int geo) {
        String result;
        switch(geo) {
            case 0 -> result = "Точное попадание координат";
            case 1 -> result = "Неточность ≈ 50 метров";
            case 2 -> result = "Неточность ≈ 100 метров";
            default -> result = "\u001B[33m" + "Низкая точнсть координат!" + "\u001B[0m";
        }

        return result;
    }
}
