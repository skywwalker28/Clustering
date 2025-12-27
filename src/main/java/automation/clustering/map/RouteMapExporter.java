package automation.clustering.map;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;

public class RouteMapExporter {

    private static final String[] COLORS = {"red", "blue", "green", "orange", "purple"};
    private static final double[] BMM_POINT = {55.592605, 37.747183};

    public static void exportHtmlMap(
            Map<Integer, List<double[]>> routes,
            Map<Integer, List<String>> addresses,
            String fileName
    ) {
        try (FileWriter writer = new FileWriter(fileName)) {

            writer.write("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8"/>
                        <title>Optimized Routes</title>
                        <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css"/>
                        <script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
                        <style>
                            #map { height: 100vh; }
                    
                            .legend {
                                position: absolute;
                                top: 10px;
                                right: 10px;
                                background: white;
                                padding: 10px 14px;
                                border-radius: 6px;
                                box-shadow: 0 2px 8px rgba(0,0,0,0.25);
                                font-family: Arial, sans-serif;
                                font-size: 14px;
                                line-height: 18px;
                                z-index: 1000;
                            }
                    
                            .legend-item {
                                display: flex;
                                align-items: center;
                                margin-bottom: 6px;
                            }
                    
                            .legend-color {
                                width: 14px;
                                height: 14px;
                                margin-right: 8px;
                                border-radius: 3px;
                            }
                        </style>
                    </head>
                    <body>
                    <div id="map"></div>
                    <div id="legend" class="legend"></div>
                    <script>
                        var map = L.map('map').setView([55.75, 37.61], 9);
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                    """);

            writer.write("""
                    var bmmLatLng = [55.592605, 37.747183];
                    
                    
                    L.circleMarker(bmmLatLng, {
                        radius: 14,
                        color: '#000',
                        weight: 2,
                        fillColor: '#FFD700',
                        fillOpacity: 0.8
                    }).addTo(map);
                    
                    
                    L.circleMarker(bmmLatLng, {
                        radius: 6,
                        color: '#000',
                        weight: 1,
                        fillColor: '#FF4500',
                        fillOpacity: 1
                    }).addTo(map)
                      .bindPopup("<b>BMM</b><br/>Центральный склад / точка старта");
                    """);


            writer.write("""
                    L.tooltip({
                        permanent: true,
                        direction: 'top',
                        className: 'bmm-label'
                    })
                    .setLatLng(bmmLatLng)
                    .setContent('BMM')
                    .addTo(map);
                    """);

            for (Map.Entry<Integer, List<double[]>> entry : routes.entrySet()) {
                int driverId = entry.getKey();
                List<double[]> points = entry.getValue();
                List<String> driverAddresses = addresses.get(driverId);
                String color = COLORS[driverId % COLORS.length];

                writer.write("var latlngs" + driverId + " = [\n");

                writer.write(String.format("  [%f, %f],\n", BMM_POINT[0], BMM_POINT[1]));

                for (double[] p : points) {
                    writer.write(String.format("  [%f, %f],\n", p[0], p[1]));
                }

                writer.write("];\n");

                for (int i = 0; i < points.size(); i++) {
                    double[] p = points.get(i);
                    String address = driverAddresses.get(i).replace("\"", "");

                    writer.write(String.format(
                            "L.marker([%f, %f]).addTo(map)\n" +
                                    " .bindPopup(\"Driver %d<br/>%s\");\n",
                            p[0], p[1], driverId + 1, address
                    ));
                }

                writer.write(String.format(
                        "L.polyline(latlngs%d, {color: '%s'}).addTo(map);\n",
                        driverId, color
                ));

                writer.write(String.format(
                        "document.getElementById('legend').innerHTML += " +
                                "`<div class='legend-item'>" +
                                "<div class='legend-color' style='background:%s'></div>" +
                                "Driver %d</div>`;\n",
                        color,
                        driverId + 1
                ));

            }

            writer.write("""
            </script>
            </body>
            </html>
            """);

            System.out.println("🗺 HTML map created: " + fileName);

        } catch (Exception e) {
            System.err.println("Error creating map: " + e.getMessage());
        }
    }
}
