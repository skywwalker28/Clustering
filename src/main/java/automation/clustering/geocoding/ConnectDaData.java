package automation.clustering.geocoding;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static automation.clustering.main.RouteOptimizationStart.dotenv;

public class ConnectDaData {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final String url = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address";

    public static String connectionToDaData(String address) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Token " + dotenv.get("API_KEY_DaData"))
                .header("X-Secret", dotenv.get("SECRET_KEY_DaData"))
                .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"query\": \"%s\", \"count\": 3}", address)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println(response.statusCode());
            System.err.println(response.body());
            throw new IOException("Geocoding fail. Something to connect DaData...");
        }

        return response.body();
    }
}
