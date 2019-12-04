package io.crnk.core.engine.information.resource;

import io.crnk.core.engine.internal.utils.PreconditionUtil;

/**
 * Defines an integer-based version range.
 */
public class VersionRange {

    public static final VersionRange UNBOUNDED = VersionRange.of(0, Integer.MAX_VALUE);

    private int min;

    private int max;

    private VersionRange() {
    }

    public static VersionRange of(int min, int max) {
        VersionRange versionRange = new VersionRange();
        versionRange.min = min;
        versionRange.max = max;
        return versionRange;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean contains(int version) {
        PreconditionUtil.verify(version >= 0, "version not set");
        return min <= version && version <= max;
    }
}
