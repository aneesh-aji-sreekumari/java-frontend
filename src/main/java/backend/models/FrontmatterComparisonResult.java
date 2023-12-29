package backend.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Optional;

@Getter
@Setter
public class FrontmatterComparisonResult {
    private Optional<ArrayList<String>> tocComparisonResult;
    private Optional<ArrayList<String>> lotComparisonResult;
    private Optional<ArrayList<String>> loiComparisonResult;
}
