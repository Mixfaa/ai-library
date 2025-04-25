package com.mixfa.ailibrary.route;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@AnonymousAllowed
public class LoginRoute extends VerticalLayout {
    public LoginRoute() {

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(new Anchor("/oauth2/authorization/github", "Authorize with github") {{
            setRouterIgnore(true);
        }});
    }
}
