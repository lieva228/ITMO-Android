package markup;

import java.util.List;

public class ListItem{
    protected List<InterfaceForLists> text;
    protected String htmlSymbolStart;
    protected String htmlSymbolEnd;

    public ListItem(List<InterfaceForLists> text) {
        this.text = text;
        htmlSymbolStart = "<li>";
        htmlSymbolEnd = "</li>";
    }

    public void toHtml(StringBuilder ans) {
        ans.append(htmlSymbolStart);
        for (InterfaceForLists abstractMarkup : text) {
            abstractMarkup.toHtml(ans);
        }
        ans.append(htmlSymbolEnd);
    }
}
