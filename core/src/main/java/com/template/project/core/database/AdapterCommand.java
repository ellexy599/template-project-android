package com.template.project.core.database;

import com.template.project.core.entity.User;

import java.util.ArrayList;

/**
 * Adapter of query command with database.
 */
public interface AdapterCommand {

    /**
     * Add User to database record or update details if already existing.
     * @param user The User object containing the details.
     */
    public void addOrUpdateUser(User user);

    /** Get User details based on email. */
    public User getUser(String email);

    /** Return all User in database record. */
    public ArrayList<User> getUsers();

    /**
     * Delete record of User in the database.
     * @param user The User containing the details of identifier for deletion of record.
     */
    public void deleteUser(User user);

}
