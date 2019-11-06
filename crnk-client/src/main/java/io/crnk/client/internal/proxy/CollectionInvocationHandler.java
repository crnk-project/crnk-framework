package io.crnk.client.internal.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.resource.meta.JsonLinksInformation;
import io.crnk.core.resource.meta.JsonMetaInformation;

public class CollectionInvocationHandler implements InvocationHandler, ObjectProxy {

    private final ObjectNode links;

    private final ObjectNode meta;

    private Collection<?> collection;

    private String url;

    private Class<?> resourceClass;

    private ClientProxyFactoryContext context;

    private boolean useSet;

    public CollectionInvocationHandler(Class<?> resourceClass, String url, ClientProxyFactoryContext context, boolean useSet, ObjectNode links, ObjectNode meta) {
        this.url = url;
        this.resourceClass = resourceClass;
        this.context = context;
        this.useSet = useSet;
        this.links = links;
        this.meta = meta;
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getMeta")) {
            return meta != null ? new JsonMetaInformation(meta, context.getModuleRegistry().getObjectMapper()) : null;
        }
        if (method.getName().equals("getLinks")) {
            return links != null ? new JsonLinksInformation(links, context.getModuleRegistry().getObjectMapper()) : null;
        }
        if (method.getDeclaringClass() == Object.class || method.getDeclaringClass() == ObjectProxy.class) {
            return method.invoke(this, args);
        }
        if (collection == null) {
            collection = context.getCollection(resourceClass, url);

            // convert list to set
            if (useSet) {
                collection = new HashSet<>(collection);
            }
        }
        try {
            return method.invoke(collection, args);
        } catch (InvocationTargetException e) { // NO SONAR ok this way
            throw e.getCause();
        }
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public boolean isLoaded() {
        return collection != null;
    }
}
