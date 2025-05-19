package com.mixfa.ailibrary.route.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

public class CloseDialogButton extends Button {
    public CloseDialogButton(Dialog dialog) {
        super("Close", _ -> dialog.close());
    }
}