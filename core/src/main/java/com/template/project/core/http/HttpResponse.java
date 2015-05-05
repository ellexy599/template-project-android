package com.template.project.core.http;

import com.template.project.core.http.api.ApiResponse;
import com.template.project.core.http.api.ApiStatusCode;
import com.template.project.core.http.json.JsonName;
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
public class HttpResponse implements ApiResponse {

    private final String TAG = HttpResponse.class.getSimpleName();

    public static final String HEADER_COOKIE = "Cookie";
    public static final String HEADER_COOKIE_RESPONSE = "Set-Cookie";

    private int mCode;
    private String mCodeName;
    private String mCodeMessage;
    private String mResponseBody;
    private boolean mIsRequestSuccess = true;

    // Message to be thrown to HttpResponse for unexpected error or connection timeout.
    private final String MSG_NETWORK_TIMEOUT = "Could not connect to server. Please check your connection.";
    private final String MSG_UNEXPECTED_ERROR = "Oops! Something went wrong. Please try again after a few minutes.";

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

    @Override
    public void initHttpResponse(Response response) {
        if(response != null) {
            boolean hasCatchResponseCode = false;
            mCode = response.getStatus();
            for(ApiStatusCode apiStatusCode : ApiStatusCode.values()) {
                if(mCode == apiStatusCode.getCode()) {
                    mIsRequestSuccess = apiStatusCode.isSuccess();
                    hasCatchResponseCode = true;
                }
            }
            if(!hasCatchResponseCode) {
                LogMe.w(TAG, "initHttpResponse hasCatchResponseCode: " + hasCatchResponseCode);
                // didn't recognize response or response is unexpected
                ApiStatusCode genericError = ApiStatusCode.UNEXPECTED_ERROR;
                mCode = genericError.getCode();
                mCodeMessage = MSG_UNEXPECTED_ERROR;
                mIsRequestSuccess = false;
            }
        }
    }

    public HttpResponse() {
        // allow empty initialization
    }

    /**
     * Initialize HttpStatusCode from the reponse of the http request
     * @param response Response obtained from the request reponse
     * @param httpResponseBody The String value of the response of the request.
     *         This value will contain the API Server Status Message if it is the
     *         returned codeMessage of the request
     */
    public HttpResponse(Response response, String httpResponseBody) {
        this.mResponseBody = httpResponseBody;
        initHttpResponse(response);
        // initialize codeMessage value
        if(mCode == ApiStatusCode.SOCKET_CONNECTION_TIMEOUT.getCode()) {
            this.mCodeMessage = MSG_NETWORK_TIMEOUT;
        } else if(mCode == ApiStatusCode.INTERNAL_SERVER_ERROR.getCode() ||
                mCode == ApiStatusCode.UNEXPECTED_ERROR.getCode()) {
            this.mCodeMessage = MSG_UNEXPECTED_ERROR;
        } else if(httpResponseBody.contains(JsonName.HTTP_RESPONSE_STATUS) &&
                httpResponseBody.startsWith("{") && httpResponseBody.endsWith("}")) {
            try {
                JSONObject jsonObj = new JSONObject(httpResponseBody);
                this.mCodeMessage = jsonObj.getString(JsonName.HTTP_RESPONSE_STATUS);
            } catch (JSONException e) {
                LogMe.w(TAG, "HttpResponse could not extract 'message' JSON field value.");
            }
        }
    }

    /**
     * This constructor is used to initialize HttpResponse
     * for network issue and unexpected error only.
     */
    public HttpResponse(ApiStatusCode apiStatusCode) {
        mCode = apiStatusCode.getCode();
        mCodeName = apiStatusCode.getCodeName();
        if(apiStatusCode.equals(ApiStatusCode.SOCKET_CONNECTION_TIMEOUT)) {
            mIsRequestSuccess = false;
            mCodeMessage = MSG_NETWORK_TIMEOUT;
        } else if(apiStatusCode.equals(ApiStatusCode.UNEXPECTED_ERROR) ||
                apiStatusCode.equals(ApiStatusCode.INTERNAL_SERVER_ERROR)) {
            mIsRequestSuccess = false;
            mCodeMessage = MSG_UNEXPECTED_ERROR;
        } else if(mResponseBody.contains(JsonName.HTTP_RESPONSE_STATUS_MESSAGE) &&
                mResponseBody.startsWith("{") && mResponseBody.endsWith("}")) {
            try {
                JSONObject jsonObj = new JSONObject(mResponseBody);
                this.mCodeMessage = jsonObj.getString(JsonName.HTTP_RESPONSE_STATUS_MESSAGE);
            } catch (JSONException e) {
                LogMe.w(TAG, "HttpResponse could not extract 'message' JSON field value.");
            }
        }
    }

    @Override
    public String toString() {
        return getCodeMessage();
    }

    /**
     * Extract the cookie from http response.
     * @return The AuthCookie object containing the cookie header name and cookie header value;
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
