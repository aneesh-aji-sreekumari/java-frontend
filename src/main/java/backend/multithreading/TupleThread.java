package backend.multithreading;
import backend.models.Tuple;
import backend.services.FrontMatterComparisonService;
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
        System.out.println("Current Thread:" + Thread.currentThread().getName());
        return frontMatterComparisonService.readPDF(file);
    }
}
