package com.template.project.core.http.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.template.project.core.entity.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser of JSON String and Entity classes.
 */
public class JsonParser {

    private static Gson getDefaultGson() {
        return new GsonBuilder().create();
    }

    /**
     * Parse JSON String value to User object.
     * @param jsonString The JSON String value to parse.
     * @return Object of User of parsed JSON String value.
     */
    public static User toUser(String jsonString) {
        return getDefaultGson().fromJson(jsonString, User.class);
    }

    /**
     * Parse User object instance to JSON String value.
     * @param userObj The User object instance to parse.
     * @return The JSON String value of parsed User object.
     */
    public static String fromUser(User userObj) {
        return getDefaultGson().toJson(userObj, User.class);
    }

    /**
     * Parse JSON String value to List of User.
     * @param jsonString The JSON String value to parse.
     * @return Object List of User of parsed JSON String value.
     */
    public static ArrayList<User> toUserList(String jsonString) {
        Type listType = new TypeToken<List<User>>() {}.getType();
        ArrayList<User> arrProgrammeList = getDefaultGson().fromJson(jsonString, listType);
        return arrProgrammeList;
    }

}
