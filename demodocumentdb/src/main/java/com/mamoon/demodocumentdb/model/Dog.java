package com.mamoon.demodocumentdb.model;

import java.util.Objects;

public class Dog {
    private String _id;
    private String kind;
    private String color;
    private String gender;
    private String price;

    public Dog() {
    }

    public Dog(String kind, String color, String gender, String price) {
        this.kind = kind;
        this.color = color;
        this.gender = gender;
        this.price = price;
    }

    public Dog(String _id, String kind, String color, String gender, String price) {
        this._id = _id;
        this.kind = kind;
        this.color = color;
        this.gender = gender;
        this.price = price;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
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
        return Objects.hash(kind, color, gender, price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dog dog = (Dog) o;
        return Objects.equals(dog.price, price) && Objects.equals(kind, dog.kind) && Objects.equals(color, dog.color) && Objects.equals(gender, dog.gender);
    }

    @Override
    public String toString() {
        return "Dog{" +
                "kind='" + kind + '\'' +
                ", color='" + color + '\'' +
                ", gender='" + gender + '\'' +
                ", price=" + price +
                '}';
    }
}
