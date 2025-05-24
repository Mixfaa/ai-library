package com.mixfa.ailibrary.route.components;


import com.mixfa.ailibrary.controller.FileStorageContoller;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.Book;
import com.mixfa.ailibrary.model.BookContentProvider;
import com.mixfa.ailibrary.model.content_provider.GoogleBookContentProvider;
import com.mixfa.ailibrary.model.content_provider.PdfFileContentProvider;
import com.mixfa.ailibrary.service.impl.Services;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditBookCompontent extends Dialog {
    private final Services services;
    private final Consumer<Book.AddRequest> handler;

    private final TextField title = new TextField("Title");
    private final TextField description = new TextField("Description");
    private final TextField isbn = new TextField("ISBN");
    private final IntegerField publishYear = new IntegerField("Publish Year");
    private final CustomMultiSelectComboBox<String> subjects = new CustomMultiSelectComboBox<>("Subjects", Function.identity());
    private final CustomMultiSelectComboBox<String> authors = new CustomMultiSelectComboBox<>("Authors", Function.identity());
    private final BookContentProvider[] providers = new BookContentProvider[1];
    private final List<String> images = new ArrayList<>();

    private Dialog makeSelectContentProviderDialog() {
        var dialog = new Dialog("Select content provider");
        dialog.getFooter().add(new CloseDialogButton(dialog));
        dialog.setWidth("1200px");

        var accordion = new Accordion();

        {
            var configurationLayout = new HorizontalLayout();
            configurationLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            var isbnField = new TextField("ISBN");
            isbnField.setPattern("[0-9]*");

            var setButton = new Button("Create", _ -> {
                try {
                    providers[0] = new GoogleBookContentProvider(Long.parseLong(isbnField.getValue()));
                    dialog.close();
                } catch (NumberFormatException e) {
                    Notification.show("Invalid ISBN");
                }
            });
            configurationLayout.add(isbnField, setButton);
            accordion.add("Google content provider", configurationLayout);
        }
        {
            var configurationLayout = new HorizontalLayout();

            var fileBuffer = new FileBuffer();
            var upload = new Upload(fileBuffer);
            upload.setAcceptedFileTypes(".pdf");

            upload.addSucceededListener(e -> {
                var fileUploadService = services.fileStorageService();
                try {
                    var fileData = fileUploadService.write(e.getFileName(), fileBuffer.getInputStream());
                    var url = FileStorageContoller.makeFileStaticURL(fileData);

                    providers[0] = new PdfFileContentProvider(url);
                    Notification.show("Contnet provider created");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Notification.show("Error uploading file");
                }
            });

            configurationLayout.add(new Div("Upload file") {{
                add(upload);
            }});

            accordion.add("Pdf file provider", configurationLayout);
        }

        dialog.add(accordion);
        return dialog;
    }

    public EditBookCompontent(String dialogTitle, Consumer<Book.AddRequest> handler, Services services) {
        super(dialogTitle);

        this.handler = handler;
        this.services = services;
        this.getFooter().add(new CloseDialogButton(this));

        var formLayout = new FormLayout();
        var addImagesDialog = VaadinCommons.editImagesDialog(images, services.fileStorageService());
        var addImagesButton = new OpenDialogButton("edit images", addImagesDialog);

        var contentProviderDialog = makeSelectContentProviderDialog();
        var contentProviderDialogButton = new OpenDialogButton("edit content provider", contentProviderDialog);

        formLayout.add(title, description, isbn, subjects, authors, publishYear, addImagesButton, contentProviderDialogButton, new Button("Submit", _ -> submit()));

        this.add(formLayout);
    }

    public void initForBook(Book book) {
        title.setValue(book.title());
        description.setValue(book.description());
        authors.setValue(book.authors());
        subjects.setValue(book.subjects());
        images.addAll(Arrays.asList(book.images()));
        providers[0] = book.contentProvider();
        isbn.setValue(String.valueOf(book.isbn()));
        publishYear.setValue(book.firstPublishYear());
    }

    private void submit() {
        var requestBuilder = Book.AddRequest.builder();
        requestBuilder.title(title.getValue());
        requestBuilder.description(description.getValue());
        requestBuilder.authors(authors.getSelectedItems().toArray(String[]::new));
        requestBuilder.subjects(subjects.getSelectedItems().toArray(String[]::new));
        requestBuilder.images(images.toArray(String[]::new));
        requestBuilder.firstPublishYear(publishYear.getValue());
        requestBuilder.isbn(Long.parseLong(isbn.getValue()));
        requestBuilder.contentProvider(providers[0]);

        handler.accept(requestBuilder.build());
    }
}
