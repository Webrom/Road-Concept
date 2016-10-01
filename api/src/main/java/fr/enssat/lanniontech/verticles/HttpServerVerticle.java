package fr.enssat.lanniontech.verticles;

import fr.enssat.lanniontech.utilities.Constants;
import fr.enssat.lanniontech.verticles.utilities.HttpResponseBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private static final int KB = 1024;
    private static final int MB = 1024 * KB;

    @Override
    public void start() {
        Router router = Router.router(vertx);

        configureGlobalHandlers(router);

        vertx.deployVerticle(new APIDocVerticle(router));
        vertx.deployVerticle(new AuthenticationVerticle(router));
        vertx.deployVerticle(new MapsVerticle(router));

        vertx.createHttpServer().requestHandler(router::accept).listen(Constants.HTTP_SERVER_PORT);
    }

    private void configureGlobalHandlers(Router router) {
       // router.route().handler(CorsHandler.create("*")); // Allows cross domain origin request. Necessary to test requests with another Swagger server.
        router.route().handler(BodyHandler.create().setBodyLimit(10 * MB));
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        // Require authentication for all path starting "/api"
        router.route("/api/*").handler(routingContext -> {
            if (routingContext.session() == null || routingContext.session().get(Constants.SESSION_CURRENT_USER) == null) {
                HttpResponseBuilder.buildUnauthorizedResponse(routingContext);
            } else {
                routingContext.next(); // process the next handler, if any
            }
        });

    }
}