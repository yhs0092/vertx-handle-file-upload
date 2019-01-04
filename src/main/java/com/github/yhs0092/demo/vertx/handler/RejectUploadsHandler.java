package com.github.yhs0092.demo.vertx.handler;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class RejectUploadsHandler implements Handler<RoutingContext> {
  private static final int REJECT_AS = 501;

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (request.headers().contains(HttpHeaders.UPGRADE, HttpHeaders.WEBSOCKET, true)) {
      context.next();
      return;
    }

    final String contentType = context.request().getHeader(HttpHeaders.CONTENT_TYPE);
    final String lowerCaseContentType = contentType.toLowerCase();
    boolean isMultipart = lowerCaseContentType.startsWith(HttpHeaderValues.MULTIPART_FORM_DATA.toString());
    if (isMultipart) {
      context.request().setExpectMultipart(true);
      request.uploadHandler(u -> {
        if (!context.failed()) {
          context.fail(REJECT_AS);
        }
      });
      request.endHandler(v -> {
        if (!context.failed()) {
          context.next();
        }
      });
    } else {
      context.next();
    }
  }
}
