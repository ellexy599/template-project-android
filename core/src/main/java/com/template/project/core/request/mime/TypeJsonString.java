package com.template.project.core.request.mime;

import retrofit.mime.TypedString;

/**
 * Type of Http POST body to passed. Used this to avoid passing
 * raw Binary data of JSON String.
 */
public class TypeJsonString extends TypedString {

    public TypeJsonString(String string) {
        super(string);
    }

    @Override public String mimeType() {
        return "application/json";
    }
}
