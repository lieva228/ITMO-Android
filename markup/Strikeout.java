package markup;

import java.util.List;

public class Strikeout extends AbstractText implements IntefaceforParagraf {

    public Strikeout(List text) {
        super(text);
        symbol = "~";
        htmlSymbolStart = "<s>";
        htmlSymbolEnd = "</s>";
    }
}
