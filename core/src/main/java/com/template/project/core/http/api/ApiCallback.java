package com.template.project.core.http.api;

/**
 * Http request apiCallback. Event Bus can be implemented with events of this callback.
 */
public interface ApiCallback {

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
