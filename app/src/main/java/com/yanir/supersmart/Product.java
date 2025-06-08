package com.yanir.supersmart;

/**
 * Represents a product in the SuperSmart application.
 * Contains information such as name, barcode, description, and price.
 * Used throughout the app to store and retrieve product data.
 */
public class Product {
    // Product name
    private String name;

    // Product barcode (identifier)
    private String barcode;

    // Product description
    private String description;

    // Product price
    private float price;

    /**
     * Constructs a new Product with the specified values.
     *
     * @param name        the name of the product
     * @param barcode     the barcode identifier
     * @param description the description of the product
     * @param price       the price of the product
     */
    public Product(String name, String barcode, String description, float price) {
        this.name = name;
        this.barcode = barcode;
        this.description = description;
        this.price = price;
    }

    /**
     * Constructs an empty Product with default values.
     */
    public Product() {
        this.name = "";
        this.barcode = "";
        this.description = "";
        this.price = 0;
    }

    /**
     * Returns the name of the product.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the barcode of the product.
     *
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Returns the description of the product.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the price of the product.
     *
     * @return the price
     */
    public float getPrice() {
        return price;
    }

    /**
     * Sets the name of the product.
     *
     * @param name the new name value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the barcode of the product.
     *
     * @param barcode the new barcode value
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * Sets the description of the product.
     *
     * @param description the new description value
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the price of the product.
     *
     * @param price the new price value
     */
    public void setPrice(float price) {
        this.price = price;
    }

    /**
     * Returns a string representation of the product object.
     *
     * @return a formatted string with product details
     */
    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", barcode='" + barcode + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }


}
