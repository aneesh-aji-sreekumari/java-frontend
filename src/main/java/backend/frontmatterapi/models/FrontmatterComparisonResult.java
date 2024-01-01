package backend.frontmatterapi.models;

import java.util.ArrayList;
import java.util.Optional;

public class FrontmatterComparisonResult {
    private Optional<ArrayList<String>> tocComparisonResult;
    private Optional<ArrayList<String>> lotComparisonResult;
    private Optional<ArrayList<String>> loiComparisonResult;

    public Optional<ArrayList<String>> getTocComparisonResult() {
        return tocComparisonResult;
    }

    public void setTocComparisonResult(Optional<ArrayList<String>> tocComparisonResult) {
        this.tocComparisonResult = tocComparisonResult;
    }

    public Optional<ArrayList<String>> getLotComparisonResult() {
        return lotComparisonResult;
    }

    public void setLotComparisonResult(Optional<ArrayList<String>> lotComparisonResult) {
        this.lotComparisonResult = lotComparisonResult;
    }

    public Optional<ArrayList<String>> getLoiComparisonResult() {
        return loiComparisonResult;
    }

    public void setLoiComparisonResult(Optional<ArrayList<String>> loiComparisonResult) {
        this.loiComparisonResult = loiComparisonResult;
    }
}
