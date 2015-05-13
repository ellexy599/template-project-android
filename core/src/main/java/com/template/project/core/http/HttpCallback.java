package com.template.project.core.http;

/**
 * Http request apiCallback. Event Bus can be implemented with events of this callback.
 */
public interface HttpCallback {

    /**
     * Event listener once http request finished.
     * @param httpResponse The String http response body.
     */
    void onSuccess(HttpResponse httpResponse);

    /**
     * Event listener once http request failed.
     * @param httpResponse The String http response body.
     */
    void onFailed(HttpResponse httpResponse);

}
