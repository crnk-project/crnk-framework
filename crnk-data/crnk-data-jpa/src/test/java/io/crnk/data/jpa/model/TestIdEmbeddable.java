package io.crnk.data.jpa.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@JsonSerialize(using = ToStringSerializer.class)
public class TestIdEmbeddable implements Serializable {

    public static final String ATTR_embIntValue = "embIntValue";

    public static final String ATTR_embStringValue = "embStringValue";

    private static final long serialVersionUID = 4473954915317129238L;

    @Column
    private Integer embIntValue;

    @Column
    private String embStringValue;

    @Column
    private boolean embBooleanValue;

    public TestIdEmbeddable() {
    }

    public TestIdEmbeddable(String idString) {
        String[] elements = idString.split("\\-");
        embIntValue = Integer.parseInt(elements[0]);
        embStringValue = elements[1];
        embBooleanValue = Boolean.parseBoolean(elements[2]);
    }

    public TestIdEmbeddable(Integer intValue, String stringValue, boolean embBooleanValue) {
        this.embIntValue = intValue;
        this.embStringValue = stringValue;
        this.embBooleanValue = embBooleanValue;
    }

    public Integer getEmbIntValue() {
        return embIntValue;
    }

    public void setEmbIntValue(Integer embIntValue) {
        this.embIntValue = embIntValue;
    }

    public boolean isEmbBooleanValue() {
        return embBooleanValue;
    }

    public void setEmbBooleanValue(boolean embBooleanValue) {
        this.embBooleanValue = embBooleanValue;
    }

    public String getEmbStringValue() {
        return embStringValue;
    }

    public void setEmbStringValue(String embStringValue) {
        this.embStringValue = embStringValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestIdEmbeddable that = (TestIdEmbeddable) o;
        return embBooleanValue == that.embBooleanValue &&
                Objects.equals(embIntValue, that.embIntValue) &&
                Objects.equals(embStringValue, that.embStringValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(embIntValue, embStringValue, embBooleanValue);
    }
}
