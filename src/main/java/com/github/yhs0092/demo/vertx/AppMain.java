package com.github.yhs0092.demo.vertx;

import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class AppMain {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    HttpServer httpServer = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create().setUploadsDirectory("uploadTemp/"));
    router.routeWithRegex("/upload").handler(routingContext -> {
      Set<FileUpload> fileUploads = routingContext.fileUploads();
      LOGGER.info("get [" + fileUploads.size() + "] uploaded files");
      for (FileUpload fileUpload : fileUploads) {
        LOGGER.info("get uploaded file: [" + fileUpload.uploadedFileName() + "], size = [" + fileUpload.size() + "]");
      }

      routingContext.response()
          .putHeader("Content-Type", "text/plain")
          .end("get " + fileUploads.size() + " files.");
    });

    httpServer.requestHandler(router::accept).listen(8080,
        res -> {
          if (res.succeeded()) {
            LOGGER.info("Server is up!");
          } else {
            LOGGER.info("Server init failed!");
          }
        });
  }
}
