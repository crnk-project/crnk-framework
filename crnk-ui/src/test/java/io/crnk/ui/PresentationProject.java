package io.crnk.ui;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.ui.presentation.annotation.PresentationFullTextSearchable;
import io.crnk.ui.presentation.annotation.PresentationLabel;

@JsonApiResource(type = "presentationProject")
public class PresentationProject {

	@JsonApiId
	private Long id;

	@PresentationFullTextSearchable
	@PresentationLabel
	private String name;

	private String description;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
