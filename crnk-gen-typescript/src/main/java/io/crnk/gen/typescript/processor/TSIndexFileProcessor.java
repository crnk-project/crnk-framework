package io.crnk.gen.typescript.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.gen.typescript.model.TSExport;
import io.crnk.gen.typescript.model.TSSource;

/**
 * Computes Index files for the given source files.
 */
public class TSIndexFileProcessor implements TSSourceProcessor {

	@Override
	public Set<TSSource> process(Set<TSSource> sources) {
		Set<TSSource> newSources = new HashSet<>(sources);

		Map<String, List<TSSource>> directoryIndex = new HashMap<>();
		for (TSSource fileSource : sources) {
			String dir = fileSource.getDirectory();

			if (!directoryIndex.containsKey(dir)) {
				directoryIndex.put(dir, new ArrayList<TSSource>());
			}
			directoryIndex.get(dir).add(fileSource);
		}

		for (Map.Entry<String, List<TSSource>> entry : directoryIndex.entrySet()) {
			TSSource indexSource = new TSSource();
			indexSource.setName("index");
			indexSource.setNpmPackage(entry.getValue().get(0).getNpmPackage());
			indexSource.setDirectory(entry.getKey());

			for (TSSource sourceFile : entry.getValue()) {
				TSExport exportElement = new TSExport();
				exportElement.setAny(true);
				exportElement.setPath("./" + sourceFile.getName());
				indexSource.getExports().add(exportElement);
			}
			newSources.add(indexSource);
		}
		return newSources;
	}
}
