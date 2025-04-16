package com.yanir.supersmart;

public class Product {
    private String name;
    private String barcode;
    private String description;
    private float price;

    public Product(String name, String barcode, String description, float price) {
        this.name = name;
        this.barcode = barcode;
        this.description = description;
        this.price = price;
    }

    //default constructor
    public Product() {
        this.name = "";
        this.barcode = "";
        this.description = "";
        this.price = 0;
    }

    // getters

    public String getName() {
        return name;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDescription() {
        return description;
    }

    public float getPrice() {
        return price;
    }

    // setters

    public void setName(String name) {
        this.name = name;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(float price) {
        this.price = price;
    }


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
