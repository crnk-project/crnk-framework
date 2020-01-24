package io.crnk.core.queryspec.internal.typed;

import io.crnk.core.queryspec.AbstractPathSpec;
import io.crnk.core.queryspec.PathSpec;

public abstract class ResourcePathSpec extends PathSpecBase {

    protected ResourcePathSpec(PathSpec pathSpec) {
        super(pathSpec);
    }

    protected ResourcePathSpec(AbstractPathSpec boundSpec) {
        super(boundSpec);
    }
}
