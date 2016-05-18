package com.template.project.core.request.http.spring;

import android.content.Context;
import android.util.Pair;

import com.template.project.core.AppConfiguration;
import com.template.project.core.entity.user.User;
import com.template.project.core.request.api.ApiCallback;
import com.template.project.core.request.api.ApiHttpUtil;
import com.template.project.core.request.api.ApiParams;
import com.template.project.core.request.api.ApiServiceType;
import com.template.project.core.request.http.retrofit.RetroHttpResponse;
import com.template.project.core.request.json.JsonParser;
import com.template.project.core.request.ssl.NoSslv3SocketFactory;
import com.template.project.core.utils.LogMe;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Class that execute calls of http request to the API Server.
 */
public class SpringHttpRequest implements SpringHttpService {

    private final String TAG = SpringHttpRequest.class.getSimpleName();

    private Context mCtx;

    private ApiServiceType mApiServiceType;
    private ApiCallback mApiCallback;
    private RetroHttpResponse mRetroHttpResponse;

    public static int TIMEOUT = 10000;
    public static int RETRY = 4;

    public SpringHttpRequest(Context ctx, ApiServiceType apiServiceType, ApiCallback apiCallback) {
        this.mCtx = ctx;
        this.mApiServiceType = apiServiceType;
        this.mApiCallback = apiCallback;
    }

    /**
     * Set the timeout of http request in millisecond. By default the timeout is 10000ms.
     * @param requestTimeout Millisecond value of timeout of http request.
     */
    public void setRequestTimeout(int requestTimeout) {
        if (requestTimeout > TIMEOUT) {
            TIMEOUT = requestTimeout;
        }
    }

    /**
     * Get the timeout of http request in millisecond. Default is 10000ms.
     * @return The millisecond timeout value of http request.
     */
    public int getRequestTimeout() {
        return TIMEOUT;
    }

    /**
     * Initialize and log SpringHttpResponse of the http request response.
     * @param status The status of http request.
     * @param messageBody The ReponseEntity body of the http request.
     */
    protected void initResponse(boolean isSuccess, HttpStatus status, String messageBody) {
        if (!isSuccess) {
            mApiCallback.onFailed(new SpringHttpResponse(HttpStatus.BAD_REQUEST, messageBody));
        } else {
            mApiCallback.onSuccess(new SpringHttpResponse(status, messageBody));
        }
        LogMe.d(TAG, "STATUS REASON PHARSE: " + status.getReasonPhrase());
    }

    /** Set SSL and default timeout of the RestTemplate */
    protected void defineRequestFactory(RestTemplate restTemp) {
        LogMe.d(TAG, "defineRequestFactory timeout: " + TIMEOUT);
        // set timeout
        ClientHttpRequestFactory requestFactory = restTemp.getRequestFactory();
        try {
            SSLContext sslcontext = SSLContext.getDefault();
            SSLSocketFactory noSslv3SocketFactory = new NoSslv3SocketFactory(sslcontext.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(noSslv3SocketFactory);
        } catch (Exception e) {
            LogMe.d(TAG, "defineRequestFactory ERROR " + e.toString());
        }

        if (requestFactory instanceof SimpleClientHttpRequestFactory) {
            LogMe.d(TAG, "defineRequestFactory HttpUrlConnection is used");
            ((SimpleClientHttpRequestFactory) requestFactory).setConnectTimeout(TIMEOUT);
            ((SimpleClientHttpRequestFactory) requestFactory).setReadTimeout(TIMEOUT);
        } else if (restTemp.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
            // API level 8 and lower uses this connection
            LogMe.d(TAG, "defineRequestFactory HttpClient is used");
            ((HttpComponentsClientHttpRequestFactory) requestFactory).setReadTimeout(TIMEOUT);
            ((HttpComponentsClientHttpRequestFactory) requestFactory).setConnectTimeout(TIMEOUT);
        }
    }

    /**
     * Add header Pair values to HttpHeaders of the request.
     * @param arguments The first argument is the HttpHeader object and the proceeding argument 
     *                  objects are the Pair containing the header name and value.
     */
    protected void addHeaders(Object...arguments) {
        for (int i=1; i < arguments.length; i++) {
            Pair pair = (Pair) arguments[i];
            String headerName = String.valueOf(pair.first);
            String headerValue = String.valueOf(pair.second);
            ((HttpHeaders) arguments[0]).add(headerName, headerValue);
        }
    }

    /** Set all the request character encoding to UTF-8 */
    protected void setCharacterEncoding(HttpHeaders httpHeader) {
        Charset utf8 = Charset.forName("UTF-8");
        MediaType mediaType = new MediaType("application", "json", utf8);
        httpHeader.setContentType(mediaType);
    }

    /** Provide a custom error handler of all the request. */
    protected void setErrorHandler(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                LogMe.e(TAG, "handleError(): " + response.getStatusCode());
            }
        });
    }

    /**
     * Execute specific request based on the {@link ApiServiceType} set in the instance
     * of this SpringHttpRequest.
     * @param apiParams HttpRequestParams object containing the needed data in http request.
     */
    public void executeHttpRequest(ApiParams apiParams) {
        switch (mApiServiceType) {
            case USER_CREATE:
                userRegister(apiParams.getUser());
                break;
            case USER_GET_DETAILS:
                break;
            case USER_UPDATE_DETAILS:
                break;
        }
    }

    @Override
    public void userRegister(User userObj) {
        try {
            String url = AppConfiguration.HOST.concat("/user");
            LogMe.d(TAG, "forgotPassword URL: " + url);
            RestTemplate restTemp = new RestTemplate();
            HttpHeaders httpHeader = new HttpHeaders();
            restTemp.getMessageConverters().add(new StringHttpMessageConverter());
            addHeaders(httpHeader, ApiHttpUtil.getHeaderDeviceOs(),
                    ApiHttpUtil.getHeaderDeviceVersion(mCtx));
            defineRequestFactory(restTemp);
            setErrorHandler(restTemp);
            setCharacterEncoding(httpHeader);
            String reqBody = JsonParser.fromUser(userObj);
            LogMe.d(TAG, "userRegister HEADERS: " + httpHeader.toString());
            LogMe.d(TAG, "userRegister BODY: " + reqBody);
            HttpEntity<String> requestEntity = new HttpEntity<>(reqBody, httpHeader);
            ResponseEntity<String> responseEntity = restTemp.postForEntity(url, requestEntity, String.class);
            initResponse(true, responseEntity.getStatusCode(), responseEntity.getBody());
            LogMe.d(TAG, "userRegister RESPONSE MESSAGE: " + responseEntity.getBody());
        } catch (Exception e) {
            LogMe.e(TAG, "ERROR userRegister " + e.toString());
            initResponse(false, HttpStatus.BAD_REQUEST, "");
        }
    }

    @Override
    public void userGetDetails(String sessionToken, String userId) {
        try {
            String url = AppConfiguration.HOST.concat("/user/").concat(userId);
            RestTemplate restTemp = new RestTemplate();
            restTemp.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpHeaders httpHeader = new HttpHeaders();
            addHeaders(httpHeader, sessionToken, ApiHttpUtil.getHeaderDeviceOs(),
                    ApiHttpUtil.getHeaderDeviceVersion(mCtx));
            defineRequestFactory(restTemp);
            setErrorHandler(restTemp);
            setCharacterEncoding(httpHeader);
            setErrorHandler(restTemp);
            HttpEntity<String> requestEntity = new HttpEntity<>(httpHeader);
            ResponseEntity<String> responseEntity = restTemp.exchange(url, HttpMethod.GET, requestEntity, String.class);
            LogMe.d(TAG, "userGetDetails URL: " + url);
            LogMe.d(TAG, "userGetDetails HEADERS: " + httpHeader.toString());
            initResponse(false, responseEntity.getStatusCode(), responseEntity.getBody());
            LogMe.d(TAG, "userGetDetails RESPONSE MESSAGE: " + responseEntity.getBody());
        } catch (Exception e) {
            LogMe.e(TAG, "userGetDetails ERROR: " + e.toString());
            initResponse(false, HttpStatus.REQUEST_TIMEOUT, "");
        }
    }

    @Override
    public void userUpdateDetails(String sessionToken, String userId, User userObj) {
        try {
            String url = AppConfiguration.HOST.concat("/user");
            LogMe.d(TAG, "forgotPassword URL: " + url);
            RestTemplate restTemp = new RestTemplate();
            HttpHeaders httpHeader = new HttpHeaders();
            restTemp.getMessageConverters().add(new StringHttpMessageConverter());
            addHeaders(httpHeader, sessionToken, ApiHttpUtil.getHeaderDeviceOs(),
                    ApiHttpUtil.getHeaderDeviceVersion(mCtx));
            defineRequestFactory(restTemp);
            setErrorHandler(restTemp);
            setCharacterEncoding(httpHeader);
            String reqBody = JsonParser.fromUser(userObj);
            LogMe.d(TAG, "userUpdateDetails HEADERS: " + httpHeader.toString());
            LogMe.d(TAG, "userUpdateDetails BODY: " + reqBody);
            HttpEntity<String> requestEntity = new HttpEntity<>(reqBody, httpHeader);
            ResponseEntity<String> responseEntity = restTemp.postForEntity(url, requestEntity, String.class);
            initResponse(false, responseEntity.getStatusCode(), responseEntity.getBody());
            LogMe.d(TAG, "userUpdateDetails RESPONSE MESSAGE: " + responseEntity.getBody());
        } catch (Exception e) {
            LogMe.e(TAG, "ERROR userUpdateDetails " + e.toString());
            initResponse(false, HttpStatus.REQUEST_TIMEOUT, "");
        }
    }

}
