package com.example.jennifers.jenniferspark;

/**
 * Created by Jennifer's Team on 10/4/2017.
 * Parking object to store information of a parking lot
 */

public class Parking {
    /**
     * Parking lot title
     */
    private String title;
    /**
     * Parking lot address
     */
    private String address;
    /**
     * Parking lot city
     */
    private String city;
    /**
     * Parking lot state
     */
    private String state;
    /**
     * Parking lot zipcode
     */
    private String zipcode;
    /**
     * Parking lot description
     */
    private String description;

    /**
     * Default Construction
     */
    public Parking() {
        title = "";
        address = "";
        city = "";
        state = "";
        zipcode = "";
        description = "";
    }

    /**
     * Parking Construction
     * Create new parking lot object with preset data
     *
     * @param title       Parking title
     * @param address     Parking address
     * @param city        Parking city
     * @param state       Parking state
     * @param zipcode     Parking zipcode
     * @param description Parking description
     */
    public Parking(String title, String address, String city, String state, String zipcode, String description) {
        this.title = title;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipcode = zipcode;
        this.description = description;
    }

    /**
     * Return parking title
     *
     * @return parking title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set new title for a parking lot
     *
     * @param title new parking title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return parking address
     *
     * @return parking address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set new address for a parking lot
     *
     * @param address new parking address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Return parking city
     *
     * @return parking city
     */
    public String getCity() {
        return city;
    }

    /**
     * Set new city for a parking lot
     *
     * @param city new parking city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Return parking state
     *
     * @return parking state
     */
    public String getState() {
        return state;
    }

    /**
     * Set new address for a parking state
     *
     * @param state new parking state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Return parking zipcode
     *
     * @return parking zipcode
     */
    public String getZipcode() {
        return zipcode;
    }

    /**
     * Set new zipcode for a parking lot
     *
     * @param zipcode new parking zipcode
     */
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    /**
     * Return parking description
     *
     * @return parking description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set new description for a parking lot
     *
     * @param description new parking address
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return address + " " + city + " " + state + " " + zipcode;
    }
}
