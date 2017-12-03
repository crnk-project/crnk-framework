package io.crnk.gen.typescript.processor;

import java.util.Set;

import io.crnk.gen.typescript.model.TSSource;

@FunctionalInterface
public interface TSSourceProcessor {

	Set<TSSource> process(Set<TSSource> sources);
}
