package io.crnk.gen.typescript.model.writer;

public class TSCodeStyle {

	private String indentation = "\t";

	private String lineSeparator = "\n";

	private boolean stringEnums = true;

	public String getIndentation() {
		return indentation;
	}

	public void setIndentation(String indentation) {
		this.indentation = indentation;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	public boolean isStringEnums() {
		return stringEnums;
	}

	public void setStringEnums(boolean stringEnums) {
		this.stringEnums = stringEnums;
	}
}
