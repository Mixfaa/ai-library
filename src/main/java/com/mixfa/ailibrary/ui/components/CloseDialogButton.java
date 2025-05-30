package com.mixfa.ailibrary.ui.components;

import com.mixfa.ailibrary.ui.localization.LocalizationProvider;
import com.mixfa.ailibrary.ui.localization.Localizator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

public class CloseDialogButton extends Button {
    public CloseDialogButton(Dialog dialog, Localizator localizator) {
        super(localizator.get("closebutton"), _ -> dialog.close());
    }
}