package io.crnk.example.openliberty.microprofile;

import io.crnk.rs.CrnkFeature;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MyFeature implements Feature {
  @Override
  public boolean configure(FeatureContext featureContext) {
    featureContext.register(CrnkFeature.class);
    return true;
  }
}
