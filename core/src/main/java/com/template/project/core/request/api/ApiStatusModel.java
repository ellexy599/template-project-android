package com.template.project.core.request.api;

/**
 * Represents the JSON of API Status Code response.
 * This class represents the JSON String response of the API in form of
 * { "code":"201", "message":"User registered", "status":"CREATED" }
 */
public class ApiStatusModel {

    private int code;
    private String message;
    private String status;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
