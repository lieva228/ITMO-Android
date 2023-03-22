package markup;

import java.util.List;

public class Strong extends AbstractText implements IntefaceforParagraf {

    public Strong(List text) {
        super(text);
        symbol = "__";
        htmlSymbolStart = "<strong>";
        htmlSymbolEnd = "</strong>";
    }
}
