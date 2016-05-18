package com.template.project.core.request.http.spring;

import com.template.project.core.request.api.ApiResponse;
import com.template.project.core.request.http.HttpStatusCode;
import com.template.project.core.request.json.JsonName;
import com.template.project.core.utils.LogMe;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

/**
 * Holds the http status code of all http request called and contains the
 * API Server Status Message if the response message of http request has the
 * JSON format of it. For example, the http request returned status code is OK, which is
 * successful, but the API treated the request as error. Like the app version calling
 * the API has correct parameters passed but the app version is old.
 */
public class SpringHttpResponse implements ApiResponse {

    private final String TAG = SpringHttpResponse.class.getSimpleName();

    private HttpStatus httpStatus;
    private int code;

    // use for API Server Status Code
    private String codeStatus;
    private String codeName;
    private String codeMessage;

    // response message of http request. For example, this will be the container of JSON String response
    // of the request of game details. This will only be use when response is successful.
    private String httpResponseMessage;

    private boolean isReqSuccess = true;

    @Override
    public boolean isReqSuccess() {
        return isReqSuccess;
    }

    @Override
    public void setReqSuccess(boolean isReqSuccess) {
        this.isReqSuccess = isReqSuccess;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getCodeName() {
        return codeName;
    }

    @Override
    public void setCodeName(String name) {

    }

    @Override
    public String getCodeMessage() {
        return this.codeMessage;
    }

    @Override
    public void setCodeMessage(String codeMessage) {}

    @Override
    public String getResponseBody() {
        return null;
    }

    @Override
    public void setResponseBody(String responseBody) {

    }

    public void initApiStatusCodeMessage(int codeValue) {
        for(HttpStatusCode apiStatusCode : HttpStatusCode.values()) {
            if(apiStatusCode.getCode() == codeValue) {
                code = apiStatusCode.getCode();
                codeName = apiStatusCode.getCodeName();
                isReqSuccess = false;
            }
        }
    }

    public void initStandardHttpStatusCode(HttpStatus httpStatus) {
        for(HttpStatus status : HttpStatus.values()) {
            if(httpStatus == status) {
                code = status.value();
                if(code == HttpStatus.OK.value() || code == HttpStatus.CREATED.value()) {
                    isReqSuccess = true;
                } else {
                    isReqSuccess = false;
                }
                for(HttpStatusCode apiStatusCode : HttpStatusCode.values()) {
                    if(apiStatusCode.getCode() == status.value()) {
                        codeName = apiStatusCode.getCodeName();
                    }
                }
            }
        }
    }

    /**
     * Initialize HttpStatusCode from the reponse of the http request
     * @param httpStatus    org.spring.framework.HttpStatusCode obtained from the request reponse
     * @param messageBody   The String value of the response of the request. This value will contain the
     *                      API Server Status Message if it is the returned message of the request
     */
    public SpringHttpResponse(HttpStatus httpStatus, String messageBody) {
        this.httpStatus = httpStatus;
        this.httpResponseMessage = messageBody;
        int codeValue = 0;

        initStandardHttpStatusCode(httpStatus);

        // JSON field 'message'
        if(messageBody.contains(JsonName.API_RESPONSE_MESSAGE)) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);
                codeMessage = jsonObj.getString(JsonName.API_RESPONSE_MESSAGE);
            } catch (JSONException e) {
                LogMe.d(TAG, e.toString());
            }
        }

        // JSON field 'status' value is either "success" or "error"
        if(messageBody.contains(JsonName.API_RESPONSE_STATUS)) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);
                codeStatus = jsonObj.getString(JsonName.API_RESPONSE_STATUS);
            } catch (JSONException e) {
                LogMe.d(TAG, e.toString());
            }
        }

        // JSON field 'code', this represents the API Status Code returned from the request
        if(messageBody.contains(JsonName.API_RESPONSE_CODE)) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);
                String codeStr = jsonObj.getString(JsonName.API_RESPONSE_CODE);// 'success' or 'error' only
                if(codeStr != null && !codeStr.equalsIgnoreCase("0")) {
                    codeValue = Integer.parseInt(codeStr);
                    initApiStatusCodeMessage(codeValue);//extract the API Status Codes
                }
            } catch (JSONException e) {
                LogMe.d(TAG, e.toString());
            }
        }
    }

    @Override
    public String toString() {
        String responseMessage = "org.spring.framework.HttpStatus value = " +
                httpStatus.value() + " " + httpStatus.getReasonPhrase() +
                " / HttpStatusCode code: " +  code + " codeName: " + codeName  + " codeMessage: " + codeMessage + " " +
                " / isRequestSuccess = " + isReqSuccess;
        return responseMessage;
    }
}
