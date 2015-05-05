package com.template.project.core.database;

/**
 * Column names of tables of application database
 */
public enum DbColumn {

    COL_USER_ID("user_id"),
    COL_USER_EMAIL("email"),
    COL_USER_PASSWORD("password");

    private final String columnName;

    private DbColumn(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return this.columnName;
    }
}
