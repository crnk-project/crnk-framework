package io.crnk.gen.typescript.writer;

public class TSCodeStyle {

	private String indentation = "\t";

	private String lineSeparator = "\n";

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
}
