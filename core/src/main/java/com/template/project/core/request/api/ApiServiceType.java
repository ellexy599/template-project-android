package com.template.project.core.request.api;

/** Type of API Service of the http request */
public enum ApiServiceType {

    USER_CREATE("user_create"),
    USER_GET_DETAILS("user_get_details"),
    USER_UPDATE_DETAILS("user_update_details");

    private final String description;

    private ApiServiceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ApiServiceType fromDescription(String description) {
        for (ApiServiceType gender : ApiServiceType.values()) {
            if (gender.getDescription().equalsIgnoreCase(description))
                return gender;
        }
        return null;
    }
    
}
