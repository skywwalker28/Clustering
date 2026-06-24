package automation.clustering.orsTest;

import automation.clustering.ors.BuildORS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestOrsRequest {

    private HttpClient mockHttpClient;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(HttpClient.class);
        BuildORS.setHttpClient(mockHttpClient);
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        BuildORS.setHttpClient(HttpClient.newHttpClient());
    }

    @Test
    void testSuccessfulRequest() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"success}\"");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String testJson = "{}";
        String result = BuildORS.sendORSRequest(testJson);

        assertEquals("{\"success}\"", result);
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testTimeoutWorksCorrectly() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new HttpConnectTimeoutException("15 секунд не можем достучатся до ors сервиса"));

        String testJson = "{}";
        String result = BuildORS.sendORSRequest(testJson);

        assertNull(result);
        assertTrue(outContent.toString().contains("15 секунд не можем достучатся до ors сервиса"));

        verify(mockHttpClient, times(1)).send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class));
    }
}
