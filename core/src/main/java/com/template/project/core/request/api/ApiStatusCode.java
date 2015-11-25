package com.template.project.core.request.api;

/**
 * API Status Codes http response body.
 */
public enum ApiStatusCode {
    
    // series 2xx - users
    USER_NON_EXISTENT(201, "user.non.existent", false);

    private int value;
    private String codeName;
    private boolean isSuccess;

    ApiStatusCode(int val, String codeName, boolean isSuccess) {
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

    /**
     * Get the ApiStatusCode from passed status code number;
     * @param code The standard http status code number.
     * @return ApiStatusCode from passed code number.
     */
    public static ApiStatusCode fromCode(int code) {
        for (ApiStatusCode statusCode : ApiStatusCode.values()) {
            if (statusCode.getCode() == code)
                return statusCode;
        }
        return null;
    }

}