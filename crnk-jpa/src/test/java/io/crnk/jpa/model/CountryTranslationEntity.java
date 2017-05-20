package io.crnk.jpa.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

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
