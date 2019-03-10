package test;

import io.crnk.core.resource.annotations.JsonApiEmbeddable;

@JsonApiEmbeddable
public class UserAddress {

    private String city;

    private String street;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}

