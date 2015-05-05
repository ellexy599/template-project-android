package com.template.project.core.http.api;

/**
 * API Server Status Message codes and standard Http Status Codes
 */
public enum ApiStatusCode {

    OK(200, "ok", true),
    CREATED(201, "created", true),
    BAD_REQUEST(400, "bad_request", false),
    UNAUTHORIZED(401, "unauthorized", false),
    FOBIDDEN(403, "forbidden", false),
    NOT_FOUND(404, "not_found", false),
    METHOD_NOT_ALLOWED(405, "method_not_allowed", false),
    NOT_ACCEPTABLE(406, "not_acceptable", false),
    BAD_GATEWAY(502, "bad_gateway", false),
    REQUEST_TIMEOUT(408, "request_timeout", false),
    GATEWAY_TIMEOUT(504, "gateway_timeout", false),
    INTERNAL_SERVER_ERROR(500, "internal_server_error", false),
    SOCKET_CONNECTION_TIMEOUT(504, "socket_connection_timeout", false),
    UNEXPECTED_ERROR(505, "unexpected_error", false);

    private int value;
    private String codeName;
    private boolean isSuccess;

    private ApiStatusCode(int val, String codeName, boolean isSuccess) {
        this.value = val;
        this.codeName = codeName;
        this.isSuccess = isSuccess;
    }

    public int getCode() {
        return this.value;
    }

    public String getCodeName() {
        return this.codeName;
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

}
