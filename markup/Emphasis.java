package markup;

import java.util.List;

public class Emphasis extends AbstractText implements IntefaceforParagraf {

    public Emphasis(List text) {
        super(text);
        symbol = "*";
        htmlSymbolStart = "<em>";
        htmlSymbolEnd = "</em>";
    }
}
