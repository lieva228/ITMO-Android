package markup;

import java.util.List;

public class UnorderedList extends AbstractList implements InterfaceForLists {

    public UnorderedList(List text) {
        super(text);
        this.htmlSymbolEnd = "</ul>";
        this.htmlSymbolStart = "<ul>";
    }
}
