package com.template.project.core.http.api;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.template.project.core.AppConfiguration;
import com.template.project.core.entity.User;
import com.template.project.core.http.HttpCallback;
import com.template.project.core.http.api.ApiService;
import com.template.project.core.http.api.ApiServiceType;
import com.template.project.core.http.HttpStatusCode;
import com.template.project.core.http.HttpResponse;
import com.template.project.core.utils.LogMe;
import com.template.project.core.utils.conn.NetConnUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.client.UrlConnectionClient;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedString;

/**
 * The http request calling API services. This is used with Retrofit
 * and OkHttpClient for calling restful APIs.
 */
public class ApiRequest {

    private static final String TAG = ApiRequest.class.getSimpleName();

    public static int TIMEOUT = 30;// default http request timeout in seconds

    private Context mCtx;

    private ApiServiceType mApiServiceType;
    private HttpCallback mHttpCallback;
    private ApiResponse mApiResponse;

    // Determine if Retrofit will be used as synchronous, meaning no Callback to API service
    private boolean mIsSynchronous;

    // Determine if ApiRequest will notify Callback
    // This can be used if Activity has been destroyed while request is in progress
    private boolean mIsDontNotifyCallback;

    private RestAdapter.Builder mRetrofitRestBuilder;
    private HttpErrorHandler mHttpErrorHandler;

    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Create ApiRequest instance to perform http calls.
     * @param ctx The Application or Activity context.
     * @param apiApiServiceType The API service to call.
     * @param httpCallback The Callback of http call execution.
     *
     */
    public ApiRequest(Context ctx, ApiServiceType apiApiServiceType, HttpCallback httpCallback) {
        this.mCtx = ctx;
        this.mApiServiceType = apiApiServiceType;
        this.mHttpCallback = httpCallback;
        mHttpErrorHandler = new HttpErrorHandler();

        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
        httpClient.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(TIMEOUT, TimeUnit.SECONDS);
        OkClient client = new OkClient(httpClient);

        mRetrofitRestBuilder = new RestAdapter.Builder()
                .setClient(new ApiRequestClient(client))
                .setEndpoint(AppConfiguration.HOST)
                .setErrorHandler(mHttpErrorHandler);

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
        if (requestTimeout > TIMEOUT) {
            TIMEOUT = requestTimeout;
            OkHttpClient httpClient = new OkHttpClient();
            httpClient.setConnectTimeout(requestTimeout, TimeUnit.SECONDS);
            httpClient.setReadTimeout(requestTimeout, TimeUnit.SECONDS);
            httpClient.setWriteTimeout(requestTimeout, TimeUnit.SECONDS);
            OkClient client = new OkClient(httpClient);
            mRetrofitRestBuilder.setClient(client);
            // mRetrofitRestBuilder.setClient(new HttpConnectionClient(TIMEOUT));
        }
    }

    /**
     * Calling this will not notify any ApiCallback even if the request has been finished.
     * This can be used when a current ApiRequest is in progress and Activity was destroyed.
     */
    public void dontNotifyCallback() {
        mIsDontNotifyCallback = false;
    }

    /**
     * Execute specific request based on the
     * {@link ApiServiceType}
     * set in the instance of this HttpRequest.
     * @param apiParams HttpRequestParams object containing the needed data in http request.
     */
    public void executeHttpRequest(ApiParams apiParams) {
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

        HeaderRequestInterceptor header = new HeaderRequestInterceptor();
        Response response = null;
        String responseBody = "(empty)";

        if (mApiServiceType == ApiServiceType.USER_REGISTER) {
            //header.addAccessToken("my access token value");
            //header.addHeader("Set-Cookie or any header key name", "the header value");
            mRetrofitRestBuilder.setRequestInterceptor(header);
            ApiService apiService = mRetrofitRestBuilder.build().create(ApiService.class);
            User user = apiParams.getUser();
            //response = apiService.register(user.getEmail(), user.getPassword());
            apiService.register(user.getEmail(), user.getPassword(), retrofitCallback);
        } else if (mApiServiceType == ApiServiceType.USER_UPDATE) {
            mRetrofitRestBuilder.setRequestInterceptor(header);
            ApiService apiService = mRetrofitRestBuilder.build().create(ApiService.class);
            User user = apiParams.getUser();
            TypedString partEmail = new TypedString(user.getEmail());
            TypedString partPassword = new TypedString(user.getPassword());
            TypedFile partPhoto = new TypedFile("image/*", new File(""));
            apiService.updateUser(partEmail, partPassword, partPhoto, retrofitCallback);
        } else if(mApiServiceType == ApiServiceType.USER_GET_DETAILS) {
            mRetrofitRestBuilder.setRequestInterceptor(header);
            ApiService apiService = mRetrofitRestBuilder.build().create(ApiService.class);
            User user = apiParams.getUser();
            apiService.getUserDetails(user.getEmail(), retrofitCallback);
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
                mHttpCallback.onSuccess(initApiResponse(response, responseBody));
            }
        } catch (IOException e) {
            mHttpCallback.onFailed(initFailedResponse());
            LogMe.e(TAG, "status: " + response.getStatus() + " body: " + responseBody
                        + " ERROR " + e.toString());
        }
    }

    // handle http error response
    private Throwable handleRetrofitError(RetrofitError cause) {
        Response r = cause.getResponse();
        if(r != null) {
            if(cause.getKind().equals(RetrofitError.Kind.NETWORK)) {
                mApiResponse = new ApiResponse(HttpStatusCode.SOCKET_CONNECTION_TIMEOUT);
            } else if(cause.getKind().equals(RetrofitError.Kind.HTTP)) {
                try {
                    String body = getBodyString(r);
                    LogMe.d(TAG, "handleError status: " + r.getStatus() + " body: " + body);
                    initApiResponse(r, body);
                } catch (Exception e) {
                    LogMe.d(TAG, "handleError initApiResponse ERROR " + e.toString());
                }
            } else {
                mApiResponse = new ApiResponse(HttpStatusCode.UNEXPECTED_ERROR);
            }
        } else {
                /*
                 * No need to implement server side change, this workaround for 401
                 * http status code could not be extracted in some device
                 * http://stackoverflow.com/questions/10431202/
                 * java-io-ioexception-received-authentication-challenge-is-null
                 */
            if(cause != null && cause.getMessage() != null
                    && cause.getMessage().contains("authentication challenge is null")) {
                LogMe.w(TAG, "handleError Response message: " + cause.getMessage());
                mApiResponse = new ApiResponse(HttpStatusCode.UNAUTHORIZED);
            }
            LogMe.e(TAG, "handleError Response is null");
        }
        mHttpCallback.onFailed(mApiResponse);
        return cause;
    }

    /*
     * Initialize ApiResponse object to pass to mApiCallback.
     */
    private HttpResponse initApiResponse(Response response, String body) {
        if(response != null) {
            mApiResponse = new ApiResponse(response, body);
            return mApiResponse;
        } else {
            LogMe.w(TAG, "initRestResponse response null: " + response + " body: " + body);
        }
        return null;
    }

    /*
     * Initialize failed http request.
     * @return The replaced String body of request to pass to mApiCallback.
     */
    private HttpResponse initFailedResponse() {
        return new ApiResponse(HttpStatusCode.SOCKET_CONNECTION_TIMEOUT);
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

    /* Http Client used by Retrofit to be able to handle device not connected to
       network and immediately throw reponse of connection timeout */
    private class ApiRequestClient implements Client {

        private Client client;

        public ApiRequestClient(Client client) {
            this.client = client;
        }

        @Override
        public Response execute(Request request) throws IOException {
            if ( !NetConnUtil.getInstance().hasNetworkConnectivity(mCtx) ) {
                // return REQUEST_TIMEOUT http status error if no network connectivity
                return new Response(request.getUrl(), HttpStatusCode.REQUEST_TIMEOUT.getCode(),
                        null, null, null);
            } else {
                return client.execute(request);
            }
        }
    }

    // Add header to http request
    private class HeaderRequestInterceptor implements RequestInterceptor {
        private HashMap<String, String> headers = new HashMap<>();

        /** Add key and value to header. */
        private void addHeader(String key, String value) {
            headers.put(key, value);
        }

        private void addAccessToken(String accessToken) {
            addHeader(HEADER_AUTHORIZATION, accessToken);
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

    // Custom UrlConnectionClient for Retrofit client to be able to set timeout
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

    /*
    * Set error handler of retrofit http client. For more details see
    * http://blog.robinchutaux.com/blog/a-smart-way-to-use-retrofit/ and
    * http://square.github.io/retrofit/
    */
    private class HttpErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError cause) {
            return handleRetrofitError(cause);
        }
    }

}
