package io.crnk.spring.mvc;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.http.HttpHeaders;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrnkErrorController extends BasicErrorController {

	public CrnkErrorController(ErrorAttributes errorAttributes,
							   ErrorProperties errorProperties) {
		super(errorAttributes, errorProperties);
	}

	public CrnkErrorController(ErrorAttributes errorAttributes,
							   ErrorProperties errorProperties,
							   List<ErrorViewResolver> errorViewResolvers) {
		super(errorAttributes, errorProperties, errorViewResolvers);
	}

	// TODO for whatever reason this is not called directly
	@RequestMapping(produces = HttpHeaders.JSONAPI_CONTENT_TYPE)
	@ResponseBody
	public ResponseEntity<Document> errorToJsonApi(HttpServletRequest request) {
		Map<String, Object> body = getErrorAttributes(request,
				isIncludeStackTrace(request, MediaType.ALL));
		HttpStatus status = getStatus(request);

		ErrorDataBuilder errorDataBuilder = ErrorData.builder();
		for (Map.Entry<String, Object> attribute : body.entrySet()) {
			if (attribute.getKey().equals("status")) {
				errorDataBuilder.setStatus(attribute.getValue().toString());
			} else if (attribute.getKey().equals("error")) {
				errorDataBuilder.setTitle(attribute.getValue().toString());
			} else if (attribute.getKey().equals("message")) {
				errorDataBuilder.setDetail(attribute.getValue().toString());
			} else {
				errorDataBuilder.addMetaField(attribute.getKey(), attribute.getValue());
			}
		}
		Document document = new Document();
		document.setErrors(Arrays.asList(errorDataBuilder.build()));
		return new ResponseEntity<>(document, status);
	}


	@RequestMapping
	@ResponseBody
	public ResponseEntity error(HttpServletRequest request) {
		return errorToJsonApi(request);
	}
}
