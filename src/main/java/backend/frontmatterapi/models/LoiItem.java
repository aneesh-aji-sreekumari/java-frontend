package backend.frontmatterapi.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoiItem implements FmTypeInterface{
    private String figureNumber;
    private String figureTitle;
    private String pageblock;
    private String pageNumber;

}
