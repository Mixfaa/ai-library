package com.mixfa.ailibrary.route;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteNotFoundError;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("404")
@AnonymousAllowed
public class NotFoundRoute extends RouteNotFoundError {
    public NotFoundRoute() {
        getUI().ifPresent(ui ->
                ui.getPage().setLocation("/")
        );
    }
}
