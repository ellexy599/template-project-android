package com.template.project.core;

/**
 * Application configuration. Set the host url of the API Server,
 * Google API Project Number, and Log enabling.
 */
public class AppConfiguration {

    // Default configuration values, this can be replace by the app module using this core library

    public static final String HOST = "http://192.168.254.254";
    public static final String HOST_VERSION = "v1";
    public static final String HOST_API_ROOT_URI = "/api/";

    public static final String GOOGLE_API_PROJ_NUMBER = "your google project api number for maps, gcm";
    public static boolean ENABLE_LOG = true;

}
