package io.crnk.example.dropwizard.mongo.managed;

import com.mongodb.MongoClient;
import io.crnk.example.dropwizard.mongo.MongoConfiguration;
import io.dropwizard.lifecycle.Managed;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class MongoManaged implements Managed {
	private final MongoClient mongoClient;
	private final Datastore datastore;

	public MongoManaged(MongoConfiguration mongoConfig) throws Exception {
		mongoClient = new MongoClient(mongoConfig.host, mongoConfig.port);
		datastore = new Morphia().createDatastore(mongoClient, mongoConfig.db);
	}

	public Datastore getDatastore() {
		return datastore;
	}

	public void start() throws Exception {
	}

	public void stop() throws Exception {
		mongoClient.close();
	}
}
