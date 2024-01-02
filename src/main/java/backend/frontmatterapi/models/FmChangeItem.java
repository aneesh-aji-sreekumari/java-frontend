package backend.frontmatterapi.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FmChangeItem {
    String pageblock;
    ChangeType changeType;
    FrontMatterType frontMatterType;
    SubSubTopic subSubTopic;
    SubTopic subTopic;
    LotItem lotItem;
    LoiItem loiItem;
}
