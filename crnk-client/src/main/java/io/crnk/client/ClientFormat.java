package io.crnk.client;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.internal.utils.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Specifies in which format request and responses are sent.
 */
public enum ClientFormat {

    /**
     * Default
     */
    JSONAPI(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET, HttpHeaders.JSONAPI_CONTENT_TYPE, Document.class.getName()),

    /**
     * Enabled if PlainJsonFormatModule is present.
     */
    PLAINJSON(HttpHeaders.JSON_CONTENT_TYPE + "; charset=" + HttpHeaders.DEFAULT_CHARSET,
            HttpHeaders.JSON_CONTENT_TYPE, "io.crnk.format.plainjson.internal.PlainJsonDocument");

    private final String documentClassName;

    private final String contentType;

    private final String acceptType;

    private Class<? extends Document> documentClass;

    private Constructor transportDocumentFactory;

    ClientFormat(String contentType, String acceptType, String documentClassName) {
        this.contentType = contentType;
        this.acceptType = acceptType;
        this.documentClassName = documentClassName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getAcceptType() {
        return acceptType;
    }

    public Class<? extends Document> getDocumentClass() {
        if (documentClass == null) {
            documentClass = (Class<? extends Document>) ClassUtils.loadClass(getClass().getClassLoader(), documentClassName);
        }
        return documentClass;
    }

    public Document toTransportDocument(Document document) {
        Class<? extends Document> documentClass = getDocumentClass();
        if (!documentClass.isInstance(document)) {
            try {
                if (transportDocumentFactory == null) {
                    transportDocumentFactory = documentClass.getConstructor(Document.class);
                }
                return (Document) transportDocumentFactory.newInstance(document);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
        return document;
    }}
