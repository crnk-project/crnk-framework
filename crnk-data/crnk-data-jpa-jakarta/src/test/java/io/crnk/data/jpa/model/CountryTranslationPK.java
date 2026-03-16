package io.crnk.data.jpa.model;

import com.google.common.base.Objects;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class CountryTranslationPK implements Serializable {

	private static final long serialVersionUID = -2786733635182897172L;

	@ManyToOne
	@JoinColumn(name = "country_cd", insertable = false, updatable = false)
	private CountryEntity country;

	@ManyToOne
	@JoinColumn(name = "lang_cd", insertable = false, updatable = false)
	private LangEntity lang;

	public CountryEntity getCountry() {
		return country;
	}

	public void setCountry(CountryEntity country) {
		this.country = country;
	}

	public LangEntity getLang() {
		return lang;
	}

	public void setLang(LangEntity lang) {
		this.lang = lang;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CountryTranslationPK other = (CountryTranslationPK) obj;
		return Objects.equal(other.lang, lang) && Objects.equal(other.country, country);
	}

}
