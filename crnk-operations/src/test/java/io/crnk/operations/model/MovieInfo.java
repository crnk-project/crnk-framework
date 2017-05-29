package io.crnk.operations.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;


@MappedSuperclass
public abstract class MovieInfo implements Serializable {

	@Version
	private Long version;

	private String imdbId;

	@Column(length = 500, nullable = false)
	private String title;

	private int year;

	private String rating;

	@Max(100)
	@Min(0)
	private Integer metacritic;

	@DecimalMax("10.00")
	@DecimalMin("0.00")
	private BigDecimal imdbRating;

	private Long imdbVotes;

	@Column(length = 1000)
	private String plot;

	@Lob
	private String fullPlot;

	private String awards;

	private boolean closingCredits;

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getImdbId() {
		return imdbId;
	}

	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public Integer getMetacritic() {
		return metacritic;
	}

	public void setMetacritic(Integer metacritic) {
		this.metacritic = metacritic;
	}

	public BigDecimal getImdbRating() {
		return imdbRating;
	}

	public void setImdbRating(BigDecimal imdbRating) {
		this.imdbRating = imdbRating;
	}

	public Long getImdbVotes() {
		return imdbVotes;
	}

	public void setImdbVotes(Long imdbVotes) {
		this.imdbVotes = imdbVotes;
	}

	public String getPlot() {
		return plot;
	}

	public void setPlot(String plot) {
		this.plot = plot;
	}

	public String getFullPlot() {
		return fullPlot;
	}

	public void setFullPlot(String fullPlot) {
		this.fullPlot = fullPlot;
	}

	public String getAwards() {
		return awards;
	}

	public void setAwards(String awards) {
		this.awards = awards;
	}

	public boolean getClosingCredits() {
		return closingCredits;
	}

	public void setClosingCredits(boolean closingCredits) {
		this.closingCredits = closingCredits;
	}
}
