package backend.frontmatterapi.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LotItem  implements FmTypeInterface{
    private String tableNumber;
    private String tableTitle;
    private String pageblock;
    private String pageNumber;

}
