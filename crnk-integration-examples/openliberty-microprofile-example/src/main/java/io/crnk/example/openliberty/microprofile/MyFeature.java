package io.crnk.example.openliberty.microprofile;

import io.crnk.rs.CrnkFeature;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class MyFeature implements Feature {
  @Override
  public boolean configure(FeatureContext featureContext) {
    featureContext.register(CrnkFeature.class);
    return true;
  }
}
