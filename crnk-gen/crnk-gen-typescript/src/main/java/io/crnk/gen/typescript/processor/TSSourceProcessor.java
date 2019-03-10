package io.crnk.gen.typescript.processor;

import io.crnk.gen.typescript.model.TSSource;

import java.util.List;

@FunctionalInterface
public interface TSSourceProcessor {

	List<TSSource> process(List<TSSource> sources);
}
