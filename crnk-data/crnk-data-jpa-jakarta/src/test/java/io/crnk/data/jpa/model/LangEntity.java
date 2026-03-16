package io.crnk.data.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LangEntity {

	@Id
	@Column(name = "lang_cd", insertable = false, updatable = false)
	private String langCd;

	@Column(name = "ctl_act_cd", insertable = false, updatable = false)
	private Boolean ctlActCd;

	public String getLangCd() {
		return langCd;
	}

	public void setLangCd(String langCd) {
		this.langCd = langCd;
	}
}
