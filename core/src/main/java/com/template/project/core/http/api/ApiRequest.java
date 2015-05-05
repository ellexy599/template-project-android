package com.template.project.core.http.api;

import com.squareup.okhttp.OkHttpClient;
import com.template.project.core.AppConfiguration;
import com.template.project.core.entity.User;
import com.template.project.core.http.HttpParams;
import com.template.project.core.http.HttpResponse;
import com.template.project.core.http.json.ApiServiceType;
import com.template.project.core.utils.LogMe;

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
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.client.UrlConnectionClient;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedString;

public class ApiRequest {

    private static final String TAG = ApiRequest.class.getSimpleName();

    public static int TIMEOUT = 10000;// default timeout

    private ApiServiceType apiServiceType;
    private ApiCallback apiCallback;
    private ApiResponse apiResponse;
    private boolean mIsAsynchronous;

    private RestAdapter.Builder retrofitRestBuilder;
    private HttpErrorHandler httpErrorHandler;
    private OkHttpClient httpClient;

    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Create ApiRequest instance to perform http calls.
     * @param apiApiServiceType The API service to call.
     * @param apiCallback The Callback of http call execution.
     *
     */
    public ApiRequest(ApiServiceType apiApiServiceType, ApiCallback apiCallback) {
        this.apiServiceType = apiApiServiceType;
        this.apiCallback = apiCallback;
        httpErrorHandler = new HttpErrorHandler();
        httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        httpClient.setReadTimeout(30, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(30, TimeUnit.SECONDS);
        retrofitRestBuilder = new RestAdapter.Builder()
                .setClient(new OkClient(httpClient))
                .setEndpoint(AppConfiguration.HOST)
                .setErrorHandler(httpErrorHandler);
        if(AppConfiguration.ENABLE_LOG) {
            retrofitRestBuilder.setLogLevel(RestAdapter.LogLevel.FULL);
        } else {
            retrofitRestBuilder.setLogLevel(RestAdapter.LogLevel.NONE);
        }
    }

    /**
     * Set the timeout of http request in millisecond. By default the timeout is 10000ms.
     * @param requestTimeout Millisecond value of timeout of http request.
     */
    public void setRequestTimeout(int requestTimeout) {
        if (requestTimeout > TIMEOUT) {
            TIMEOUT = requestTimeout;
            retrofitRestBuilder.setClient(new HttpConnectionClient(TIMEOUT));
        }
    }

    /**
     * Execute specific request based on the
     * {@link ApiServiceType}
     * set in the instance of this HttpRequest.
     * @param httpParams HttpRequestParams object containing the needed data in http request.
     */
    public void executeHttpRequest(HttpParams httpParams) {
        Callback retrofitCallback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                handleRetrofitResponse(response);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                handleRetrofitError(retrofitError);
            }
        };
        HeaderRequestInterceptor header = new HeaderRequestInterceptor();
        Response response = null;
        String responseBody = "(empty)";
        if (apiServiceType == ApiServiceType.USER_REGISTER) {
            //header.addAccessToken("my access token value");
            //header.addHeader("Set-Cookie or any header key name", "the header value");
            retrofitRestBuilder.setRequestInterceptor(header);
            ApiService apiService = retrofitRestBuilder.build().create(ApiService.class);
            User user = httpParams.getUser();
            //response = apiService.register(user.getEmail(), user.getPassword());
            apiService.register(user.getEmail(), user.getPassword(), retrofitCallback);
        } else if (apiServiceType == ApiServiceType.USER_UPDATE) {
            retrofitRestBuilder.setRequestInterceptor(header);
            ApiService apiService = retrofitRestBuilder.build().create(ApiService.class);
            User user = httpParams.getUser();
            TypedString partEmail = new TypedString(user.getEmail());
            TypedString partPassword = new TypedString(user.getPassword());
            TypedFile partPhoto = new TypedFile("image/*", new File(""));
            apiService.updateUser(partEmail, partPassword, partPhoto, retrofitCallback);
        } else if(apiServiceType == ApiServiceType.USER_GET_DETAILS) {
            retrofitRestBuilder.setRequestInterceptor(header);
            ApiService apiService = retrofitRestBuilder.build().create(ApiService.class);
            User user = httpParams.getUser();
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
                apiCallback.onSuccess(initApiResponse(response, responseBody));
            }
        } catch (IOException e) {
            apiCallback.onFailed(initFailedResponse());
            LogMe.e(TAG, "status: " + response.getStatus() + " body: " + responseBody
                        + " ERROR " + e.toString());
        }
    }

    // handle http error reponse
    private Throwable handleRetrofitError(RetrofitError cause) {
        Response r = cause.getResponse();
        if(r != null) {
            if(cause.getKind().equals(RetrofitError.Kind.NETWORK)) {
                apiResponse = new HttpResponse(ApiStatusCode.SOCKET_CONNECTION_TIMEOUT);
            } else if(cause.getKind().equals(RetrofitError.Kind.HTTP)) {
                try {
                    String body = getBodyString(r);
                    LogMe.d(TAG, "handleError status: " + r.getStatus() + " body: " + body);
                    initApiResponse(r, body);
                } catch (Exception e) {
                    LogMe.d(TAG, "handleError initApiResponse ERROR " + e.toString());
                }
            } else {
                apiResponse = new HttpResponse(ApiStatusCode.UNEXPECTED_ERROR);
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
                apiResponse = new HttpResponse(ApiStatusCode.UNAUTHORIZED);
            }
            LogMe.e(TAG, "handleError Response is null");
        }
        apiCallback.onFailed(apiResponse);
        return cause;
    }

    /*
     * Initialize ApiResponse object to pass to apiCallback.
     */
    private ApiResponse initApiResponse(Response response, String body) {
        if(response != null) {
            apiResponse = new HttpResponse(response, body);
            return apiResponse;
        } else {
            LogMe.w(TAG, "initRestResponse response null: " + response + " body: " + body);
        }
        return null;
    }

    /*
     * Initialize failed http request.
     * @return The replaced String body of request to pass to apiCallback.
     */
    private ApiResponse initFailedResponse() {
        return new HttpResponse(ApiStatusCode.SOCKET_CONNECTION_TIMEOUT);
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

    /**
     * Http request apiCallback.
     */
    public static interface ApiCallback {
        /**
         * Event listener once http request finished.
         * @param apiResponse The String http response body.
         */
        void onSuccess(ApiResponse apiResponse);

        /**
         * Event listener once http request failed.
         * @param apiResponse The String http response body.
         */
        void onFailed(ApiResponse apiResponse);
    }

    // add header to http request
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

    // custom UrlConnectionClient for Retrofit client to be able to set timeout
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
    * set error handler of retrofit http client. For more details see
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
