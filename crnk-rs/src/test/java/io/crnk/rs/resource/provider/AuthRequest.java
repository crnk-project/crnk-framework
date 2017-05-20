package io.crnk.rs.resource.provider;

public class AuthRequest {

	private String authType;
	private String value;

	public AuthRequest(String authType, String value) {
		this.authType = authType;
		this.value = value;
	}

	public static AuthRequest fromAuthorizationHeader(String authorizationHeaderValue) {
		String[] authComponents = authorizationHeaderValue.trim().split(" ");

		return new AuthRequest(authComponents[0], authComponents[1]);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AuthRequest that = (AuthRequest) o;

		return this.authType.equals(that.authType)
				&& this.value.equals(that.value);
	}
}
