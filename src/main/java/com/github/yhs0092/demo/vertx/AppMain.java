package com.github.yhs0092.demo.vertx;

import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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

    // Here I set uploads dir as null, but it doesn't work as I expect.
//    router.route().handler(new RejectUploadsHandler());
//        .handler(ctx -> {
//          System.out.println("pre handle");
//          ctx.request().uploadHandler(upload -> {
////        ctx.request().connection().close();
////        ctx.response()
////            .putHeader("Content-Type", "text/plain")
////            .end("uploading file not supported");
//            System.out.println("heihei!");
//          });
//          ctx.next();
//        })
//        .handler(BodyHandler.create())
//        .handler(ctx -> {
//          System.out.println("go on");
//          ctx.next();
//        });

    router.route()
        .handler(
            BodyHandler.create().setBodyLimit(256)/*.setHandleFileUploads(true)*/.setDeleteUploadedFilesOnEnd(true))
        .failureHandler(context -> {
          LOGGER.info(
              "get failure, context status [" + context.statusCode() + "], response status [" + context.response()
                  .getStatusCode() + "]",
              context.failure());

          if (!context.response().ended()) {
            context.response().setStatusCode(400).end("end!!");
          }
          if (!context.response().closed()) {
            LOGGER.info("close response");
            context.response().close();
          }
        });

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
    router.routeWithRegex("/testPost").handler(routingContext -> {
      final Buffer body = routingContext.getBody();
      LOGGER.info("/testPost gets body: [" + body + "]");
      routingContext.response()
          .putHeader("Content-Type", routingContext.request().getHeader("Content-Type"))
          .end(body.toString());
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
