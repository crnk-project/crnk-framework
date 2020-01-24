package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiEmbeddable;

/**
 * Deeply nested data structure below {@link ProjectData}.
 */
@JsonApiEmbeddable
public class ProjectStatus {

    private String qualityStatus;

    private String timelineStatus;

    public String getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(String qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public String getTimelineStatus() {
        return timelineStatus;
    }

    public void setTimelineStatus(String timelineStatus) {
        this.timelineStatus = timelineStatus;
    }
}
