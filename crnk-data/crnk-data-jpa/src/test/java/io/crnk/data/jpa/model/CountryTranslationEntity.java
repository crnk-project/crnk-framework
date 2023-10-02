package io.crnk.data.jpa.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

@Entity
public class CountryTranslationEntity {

	@EmbeddedId
	private CountryTranslationPK countryTranslationPk;

	@NotNull
	private String txt;

	public String getTxt() {
		return txt;
	}

	public void setTxt(String txt) {
		this.txt = txt;
	}

	public CountryTranslationPK getCountryTranslationPk() {
		return countryTranslationPk;
	}

	public void setCountryTranslationPk(CountryTranslationPK countryTranslationPk) {
		this.countryTranslationPk = countryTranslationPk;
	}
}
