package markup;


public class Text implements Markup, IntefaceforParagraf {
    private String s;

    public Text(String s) {
        this.s = s;
    }

    @Override
    public void toMarkdown(StringBuilder ans) {
        ans.append(s);
    }

    @Override
    public void toHtml(StringBuilder ans) {
        ans.append(s);
    }
}
