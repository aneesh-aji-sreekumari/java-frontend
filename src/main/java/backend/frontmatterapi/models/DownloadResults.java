package backend.frontmatterapi.models;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.ArrayList;
@Getter
@Setter
public class DownloadResults {
    private ArrayList<String> tocResult;
    private ArrayList<String> loiResult;
    private  ArrayList<String> lotResult;
    private Path path;
}
