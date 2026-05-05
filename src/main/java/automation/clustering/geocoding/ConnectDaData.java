package automation.clustering.geocoding;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ConnectDaData {
    public static final Dotenv dotenv = Dotenv.load();
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final String url = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address";
    public static final String API_KEY_DaData = dotenv.get("API_DADATA_SECRET_KEY");
    public static final String SECRET_KEY_DaData = dotenv.get("API_DADATA_KEY");

    public static String connectionToDaData(String address) throws IOException, InterruptedException {
        if (API_KEY_DaData == null || SECRET_KEY_DaData == null) {
            throw new IllegalStateException("KEY or Secret KEY DaData not found in .env file");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Token " + API_KEY_DaData)
                .header("X-Secret", SECRET_KEY_DaData)
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
