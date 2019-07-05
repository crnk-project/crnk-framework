package io.crnk.meta.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListenerBase;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.TestModule;
import okhttp3.OkHttpClient.Builder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMetaJerseyTest extends JerseyTestBase {

    protected CrnkClient client;

    protected CrnkBoot boot;

    public static void setNetworkTimeout(CrnkClient client, final int timeout, final TimeUnit timeUnit) {
        OkHttpAdapter httpAdapter = (OkHttpAdapter) client.getHttpAdapter();
        httpAdapter.addListener(new OkHttpAdapterListenerBase() {

            @Override
            public void onBuild(Builder builder) {
                builder.readTimeout(timeout, timeUnit);
            }
        });
    }

    @Before
    public void setup() {
        client = new CrnkClient(getBaseUri().toString());
        client.addModule(createModule());
        setNetworkTimeout(client, 10000, TimeUnit.SECONDS);
    }

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    public MetaModule createModule() {
        MetaModuleConfig config = new MetaModuleConfig();
        config.addMetaProvider(new ResourceMetaProvider());
        MetaModule module = MetaModule.createServerModule(config);
        return module;
    }

    @ApplicationPath("/")
    private class TestApplication extends ResourceConfig {

        public TestApplication() {
            CrnkFeature feature = new CrnkFeature();
            ObjectMapper objectMapper = feature.getObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            feature.addModule(createModule());
            feature.addModule(new TestModule());
            boot = feature.getBoot();
            setupFeature(feature);
            register(feature);
        }
    }

    protected void setupFeature(CrnkFeature feature) {
    }

}
