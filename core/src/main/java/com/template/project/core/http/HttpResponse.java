package com.template.project.core.http;

import retrofit.client.Response;

public interface HttpResponse {

    /**
     * Get the MessageCode of the reponse of http request.
     * @return The HttpStatus code.
     */
    int getCode();

    /**
     * Set HttpStatus code or the Http Status Code.
     */
    void setCode(int code);

    /**
     * Get the ApiStatusCode name.
     * @return Code name of ApiStatusCode.
     */
    String getCodeName();

    /**
     * Set ApiStatusCode code name.
     */
    void setCodeName(String name);

    /**
     * Return the response message if API Server Status Message is the response of the request.
     * @return The response code message of the performed http request.
     */
    String getCodeMessage();

    /**
     * Set the response message from API call.
     */
    void setCodeMessage(String message);

    /**
     * Return the response of the http request.
     * @return The response body of the performed http request.
     */
    String getResponseBody();

    /**
     * Set the response body of the http request performed.
     */
    void setResponseBody(String responseBody);

    /**
     * Return true if http request is successful
     */
    boolean isReqSuccess();

    /**
     *  Set if http request success or not. Use in file downloading.
     *  @param isReqSuccess Determine of http request made is successful.
     */
    void setReqSuccess(boolean isReqSuccess);

    /**
     * Assign with default value of HttpStatus for MessageCode and codeMessage base
     * from passed result of http request from default constructor.
     * @param responseObj The standard http status response
     */
    void initHttpResponse(Response responseObj);

}
