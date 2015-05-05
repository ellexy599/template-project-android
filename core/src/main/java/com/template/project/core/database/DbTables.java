package com.template.project.core.database;

/*
 * Table names of the application database
 */
public enum DbTables {

    TBL_USER("user_tbl");

    private final String tableName;

    private DbTables(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return this.tableName;
    }
}
