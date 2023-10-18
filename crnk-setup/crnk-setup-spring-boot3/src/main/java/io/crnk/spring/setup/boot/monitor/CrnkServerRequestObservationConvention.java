package io.crnk.spring.setup.boot.monitor;

import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;

@Slf4j
public class CrnkServerRequestObservationConvention extends DefaultServerRequestObservationConvention {
	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkServerRequestObservationConvention.class);
}
