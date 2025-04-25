package com.mixfa.ailibrary.route.comp;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

public class DialogCloseButton extends Button {

    public DialogCloseButton(Dialog dialog) {
        super("Close", _ -> dialog.close());
    } 
    
}