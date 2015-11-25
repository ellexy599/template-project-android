package com.template.project.core.request.http.retrofit;

import android.content.Context;

import com.template.project.core.AppConfiguration;
import com.template.project.core.entity.user.User;
import com.template.project.core.request.api.ApiCallback;
import com.template.project.core.request.api.ApiParams;
import com.template.project.core.request.api.ApiResponse;
import com.template.project.core.request.api.ApiServiceType;
import com.template.project.core.request.http.HttpStatusCode;
import com.template.project.core.request.json.JsonParser;
import com.template.project.core.request.mime.TypeJsonString;
import com.template.project.core.utils.LogMe;
import com.template.project.core.utils.conn.NetConnUtil;
import com.template.project.core.utils.file.OsUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.client.UrlConnectionClient;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedString;

/**
 * The http request calling Rest API services with the use of Retrofit.
 * This is used with Retrofit and OkHttpClient for calling restful APIs.
 * Example API call:
 * <pre>
 * RetroHttpRequest apiRequest = new RetroHttpRequest(getActivity(),
 *     ApiServiceType.USER_GET_DETAILS,
 *     new ApiCallback() {
 *         @Override
 *         public void onSuccess(ApiResponse apiResponse) {
 *             // Evaluate status code of the response to determine if there's API error
 *         }
 *
 *         @Override
 *         public void onFailed(ApiResponse apiResponse) {
 *                 // Do corresponding event when request failed or client encountered API error
 *             }
 *         });
 * ApiParams params = new ApiParams();
 * params.setUser(userObj);
 * apiRequest.executeHttpRequest(params);
 * </pre>
 */
public class RetroHttpRequest {

    private static final String TAG = RetroHttpRequest.class.getSimpleName();

    public static int TIMEOUT = 15;// default http request timeout in seconds

    private Context mCtx;

    private ApiServiceType mApiServiceType;
    private ApiCallback mApiCallback;
    private RetroHttpResponse mRetroHttpResponse;

    // Determine if Retrofit will be used as synchronous, meaning no Callback to API service
    private boolean mIsSynchronous;

    // Determine if RetroHttpRequest will notify Callback
    // This can be used if Activity has been destroyed while request is in progress
    private boolean mIsDontNotifyCallback;

    private RestAdapter.Builder mRetrofitRestBuilder;
    private HttpErrorHandler mHttpErrorHandler;

    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Create RetroHttpRequest instance to perform http calls.
     * @param ctx The Application or Activity context.
     * @param apiApiServiceType The API service to call.
     * @param apiCallback The Callback of http call execution.
     *
     */
    public RetroHttpRequest(Context ctx, ApiServiceType apiApiServiceType, ApiCallback apiCallback) {
        this.mCtx = ctx;
        this.mApiServiceType = apiApiServiceType;
        this.mApiCallback = apiCallback;
        mHttpErrorHandler = new HttpErrorHandler();

        mRetrofitRestBuilder = new RestAdapter.Builder();
        mRetrofitRestBuilder.setEndpoint(AppConfiguration.HOST);

        // Only enable setErrorHandler when Retrofit is not asynchronous
        //mRetrofitRestBuilder.setErrorHandler(mHttpErrorHandler);

        setRequestTimeout(TIMEOUT);

        if(AppConfiguration.ENABLE_LOG) {
            mRetrofitRestBuilder.setLogLevel(RestAdapter.LogLevel.FULL);
        } else {
            mRetrofitRestBuilder.setLogLevel(RestAdapter.LogLevel.NONE);
        }
    }

    /**
     * Set the timeout of http request in seconds. By default the timeout is 30 seconds.
     * @param requestTimeout Seconds value of timeout of http request.
     */
    public void setRequestTimeout(int requestTimeout) {
        TIMEOUT = requestTimeout;
        /*OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(requestTimeout, TimeUnit.SECONDS);
        httpClient.setReadTimeout(requestTimeout, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(requestTimeout, TimeUnit.SECONDS);
        OkClient client = new OkClient(httpClient);*/

        HttpConnectionClient httpClient = new HttpConnectionClient(TIMEOUT * 1000);
        mRetrofitRestBuilder.setClient(new ApiRequestClient(httpClient));
    }

    /**
     * Calling this will not notify any ApiCallback even if the request has been finished.
     * This can be used when a current RetroHttpRequest is in progress and Activity was destroyed.
     */
    public void dontNotifyCallback() {
        mIsDontNotifyCallback = false;
    }

    /**
     * Execute specific request based on the
     * {@link ApiServiceType}
     * set in the instance of this RetroHttpRequest.
     * @param apiParams HttpRequestParams object containing the needed data in http request.
     */
    public void executeHttpRequest(final ApiParams apiParams) {
        Callback retrofitCallback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                if ( !mIsDontNotifyCallback ) {
                    handleRetrofitResponse(response);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if ( !mIsDontNotifyCallback) {
                    handleRetrofitError(retrofitError);
                }
            }
        };

        Response response = null;
        String responseBody = "(empty)";

        HeaderRequestInterceptor header = new HeaderRequestInterceptor();
        header.addHeaderContentType();
        header.addHeaderDeviceOs();
        header.addHeaderDeviceVersion();
        
        if (mApiServiceType == ApiServiceType.USER_CREATE) {
            mRetrofitRestBuilder.setRequestInterceptor(header);
            User user = apiParams.getUser();
            TypeJsonString requestBody = new TypeJsonString(JsonParser.fromUser(user));
            RetroHttpService apiService = mRetrofitRestBuilder.build().create(RetroHttpService.class);
            apiService.userRegister(requestBody, retrofitCallback);
        } else if (mApiServiceType == ApiServiceType.USER_GET_DETAILS) {
            mRetrofitRestBuilder.setRequestInterceptor(header);
            User user = apiParams.getUser();
            RetroHttpService apiService = mRetrofitRestBuilder.build().create(RetroHttpService.class);
            apiService.userGetDetails(user.getId(), retrofitCallback);
        } else if (mApiServiceType == ApiServiceType.USER_UPDATE_DETAILS) {
            mRetrofitRestBuilder.setRequestInterceptor(header);
            User user = apiParams.getUser();
            TypeJsonString requestBody = new TypeJsonString(JsonParser.fromUser(user));
            RetroHttpService apiService = mRetrofitRestBuilder.build().create(RetroHttpService.class);
            apiService.userUpdateDetails(user.getId(), requestBody, retrofitCallback);
        } 

        // Use this instead of retrofit service interface callback if not asynchronous
        //handleRetrofitResponse(response);
    }

    // handle http response
    private void handleRetrofitResponse(Response response) {
        String responseBody = "(empty)";
        try {
            if(response != null) {
                responseBody = getBodyString(response);
                mApiCallback.onSuccess(initApiResponse(response, responseBody));
            }
        } catch (IOException e) {
            LogMe.e(TAG, "status: " + response.getStatus() + " body: " + responseBody
                    + " ERROR " + e.toString());
            mApiCallback.onFailed(initFailedResponse());
        }
    }

    // handle http error response
    private Throwable handleRetrofitError(RetrofitError cause) {
        LogMe.d(TAG, "handleRetrofitError called");
        Response r = cause.getResponse();
        if(r != null) {
            if(cause.getKind().equals(RetrofitError.Kind.NETWORK)) {
                mRetroHttpResponse = new RetroHttpResponse(HttpStatusCode.REQUEST_TIMEOUT);
            } else if (cause.getKind().equals(RetrofitError.Kind.UNEXPECTED)) {
                mRetroHttpResponse = new RetroHttpResponse(HttpStatusCode.UNEXPECTED_ERROR);
            } else if(cause.getKind().equals(RetrofitError.Kind.HTTP)) {
                try {
                    String body = getBodyString(r);
                    LogMe.d(TAG, "handleRetrofitError status: " + r.getStatus() + " body: " + body);
                    initApiResponse(r, body);
                } catch (Exception e) {
                    LogMe.d(TAG, "handleRetrofitError initApiResponse ERROR " + e.toString());
                }
            } else {
                mRetroHttpResponse = new RetroHttpResponse(HttpStatusCode.UNEXPECTED_ERROR);
            }
        } else if(cause != null && cause.getMessage() != null
                && cause.getMessage().contains("authentication challenge is null")) {
                /*
                 * No need to implement server side change, this workaround for 401
                 * http status code could not be extracted in some device
                 * http://stackoverflow.com/questions/10431202/
                 * java-io-ioexception-received-authentication-challenge-is-null
                 */
                LogMe.w(TAG, "handleRetrofitError Response message: " + cause.getMessage());
                mRetroHttpResponse = new RetroHttpResponse(HttpStatusCode.UNAUTHORIZED);
        } else {
            mRetroHttpResponse = new RetroHttpResponse(HttpStatusCode.REQUEST_TIMEOUT);
        }
        mApiCallback.onFailed(mRetroHttpResponse);
        return cause;
    }

    /*
     * Initialize RetroHttpResponse object to pass to mApiCallback.
     */
    private ApiResponse initApiResponse(Response response, String body) {
        if(response != null) {
            mRetroHttpResponse = new RetroHttpResponse(response, body);
            return mRetroHttpResponse;
        } else {
            LogMe.w(TAG, "initRestResponse response null: " + response + " body: " + body);
        }
        return null;
    }

    /*
     * Initialize failed http request.
     * @return The replaced String body of request to pass to mApiCallback.
     */
    private ApiResponse initFailedResponse() {
        return new RetroHttpResponse(HttpStatusCode.SOCKET_CONNECTION_TIMEOUT);
    }

    // to convert byte stream of Response body of request
    private static String getBodyString(Response response) throws IOException {
        TypedInput body = response.getBody();
        if (body!= null) {
            if (!(body instanceof TypedByteArray)) {
                // Read the entire response body to we can log it and replace the original response
                response = readBodyToBytesIfNecessary(response);
                body = response.getBody();
            }
            byte[] bodyBytes = ((TypedByteArray) body).getBytes();
            String bodyMime = body.mimeType();
            String bodyCharset = MimeUtil.parseCharset(bodyMime, Charset.defaultCharset().name());
            return new String(bodyBytes, bodyCharset);
        }
        return null;
    }

    private static Response readBodyToBytesIfNecessary (Response response) throws IOException {
        TypedInput body = response.getBody();
        if (body == null || body instanceof TypedByteArray) {
            return response;
        }
        String bodyMime = body.mimeType();
        byte[] bodyBytes = streamToBytes(body.in());
        body = new TypedByteArray(bodyMime, bodyBytes);

        return replaceResponseBody(response, body);
    }

    private static Response replaceResponseBody(Response response, TypedInput body) {
        return new Response(response.getUrl(), response.getStatus(),
                response.getReason(), response.getHeaders(), body);
    }

    private static byte[] streamToBytes(InputStream stream) throws IOException {
        int BUFFER_SIZE = 0x1000;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (stream != null) {
            byte[] buf = new byte[BUFFER_SIZE];
            int r;
            while ((r = stream.read(buf)) != -1) {
                baos.write(buf, 0, r);
            }
        }
        return baos.toByteArray();
    }

    /*
     * Http Client used by Retrofit to be able to handle device not connected to
     * network and immediately throw response of connection timeout
     */
    private class ApiRequestClient implements Client {

        private Client client;

        public ApiRequestClient(Client client) {
            this.client = client;
        }

        @Override
        public Response execute(Request request) throws IOException {
            if ( !NetConnUtil.getInstance().hasNetworkConnectivity(mCtx) ) {
                return new Response(request.getUrl(),
                        HttpStatusCode.REQUEST_TIMEOUT.getCode(),
                        "No connectivity",
                        new ArrayList<Header>(),
                        new TypedString(RetroHttpResponse.MSG_NETWORK_TIMEOUT));
            } else {
                return client.execute(request);
            }
        }
    }

    /*
     * Http Client used by Retrofit to be able to handle device not connected to
     * network and immediately throw response of connection timeout
     */
    private class HttpConnectionClient extends UrlConnectionClient {

        private int timeoutMs = 30000;

        public HttpConnectionClient(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        @Override
        protected HttpURLConnection openConnection(Request request) throws IOException {
            HttpURLConnection connection = super.openConnection(request);
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);
            return connection;
        }
    }

    // Add header to http request
    private class HeaderRequestInterceptor implements RequestInterceptor {
        private HashMap<String, String> headers = new HashMap<>();

        // Add key and value to header.
        private void addHeader(String key, String value) {
            headers.put(key, value);
        }

        public void addHeaderContentType() {
            addHeader("Content-Type", "application/json; charset=UTF-8");
        }

        public void addHeaderDeviceOs() {
            addHeader("X-PlaySync-DeviceOS", OsUtil.getOs());
        }

        public void addHeaderDeviceVersion() {
                addHeader("X-PlaySync-DeviceVersion", OsUtil.getAppVersion(mCtx));
        }


        public void addHeaderSession(String sessionToken) {
            addHeader("X-PlaySync-Token", sessionToken);
        }

        @Override
        public void intercept(RequestFacade request) {
            for(Map.Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
                LogMe.d(TAG, "HeaderRequestInterceptor intercept " +
                        "header name: " + entry.getKey() + " header value: " + entry.getValue());
            }
        }
    }

    /*
    * Set error handler of retrofit http client when used as not asynchronous.
    * For more details see
    * http://blog.robinchutaux.com/blog/a-smart-way-to-use-retrofit/ and
    * http://square.github.io/retrofit/
    */
    private class HttpErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError cause) {
            LogMe.d(TAG, "HttpErrorHandler handleError called.");
            return handleRetrofitError(cause);
        }
    }

}
