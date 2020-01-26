package io.crnk.gen.asciidoc.internal;

import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsciidocUtils {

	public static String fromHtml(String html) {
		if (html == null) {
			return "";
		}

		String text = html;
		text = text.replace("<b>", "*");
		text = text.replace("</b>", "*");
		text = text.replace("<i>", "_");
		text = text.replace("</i>", "_");
		text = text.replace("<ul>", "\n");
		text = text.replace("</ul>", "\n");
		text = text.replace("<li>", "- ");
		text = text.replace("</li>", "");
		text = text.replace("<br>", "\n");
		text = text.replace("<p>", "\n\n");
		text = text.replace("</p>", "\n\n");

		text = text.replace("<h1>", "=");
		text = text.replace("<h2>", "==");
		text = text.replace("<h3>", "===");
		text = text.replace("</h1>", "");
		text = text.replace("</h2>", "");
		text = text.replace("</h3>", "");

		Pattern p = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>");
		while (true) {
			Matcher m = p.matcher(text);
			if (m.find()) {
				String url = m.group(1);
				String name = m.group(2);
				text = m.replaceFirst(url + "[" + name + "]");
			} else {
				break;
			}
		}


		text = text.replaceAll("\\n(\\h)*", "\n");

		return text;
	}

	public static String toAnchor(String name) {
		return name.toLowerCase().replace(" ", "_").replace("/", "_");
	}

	public static String getAnchor(MetaResource resource) {
		return AsciidocUtils.toAnchor("resources_" + resource.getResourceType());
	}

	public static String getAnchor(MetaType type) {
		if (type instanceof MetaResource) {
			return getAnchor((MetaResource) type);
		}
		return AsciidocUtils.toAnchor("types_" + type.getName());
	}

}
