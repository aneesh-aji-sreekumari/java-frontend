package backend.frontmatterapi.models;
public class OutputStringDto {
    private String filename;
    private String output;
    public OutputStringDto(String filename, String output){
        this.output = output;
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
