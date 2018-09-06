package com.example.jennifers.jenniferspark;

/**
 * Created by Jennifer's Team on 10/4/2017.
 * User object to store current user
 */

public class User {
    /**
     * User's name
     */
    private String name;
    /**
     * User's email
     */
    private String email;
    /**
     * User's administration permission
     */
    private int isAdmin;

    public User(){
        name ="";
        email="";
        isAdmin=0;

    }
    /**
     * User constructor with preset name and administration permission
     *
     * @param name    name of user
     * @param isAdmin administration permission
     */
    public User(String name, String email, int isAdmin) {
        this.name = name;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    /**
     * Get name value of current user
     *
     * @return name of current user
     */
    public String getName() {
        return name;
    }

    /**
     * Get administration permission of current user
     *
     * @return administration permission of current user
     */
    public int getIsAdmin() {
        return isAdmin;
    }

    /**
     * Get email of current user
     *
     * @return email of current user
     */
    public String getEmail() {
        return email;
    }

}
