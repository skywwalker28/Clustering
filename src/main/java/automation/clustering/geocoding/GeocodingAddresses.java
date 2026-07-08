package automation.clustering.geocoding;

import automation.clustering.model.DeliveryPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static automation.clustering.addresses.BaseCoordinates.getBaseCoordinate;
import static automation.clustering.geocoding.ConnectDaData.connectionToDaData;
import static automation.clustering.geocoding.GetLatLon.parseJsonAndGetLatLon;
import static automation.clustering.geocoding.CleanAddress.cleaningAddress;

public class GeocodingAddresses {

    public static List<double[]> getCoordinates(List<DeliveryPoint> allAddresses) {
        List<double[]> allCoordinates = new ArrayList<>();

        for (DeliveryPoint current : allAddresses) {
            System.out.println("_____ Адрес: (" + current.getNumber() + ") " + current.getAddress() + " ______");

            double[] coordinate = getBaseCoordinate(current.getAddress());
            if (coordinate == null) {
                try {
                    System.out.println("Попытка геокодирования №1");
                    coordinate = parseJsonAndGetLatLon(connectionToDaData(current.getAddress()));
                } catch (Exception e) {
                    System.out.println("не найден...");
                }
            }

            if (coordinate == null ) {
                try {
                    System.out.println("Попытка геокодирования №2\n");
                    String cleanedAddress = cleaningAddress(current.getAddress());
                    coordinate = parseJsonAndGetLatLon(connectionToDaData(cleanedAddress));
                } catch (Exception e) {
                    System.out.println("не найден...");
                }
            }


            if (coordinate != null) {
                System.out.println("Адресс найден: " + Arrays.toString(coordinate) + "\n");
                current.setLat(coordinate[0]);
                current.setLon(coordinate[1]);
                allCoordinates.add(coordinate);
            } else {
                System.err.println("\u001B[31mТОЧКА НЕ НАЙДЕНА: (" + current.getNumber()
                        + ") " + current.getAddress() + "\u001B[0m");
            }
        }

        System.out.println();
        allAddresses.removeIf(point -> point.getLon() == 0.0);
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
