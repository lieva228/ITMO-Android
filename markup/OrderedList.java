package markup;

import java.util.List;

public class OrderedList extends AbstractList implements InterfaceForLists {

    public OrderedList(List text) {
        super(text);
        this.htmlSymbolStart = "<ol>";
        this.htmlSymbolEnd = "</ol>";
    }
}