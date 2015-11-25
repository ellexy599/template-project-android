package com.template.project.core.request.http.retrofit;

import com.template.project.core.request.api.ApiResponse;
import com.template.project.core.request.api.ApiStatusModel;
import com.template.project.core.request.http.HttpStatusCode;
import com.template.project.core.request.json.JsonName;
import com.template.project.core.request.json.JsonParser;
import com.template.project.core.utils.FieldValidator;
import com.template.project.core.utils.LogMe;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import retrofit.client.Header;
import retrofit.client.Response;

/**
 * Holds the http status code of all http request called and contains the
 * API Server Status Message if the response codeMessage of http request has the
 * JSON format of it.
 */
public class RetroHttpResponse implements ApiResponse {

    private final String TAG = RetroHttpResponse.class.getSimpleName();

    public static final String HEADER_COOKIE = "Cookie";
    public static final String HEADER_COOKIE_RESPONSE = "Set-Cookie";

    private int mCode;
    private String mCodeName;
    private String mCodeMessage;
    private String mResponseBody;
    private boolean mIsRequestSuccess = true;

    // Message to be thrown to ApiResponse for unexpected error or connection timeout.
    public static final String MSG_NETWORK_TIMEOUT =
            "Could not connect to server. Please check your connection.";
    public static final String MSG_UNEXPECTED_ERROR =
            "Oops! Something went wrong. Please try again after a few minutes.";

    @Override
    public int getCode() {
        return this.mCode;
    }

    @Override
    public void setCode(int code) {
        this.mCode = code;
    }

    @Override
    public String getCodeName() {
        return this.mCodeName;
    }

    @Override
    public void setCodeName(String name) {
        this.mCodeName = name;
    }

    @Override
    public String getCodeMessage() {
        return mCodeMessage;
    }

    @Override
    public void setCodeMessage(String codeMessage) {
        this.mCodeMessage = codeMessage;
    }

    @Override
    public String getResponseBody() {
        return this.mResponseBody;
    }

    @Override
    public void setResponseBody(String responseBody) {
        this.mResponseBody = responseBody;
    }

    @Override
    public boolean isReqSuccess() {
        return mIsRequestSuccess;
    }

    @Override
    public void setReqSuccess(boolean isReqSuccess) {
        this.mIsRequestSuccess = isReqSuccess;
    }

    public RetroHttpResponse() {
        // allow empty initialization
    }

    /**
     * Constructor for RetroHttpResponse with HttpStatusCode or ApiStatusCode from http response of API call.
     * @param response Response obtained from the request reponse
     * @param httpResponseBody The String value of the response of the request.
     */
    public RetroHttpResponse(Response response, String httpResponseBody) {
        this.mCode = response.getStatus();
        this.mResponseBody = httpResponseBody;

        if(response != null) {
            for(HttpStatusCode httpStatusCode : HttpStatusCode.values()) {
                if(mCode == httpStatusCode.getCode()) {
                    mIsRequestSuccess = httpStatusCode.isSuccess();
                }
            }
        }

        boolean hasInitApiStatusCode = false;

        //replace code, codeName, message error with API Status Code
        if (!mIsRequestSuccess && FieldValidator.isStringValid(httpResponseBody) &&
                httpResponseBody.startsWith("{") && httpResponseBody.endsWith("}") &&
                httpResponseBody.contains(JsonName.API_RESPONSE_STATUS) &&
                httpResponseBody.contains(JsonName.API_RESPONSE_MESSAGE) &&
                httpResponseBody.contains(JsonName.API_RESPONSE_CODE)) {

            ApiStatusModel apiStatusModel = JsonParser.toApiStatusModel(httpResponseBody);
            this.mCode = apiStatusModel.getCode();
            this.mIsRequestSuccess = apiStatusModel.getStatus()
                    .equalsIgnoreCase("success") ? true : false;
            this.mCodeName = apiStatusModel.getMessage();
            this.mCodeMessage = apiStatusModel.getMessage();

            hasInitApiStatusCode = true;
        }

        if (!mIsRequestSuccess && !hasInitApiStatusCode) {
            // initialize codeMessage for general error status
            if (mCode == HttpStatusCode.SOCKET_CONNECTION_TIMEOUT.getCode() ||
                    mCode == HttpStatusCode.GATEWAY_TIMEOUT.getCode() ||
                    mCode == HttpStatusCode.REQUEST_TIMEOUT.getCode()) {
                this.mIsRequestSuccess = false;
                this.mCodeMessage = MSG_NETWORK_TIMEOUT;

            } else if (mCode == HttpStatusCode.INTERNAL_SERVER_ERROR.getCode() ||
                    mCode == HttpStatusCode.UNEXPECTED_ERROR.getCode()) {
                this.mIsRequestSuccess = false;
                this.mCodeMessage = MSG_UNEXPECTED_ERROR;

            } else if (!mIsRequestSuccess) {
                HttpStatusCode genericError = HttpStatusCode.UNEXPECTED_ERROR;
                this.mCode = genericError.getCode();
                this.mIsRequestSuccess = false;
                this.mCodeMessage = MSG_UNEXPECTED_ERROR;
            }
        }
    }

    /** Constructor for ApiResponse for network issue and unexpected error only. */
    public RetroHttpResponse(HttpStatusCode httpStatusCode) {
        mCode = httpStatusCode.getCode();
        mCodeName = httpStatusCode.getCodeName();
        if(mCode == HttpStatusCode.SOCKET_CONNECTION_TIMEOUT.getCode() ||
                mCode == HttpStatusCode.GATEWAY_TIMEOUT.getCode() ||
                mCode == HttpStatusCode.REQUEST_TIMEOUT.getCode()) {
            mIsRequestSuccess = false;
            mCodeMessage = MSG_NETWORK_TIMEOUT;
        } else if(mCode == HttpStatusCode.UNEXPECTED_ERROR.getCode() ||
                mCode == HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()) {
            mIsRequestSuccess = false;
            mCodeMessage = MSG_UNEXPECTED_ERROR;
        } else if(mResponseBody.contains(JsonName.API_RESPONSE_MESSAGE) &&
                mResponseBody.startsWith("{") && mResponseBody.endsWith("}")) {
            try {
                JSONObject jsonObj = new JSONObject(mResponseBody);
                this.mCodeMessage = jsonObj.getString(JsonName.API_RESPONSE_MESSAGE);
            } catch (JSONException e) {
                LogMe.w(TAG, "ApiResponse could not extract 'message' JSON field value.");
            }
        }
    }

    @Override
    public String toString() {
        return getCodeMessage();
    }

    /**
     * Extract the cookie from http response.
     * @return The AuthCookie object containing the cookie header name and cookie header value.
     */
    private void extractCookie(Response response) {
        if(response != null && response.getHeaders() != null && response.getHeaders().size() > 0) {
            for(Header header : response.getHeaders()) {
                if(header != null && header.getName() != null) {
                    CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
                    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                    CookieHandler.setDefault(cookieManager);
                    if(header.getName().equals(HEADER_COOKIE_RESPONSE)) {
                        String cookieName = HEADER_COOKIE;
                        String cookieValue = header.getValue();
                        LogMe.d(TAG, "extract header name: " + header.getName()
                                + " " + header.getValue());
                    }
                }
            }
        }
    }
}
