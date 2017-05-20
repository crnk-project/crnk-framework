package io.crnk.jpa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
public class CountryEntity {

	@Id
	@Column(name = "country_cd", insertable = false, updatable = false)
	private String countryCd;

	@Column(name = "ctl_act_cd")
	@NotNull
	private Boolean ctlActCd;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "countryTranslationPk.country", cascade = CascadeType.PERSIST)
	@JsonIgnore
	private List<CountryTranslationEntity> translations;

	public String getCountryCd() {
		return countryCd;
	}

	public void setCountryCd(String countryCd) {
		this.countryCd = countryCd;
	}

	public Boolean getCtlActCd() {
		return ctlActCd;
	}

	public void setCtlActCd(Boolean ctlActCd) {
		this.ctlActCd = ctlActCd;
	}

	public List<CountryTranslationEntity> getTranslations() {
		return translations;
	}

	public void setTranslations(List<CountryTranslationEntity> translations) {
		this.translations = translations;
	}
}
