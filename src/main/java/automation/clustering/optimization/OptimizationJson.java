package automation.clustering.optimization;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OptimizationJson {

    public static final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjRlOD" +
            "I1N2ZkOGU1YzRmZjdiMjgxNTJhYWViZjFkZDY2IiwiaCI6Im11cm11cjY0In0=";


    public static String buildOptimizationJson(double[][] points, int driverCount) {
        StringBuilder json = new StringBuilder();
        json.append("{\"jobs\":[");

        for (int i = 0; i < points.length; i++) {
            json.append(String.format("{\"id\":%d,\"location\":[%.6f,%.6f],\"amount\":[1]}",
                    i, points[i][1], points[i][0]));
            if (i < points.length - 1) json.append(",");
        }

        json.append("],\"vehicles\":[");

        double[] baseLocation = points[0];
        int capacity = (points.length / driverCount) + 2;

        for (int d = 0; d < driverCount; d++) {

            json.append(String.format("{\"id\":%d,\"profile\":\"driving-car\"," +
                            "\"start\":[%.6f,%.6f],\"end\":[%.6f,%.6f]," +
                            "\"capacity\":[%d],\"skills\":[1],\"time_window\":[0,28800]}",
                    d,
                    baseLocation[1], baseLocation[0],  // start lon,lat
                    baseLocation[1], baseLocation[0],      // end lon,lat
                    capacity));
            if (d < driverCount - 1) json.append(",");
        }

        json.append("]}");

        return json.toString();
    }


    public static String sendPostRequest(String urlString, String json) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", API_KEY);
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setConnectTimeout(5000);
        con.setReadTimeout(30000);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        System.out.println("Http code: " + responseCode);

        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return response.toString();
        } else {
            // Читаем ошибку
            BufferedReader err = new BufferedReader(
                    new InputStreamReader(con.getErrorStream(), "utf-8"));
            StringBuilder error = new StringBuilder();
            String line;

            while ((line = err.readLine()) != null) {
                error.append(line);
            }
            err.close();

            throw new Exception("API Error " + responseCode + ": " + error.toString());
        }
    }
}
