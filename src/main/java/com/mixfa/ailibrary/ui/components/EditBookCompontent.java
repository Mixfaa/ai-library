package com.mixfa.ailibrary.ui.components;


import com.mixfa.ailibrary.controller.FileStorageContoller;
import com.mixfa.ailibrary.misc.VaadinCommons;
import com.mixfa.ailibrary.model.library.Book;
import com.mixfa.ailibrary.model.library.BookContentProvider;
import com.mixfa.ailibrary.model.library.content_provider.GoogleBookContentProvider;
import com.mixfa.ailibrary.model.library.content_provider.PdfFileContentProvider;
import com.mixfa.ailibrary.service.misc.impl.Services;
import com.mixfa.ailibrary.ui.localization.Localizer;
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

    private final Localizer localizer;

    private final TextField title;
    private final TextField description;
    private final TextField isbn;
    private final IntegerField publishYear;
    private final CustomMultiSelectComboBox<String> subjects;
    private final CustomMultiSelectComboBox<String> authors;
    private final BookContentProvider[] providers = new BookContentProvider[1];
    private final List<String> images = new ArrayList<>();

    private Dialog makeSelectContentProviderDialog() {
        var dialog = new Dialog(localizer.get("editbook.selectcontentprovider"));
        dialog.getFooter().add(new CloseDialogButton(dialog, localizer));
        dialog.setWidth("1200px");

        var accordion = new Accordion();

        {
            var configurationLayout = new HorizontalLayout();
            configurationLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            var isbnField = new TextField(localizer.get("editbook.isbn"));
            isbnField.setPattern("[0-9]*");

            var setButton = new Button(localizer.get("editbook.create"), _ -> {
                try {
                    providers[0] = new GoogleBookContentProvider(Long.parseLong(isbnField.getValue()));
                    dialog.close();
                } catch (NumberFormatException e) {
                    Notification.show(localizer.get("editbook.invalidisbn"));
                }
            });
            configurationLayout.add(isbnField, setButton);
            accordion.add(localizer.get("editbook.googleprovider"), configurationLayout);
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
                    Notification.show(localizer.get("editbook.providercreated"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Notification.show(localizer.get("editbook.uploaderror"));
                }
            });

            configurationLayout.add(new Div(localizer.get("editbook.uploadfile")) {{
                add(upload);
            }});

            accordion.add(localizer.get("editbook.pdfprovider"), configurationLayout);
        }

        dialog.add(accordion);
        return dialog;
    }

    public EditBookCompontent(String dialogTitle, Consumer<Book.AddRequest> handler, Localizer localizer, Services services) {
        super(dialogTitle);

        this.title = new TextField(localizer.get("editbook.title"));
        this.description = new TextField(localizer.get("editbook.description"));
        this.isbn = new TextField(localizer.get("editbook.isbn"));
        this.publishYear = new IntegerField(localizer.get("editbook.publishyear"));
        this.subjects = new CustomMultiSelectComboBox<>(localizer.get("editbook.subjects"), Function.identity());
        this.authors = new CustomMultiSelectComboBox<>(localizer.get("editbook.authors"), Function.identity());

        this.handler = handler;
        this.services = services;
        this.localizer = localizer;
        this.getFooter().add(new CloseDialogButton(this, localizer));

        var formLayout = new FormLayout();
        var addImagesDialog = VaadinCommons.editImagesDialog(images, localizer, services.fileStorageService());
        var addImagesButton = new OpenDialogButton(localizer.get("editbook.editimages"), addImagesDialog);

        var contentProviderDialog = makeSelectContentProviderDialog();
        var contentProviderDialogButton = new OpenDialogButton(localizer.get("editbook.editcontentprovider"), contentProviderDialog);

        formLayout.add(title, description, isbn, subjects, authors, publishYear, addImagesButton, contentProviderDialogButton, new Button(localizer.get("editbook.submit"), _ -> submit()));

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
