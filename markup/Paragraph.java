package markup;

import java.util.List;

public class Paragraph implements InterfaceForLists, Markup {
    protected List<IntefaceforParagraf> text;
    protected String symbol;
    protected String htmlSymbolStart;
    protected String htmlSymbolEnd;

    public Paragraph(List<IntefaceforParagraf> text) {
        this.text = text;
        symbol = "";
        htmlSymbolStart = "";
        htmlSymbolEnd = "";
    }

    public void toHtml(StringBuilder ans) {
        ans.append(htmlSymbolStart);
        for (Markup abstractMarkup : text) {
            abstractMarkup.toHtml(ans);
        }
        ans.append(htmlSymbolEnd);
    }

    public void toMarkdown(StringBuilder ans) {
        ans.append(symbol);
        for (IntefaceforParagraf abstractMarkup : text) {
            abstractMarkup.toMarkdown(ans);
        }
        ans.append(symbol);
    }
}
