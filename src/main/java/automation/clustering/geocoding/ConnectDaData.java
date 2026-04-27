package automation.clustering.geocoding;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ConnectDaData {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final String url = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address";
    private static final String API_KEY = "b756271d85802e92aa6e6d398a0a5bf72fafcb19";
    private static final String SECRET_KEY = "b2c831847d01d799c6ac14b8143f2efcbff96986";

    public static String connectionToDaData(String address) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Token " + API_KEY)
                .header("X-Secret", SECRET_KEY)
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
