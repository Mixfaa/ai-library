package com.mixfa.ailibrary.route.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;


public class GoogleBooksViewerComponent extends VerticalLayout {

    public GoogleBooksViewerComponent(long isbn) {
        setSizeFull();  // Take full viewport size
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        setPadding(false);
        setSpacing(false);
        var srcdoc = "<script type=\"text/javascript\" src=\"//books.google.com/books/previewlib.js\"></script>" +
                "<script type=\"text/javascript\">GBS_insertEmbeddedViewer('ISBN:" + isbn + "',1000,800); </script>";

        var iframe = new IFrame("Google Books Viewer");

        iframe.setSrcdoc(srcdoc);
        iframe.setWidth("1050px");
        iframe.setHeight("850px");

        add(iframe);

    }

}

