package markup;

import java.util.List;

abstract class AbstractList {
    protected List<ListItem> text;
    protected String htmlSymbolStart;
    protected String htmlSymbolEnd;

    public AbstractList(List<ListItem> text)   {
        this.text = text;
    }

    public void toHtml(StringBuilder ans) {
        ans.append(htmlSymbolStart);
        for (ListItem listItem : text) {
            listItem.toHtml(ans);
        }
        ans.append(htmlSymbolEnd);
    }
}
