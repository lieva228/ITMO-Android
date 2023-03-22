package markup;

import java.util.List;

public class MarkupMain {
    public static void main(String[] args) {
        StringBuilder f = new StringBuilder();
        Paragraph text = new Paragraph(List.of(new Strong(List.of())));
        text.toHtml(f);
        System.out.println(f);
    }
}
