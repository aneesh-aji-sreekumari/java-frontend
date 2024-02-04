package backend.frontmatterapi.multithreading;
import backend.frontmatterapi.models.Tuple;
import backend.frontmatterapi.services.FrontMatterComparisonService;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class TupleThread implements Callable<Optional<Tuple>> {
    public FrontMatterComparisonService frontMatterComparisonService;
    public ExecutorService es;
    public File file;
    public TupleThread(
            FrontMatterComparisonService frontMatterComparisonService,
            ExecutorService es,
            File file){
        this.es = es;
        this.frontMatterComparisonService = frontMatterComparisonService;
        this.file = file;
    }
    @Override
    public Optional<Tuple> call() throws Exception {
        return frontMatterComparisonService.readPDF(file);
    }
}
