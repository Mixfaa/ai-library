package com.mixfa.ailibrary.route.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

public class OpenDialogButton extends Button {
    public OpenDialogButton(String caption, Dialog dialog) {
        super(caption, _ -> dialog.open());
    }
}
