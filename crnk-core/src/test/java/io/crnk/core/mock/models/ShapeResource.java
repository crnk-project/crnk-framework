package io.crnk.core.mock.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "shapes")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class ShapeResource extends AbstractResource<ShapeResource.Shape> {

	public ShapeResource(Shape delegate) {
		super(delegate);
	}

	public String getType() {
		return getDelegate().getType();
	}

	public static class Shape implements Identifiable<String> {

		private String id;

		private String type;

		public Shape(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}
}