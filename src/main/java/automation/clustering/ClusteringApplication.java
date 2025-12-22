package automation.clustering;

import automation.clustering.optimization.RouteOptimizationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClusteringApplication implements CommandLineRunner {

    private final RouteOptimizationService optimizationService;

    public ClusteringApplication(RouteOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ClusteringApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        optimizationService.optimizeAndDisplayRoutes();
    }
}
