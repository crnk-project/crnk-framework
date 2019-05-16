package io.crnk.data.activiti.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Makes use of the system default timezone.
 */
public class DefaultDateTimeMapper implements DateTimeMapper {


	@Override
	public OffsetDateTime toOffsetDateTime(Date date) {
		Instant instant = date.toInstant();
		return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
}
