package io.crnk.activiti.mapper;

import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Performs conversion of legacy java.util.Date to java.time.* API.
 */
public interface DateTimeMapper {

	OffsetDateTime toOffsetDateTime(Date date);
}
