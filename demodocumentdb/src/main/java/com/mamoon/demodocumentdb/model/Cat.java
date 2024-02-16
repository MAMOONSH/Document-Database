package com.mamoon.demodocumentdb.model;

import java.util.Objects;

public class Cat {
    private String _id;
    private String furLevel;
    private String color;
    private String gender;
    private String price;

    public Cat() {
    }

    public Cat(String furLevel, String color, String gender, String price) {
        this.furLevel = furLevel;
        this.color = color;
        this.gender = gender;
        this.price = price;
    }


    public String getFurLevel() {
        return furLevel;
    }

    public void setFurLevel(String furLevel) {
        this.furLevel = furLevel;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(furLevel, color, gender, price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cat cat = (Cat) o;
        return Objects.equals(cat.price, price) && Objects.equals(furLevel, cat.furLevel) && Objects.equals(color, cat.color) && Objects.equals(gender, cat.gender);
    }

    @Override
    public String toString() {
        return "Cat{" +
                "furLevel='" + furLevel + '\'' +
                ", color='" + color + '\'' +
                ", gender='" + gender + '\'' +
                ", price=" + price +
                '}';
    }
}
