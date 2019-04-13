package io.crnk.client;

import io.crnk.client.action.JerseyActionStubFactory;
import io.crnk.test.mock.ClientTestModule;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Class creates a CrnkClient to serialize links as JSON objects.<br />
 * All tests can be found in {@link AbstractProxiedObjectsClientTest}.
 */
public class ObjectLinkProxiedObjectsClientTest extends AbstractProxiedObjectsClientTest {

    @Override
    protected void createClient() {
        client = new CrnkClient(getBaseUri().toString(), CrnkClient.ClientType.OBJECT_LINKS);
        client.addModule(new ClientTestModule());
        // tag::jerseyStubFactory[]
        client.setActionStubFactory(JerseyActionStubFactory.newInstance());
        // end::jerseyStubFactory[]
        client.getHttpAdapter().setReceiveTimeout(10000000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected TestApplication configure() {
        return new TestApplication(false, true);
    }


    @Ignore
    @Override
    @Test
    public void noproxyForIdFieldAndSerializedId() {
        // TODO fully implement on crnk client
    }

    @Ignore
    @Override
    @Test
    public void noProxyForLazy() {
        // TODO fully implement on crnk client
    }


    @Ignore
    @Override
    @Test
    public void proxyForSerializedIdWithoutRelationId() {
        // TODO fully implement on crnk client
    }
}
