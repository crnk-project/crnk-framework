package io.crnk.core.engine.http;

public class HttpStatus {


    private HttpStatus() {
    }

    public static final int OK_200 = 200;

    public static final int CREATED_201 = 201;

    public static final int ACCEPTED_202 = 202;

    public static final int NO_CONTENT_204 = 204;

    public static final int NOT_FOUND_404 = 404;

    public static final int BAD_REQUEST_400 = 400;

    public static final int UNAUTHORIZED_401 = 401;

    public static final int FORBIDDEN_403 = 403;

    public static final int METHOD_NOT_ALLOWED_405 = 405;

    public static final int CONFLICT_409 = 409;

    public static final int PRECONDITION_FAILED_412 = 412;

    public static final int UNSUPPORTED_MEDIA_TYPE_415 = 415;

    public static final int UNPROCESSABLE_ENTITY_422 = 422;

    public static final int INTERNAL_SERVER_ERROR_500 = 500;

    public static final int NOT_IMPLEMENTED_501 = 501;

    public static final int BAD_GATEWAY_502 = 502;

    public static final int SERVICE_UNAVAILABLE_503 = 503;

    public static final int GATEWAY_TIMEOUT_504 = 504;


    public static final int HTTP_VERSION_NOT_SUPPORTED_505 = 505;

    public static String toMessage(int statusCode) {
        switch (statusCode) {
            case OK_200:
                return "OK";
            case CREATED_201:
                return "CREATED";
            case NO_CONTENT_204:
                return "NO_CONTENT";
            case NOT_FOUND_404:
                return "NOT_FOUND";
            case BAD_REQUEST_400:
                return "BAD_REQUEST";
            case UNAUTHORIZED_401:
                return "UNAUTHORIZED";
            case FORBIDDEN_403:
                return "FORBIDDEN";
            case METHOD_NOT_ALLOWED_405:
                return "METHOD_NOT_ALLOWED";
            case CONFLICT_409:
                return "CONFLICT";
            case PRECONDITION_FAILED_412:
                return "PRECONDITION_FAILED";
            case UNSUPPORTED_MEDIA_TYPE_415:
                return "UNSUPPORTED_MEDIA_TYPE";
            case UNPROCESSABLE_ENTITY_422:
                return "UNPROCESSABLE_ENTITY";
            case INTERNAL_SERVER_ERROR_500:
                return "INTERNAL_SERVER_ERROR";
            case NOT_IMPLEMENTED_501:
                return "NOT_IMPLEMENTED";
            case BAD_GATEWAY_502:
                return "BAD_GATEWAY";
            case SERVICE_UNAVAILABLE_503:
                return "SERVICE_UNAVAILABLE";
            case GATEWAY_TIMEOUT_504:
                return "GATEWAY_TIMEOUT";
            case HTTP_VERSION_NOT_SUPPORTED_505:
                return "HTTP_VERSION_NOT_SUPPORTED";
            default:
                return null;
        }
    }
}
