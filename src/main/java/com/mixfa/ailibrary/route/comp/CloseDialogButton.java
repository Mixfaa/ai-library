package com.mixfa.ailibrary.route.comp;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

public class CloseDialogButton extends Button {
    public CloseDialogButton(Dialog dialog) {
        super("Close", _ -> dialog.close());
    }
}