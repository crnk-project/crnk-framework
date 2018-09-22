package io.crnk.gen.typescript.processor;

import io.crnk.gen.typescript.model.TSExport;
import io.crnk.gen.typescript.model.TSSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes Index files for the given source files.
 */
public class TSIndexFileProcessor implements TSSourceProcessor {

	@Override
	public List<TSSource> process(List<TSSource> sources) {
		List<TSSource> newSources = new ArrayList<>(sources);

		Map<String, List<TSSource>> directoryIndex = new HashMap<>();
		for (TSSource fileSource : sources) {
			String dir = fileSource.getDirectory();

			if (!directoryIndex.containsKey(dir)) {
				directoryIndex.put(dir, new ArrayList<TSSource>());
			}
			directoryIndex.get(dir).add(fileSource);
		}

		List<String> keys = new ArrayList<>(directoryIndex.keySet());
		Collections.sort(keys);

		for (String key : keys) {
			List<TSSource> sourceFiles = new ArrayList<>(directoryIndex.get(key));
			Collections.sort(sourceFiles, new Comparator<TSSource>() {
				@Override
				public int compare(TSSource o1, TSSource o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			TSSource indexSource = new TSSource();
			indexSource.setName("index");
			indexSource.setNpmPackage(sourceFiles.get(0).getNpmPackage());
			indexSource.setDirectory(key);

			for (TSSource sourceFile : sourceFiles) {
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
