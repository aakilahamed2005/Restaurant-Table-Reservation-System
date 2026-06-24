package com.example.restaurantTableReservation.Restaurant_Mangement;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

@Component
public class MarkupRenderer {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    private final PolicyFactory sanitizerPolicy = new HtmlPolicyBuilder()
            .allowElements("p", "b", "i", "strong", "em", "ul", "ol", "li",
                    "h1", "h2", "h3", "h4", "br", "a", "blockquote", "code", "pre")
            .allowAttributes("href").onElements("a")
            .requireRelNofollowOnLinks()
            .toFactory();

    public String renderToSafeHtml(String rawMarkup) {
        if (rawMarkup == null) return "";
        Node document = parser.parse(rawMarkup);
        String html = renderer.render(document);
        return sanitizerPolicy.sanitize(html);
    }
}