package com.github.yhs0092.demo.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class AppMain {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    HttpServer httpServer = vertx.createHttpServer();

    Router router = Router.router(vertx);

    router.route()
        .handler(
            BodyHandler.create().setBodyLimit(256).setDeleteUploadedFilesOnEnd(true))
        .failureHandler(context -> {
          LOGGER.info("get failure!", context.failure());

          if (!context.response().ended()) {
            context.response().setStatusCode(413).end("failure response");
          }
          if (!context.response().closed()) {
            // if I close this response, the uploaded file will not be removed
            LOGGER.info("close response");
            context.response().close();
          }
        });

    router.routeWithRegex("/upload").handler(routingContext ->
        routingContext.response()
            .putHeader("Content-Type", "text/plain")
            .end("get " + routingContext.fileUploads().size() + " files."));

    httpServer.requestHandler(router).listen(8080,
        res -> {
          if (res.succeeded()) {
            LOGGER.info("Server is up!");
          } else {
            LOGGER.info("Server init failed!");
          }
        });
  }
}
