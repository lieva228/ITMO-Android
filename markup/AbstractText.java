package markup;

import java.util.List;

abstract class AbstractText implements Markup {
    public List<Markup> text;
    protected String symbol;
    protected String htmlSymbolStart;
    protected String htmlSymbolEnd;

    public AbstractText(List text)   {
        this.text = text;
    }

    public void toMarkdown(StringBuilder ans) {
        ans.append(symbol);
        for (Markup markup : text) {
            markup.toMarkdown(ans);
        }
        ans.append(symbol);
    }

    public void toHtml(StringBuilder ans) {
        ans.append(htmlSymbolStart);
        for (Markup markup : text) {
            markup.toHtml(ans);
        }
        ans.append(htmlSymbolEnd);
    }
}
