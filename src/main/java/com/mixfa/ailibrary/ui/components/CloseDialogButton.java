package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.ui.localization.Localizer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

public class CloseDialogButton extends Button {
    public CloseDialogButton(Dialog dialog, Localizer localizer) {
        super(localizer.get("closebutton"), _ -> dialog.close());
    }
}