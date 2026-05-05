package automation.clustering.wrapper;

import java.util.Arrays;

public record CoordinateWrapper(double[] coords) {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CoordinateWrapper && Arrays.equals(coords, ((CoordinateWrapper) obj).coords);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coords);
    }
}
