package org.cdpg.dx.catalogue;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.catalogue.client.CatalogueClient;
import org.cdpg.dx.catalogue.client.CatalogueClientImpl;
import org.cdpg.dx.catalogue.service.CatalogueService;
import org.cdpg.dx.catalogue.service.CatalogueServiceImpl;

import static org.cdpg.dx.common.config.ServiceProxyAddressConstants.CATALOGUE_SERVICE_ADDRESS;

public class CatalogueVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(CatalogueVerticle.class);
  static WebClient webClient;
  private MessageConsumer<JsonObject> consumer;
  private ServiceBinder binder;
  private CatalogueService catalogueService;
  private CatalogueClient catalogueClient;

  @Override
  public void start() throws Exception {
    try {
      validateConfig();
      binder = new ServiceBinder(vertx);

      WebClientOptions options =
          new WebClientOptions().setTrustAll(true).setVerifyHost(false).setSsl(true);
      webClient = WebClient.create(vertx, options);

      catalogueClient =
          new CatalogueClientImpl(
              config().getString("catServerHost"),
              config().getInteger("catServerPort"),
              config().getString("dxCatalogueBasePath"),
              webClient);

      catalogueService = new CatalogueServiceImpl(vertx, catalogueClient);
      consumer =
          binder
              .setAddress(CATALOGUE_SERVICE_ADDRESS)
              .register(CatalogueService.class, catalogueService);

      LOGGER.info("Catalogue Verticle deployed.");
    } catch (Exception e) {
      LOGGER.error("Failed to deploy CatalogueVerticle: {}", e.getMessage());
      throw e; // Rethrow to signal deployment failure
    }
  }

  @Override
  public void stop() throws Exception {
    LOGGER.info("Shutting down CatalogueVerticle and unregistering service proxy.");
    if (binder != null && consumer != null) {
      binder.unregister(consumer);
    }
  }

  private void validateConfig() {
    checkConfigExists("catServerHost");
    checkConfigExists("catServerPort");
    checkConfigExists("dxCatalogueBasePath");
  }

  private void checkConfigExists(String key) {
    if (config().getValue(key) == null) {
      LOGGER.error("Configuration error: Missing required configuration: [{}]", key);
      throw new IllegalArgumentException("Missing required configuration: " + key);
    }
  }
}
