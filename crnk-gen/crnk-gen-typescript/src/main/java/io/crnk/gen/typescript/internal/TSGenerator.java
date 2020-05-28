package io.crnk.gen.typescript.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.IOUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.gen.typescript.TSGeneratorConfig;
import io.crnk.gen.typescript.TSResourceFormat;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.model.libraries.NgrxJsonApiLibrary;
import io.crnk.gen.typescript.model.writer.TSWriter;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.transform.TSMetaTransformation;
import io.crnk.gen.typescript.transform.TSMetaTransformationContext;
import io.crnk.gen.typescript.transform.TSMetaTransformationOptions;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TSGenerator.class);

    private File outputDir;

    private MetaLookup lookup;

    private TSGeneratorConfig config;

    private Map<MetaElement, TSElement> elementSourceMap = new HashMap<>();

    private List<TSSource> sources = new ArrayList<>();

    private ArrayList<TSMetaTransformation> transformations;

    private List<TSElement> transformedElements = new ArrayList<>();

    public TSGenerator(File outputDir, MetaLookup lookup, TSGeneratorConfig config) {
        this.outputDir = outputDir;
        this.lookup = lookup;
        this.config = config;

        transformations = new ArrayList<>();
        for (final String className : config.getMetaTransformationClassNames()) {
            ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    transformations.add((TSMetaTransformation) getClass().getClassLoader().loadClass(className).newInstance());
                    return null;
                }
            }, "failed to load transformation implementation {}", className);
        }
    }

    public void run() throws IOException {
        if (config.getFormat() == TSResourceFormat.PLAINJSON) {
            // TODO eventually something better necesary (non-static)
            NgrxJsonApiLibrary.initPlainJson(config.getNpm().getPackageName());
        } else {
            NgrxJsonApiLibrary.initJsonApi();
        }
        transformMetaToTypescript();
        runProcessors();
        writeSources();
    }


    protected void writeSources() throws IOException {
        for (TSSource fileSource : sources) {
            TSWriter writer = new TSWriter(config.getCodeStyle());
            fileSource.accept(writer);

            File file = getFile(fileSource);
            String source = writer.toString();
            write(file, source);
        }

        if (config.getFormat() == TSResourceFormat.PLAINJSON) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("crnk.ts")) {
                File srcOutputDir = outputDir;
                File crnkFile = new File(srcOutputDir, "crnk.ts");
                crnkFile.getParentFile().mkdirs();
                byte[] data = IOUtils.readFully(inputStream);
                try (FileOutputStream out = new FileOutputStream(crnkFile)) {
                    out.write(data);
                }
            }
        }
    }

    protected static void write(File file, String source) throws IOException {
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(source);
        }
    }

    protected File getFile(TSSource sourceFile) {
        File dir = new File(outputDir, sourceFile.getDirectory());
        return new File(dir, sourceFile.getName() + ".ts");
    }

    private boolean postProcessing = false;

    public void transformMetaToTypescript() {
        Collection<MetaElement> elements = lookup.findElements(MetaElement.class);
        LOGGER.debug("transforming {} elements", elements.size());
        for (MetaElement element : elements) {
            boolean isRoot = isRoot(element);
            boolean isGenerated = isGenerated(element);
            if (isRoot && isGenerated) {
                LOGGER.debug("transforming {}", element.getId());
                transform(element, TSMetaTransformationOptions.EMPTY);
            } else {
                LOGGER.debug("ignoring {}, root={}, generated={}", element.getId(), isRoot, isGenerated);
            }
        }

        try {
            postProcessing = true;
            for (TSElement transformedElement : new ArrayList<>(transformedElements)) {
                for (TSMetaTransformation transformation : transformations) {
                    transformation.postTransform(transformedElement, createMetaTransformationContext());
                }
            }
        } finally {
            postProcessing = false;
        }
    }

    private boolean isGenerated(MetaElement element) {
        Set<String> includes = config.getIncludes();
        Set<String> excludes = config.getExcludes();
        return (includes.isEmpty() || matches(element.getId(), includes))
                && !matches(element.getId(), excludes);
    }

    private boolean matches(String id, Set<String> set) {
        for (String element : set) {
            if (id.startsWith(element)) {
                return true;
            }
        }
        return false;
    }


    public void runProcessors() {
        for (TSSourceProcessor processor : config.getSourceProcessors()) {
            sources = processor.process(sources);
        }
    }


    protected TSElement transform(MetaElement element, TSMetaTransformationOptions options) {
        if (elementSourceMap.containsKey(element)) {
            return elementSourceMap.get(element);
        }
        if (postProcessing) {
            throw new IllegalStateException("cannot add further element while post processing: " + element.getId());
        }
        for (TSMetaTransformation transformation : transformations) {
            if (transformation.accepts(element)) {
                LOGGER.debug("transforming type {} of type {} with {}", element.getId(), element.getClass().getSimpleName(),
                        transformation);
                TSElement tsElement = transformation.transform(element, createMetaTransformationContext(), options);
                transformedElements.add(tsElement);
                return tsElement;
            }
        }
        throw new IllegalStateException("unexpected element: " + element);
    }

    protected TSMetaTransformationContext createMetaTransformationContext() {
        return new TSMetaTransformationContextImpl();
    }

    private boolean isRoot(MetaElement element) {
        for (TSMetaTransformation transformation : transformations) {
            if (transformation.accepts(element)) {
                return transformation.isRoot(element);
            }
        }
        return false;
    }

    protected class TSMetaTransformationContextImpl implements TSMetaTransformationContext {

        private boolean isGenerated(TSSource source) {
            PreconditionUtil.verify(source != null, "no source provided");
            PreconditionUtil.verify(source.getNpmPackage() != null, "no package name specified for %s", source.getName());
            return source.getNpmPackage().equals(config.getNpm().getPackageName());
        }

        @Override
        public String getDirectory(MetaElement meta) {
            String idPath = meta.getId().substring(0, meta.getId().lastIndexOf('.'));
            if (idPath.startsWith(ResourceMetaProvider.DEFAULT_ID_PREFIX)) {
                return idPath.substring(Math.min(ResourceMetaProvider.DEFAULT_ID_PREFIX.length() + 1, idPath.length()));
            }
            String prefix = idPath;
            while (true) {
                String npmName = config.getNpm().getPackageMapping().get(prefix);
                if (npmName != null) {
                    return idPath.substring(prefix.length()).replace('.', '/');
                }

                String directoryMapping = config.getNpm().getDirectoryMapping().get(prefix);
                if (directoryMapping != null) {
                    String suffix = idPath.substring(prefix.length()).replace('.', '/');
                    String directory = normalize(directoryMapping) + "/" + normalize(suffix);
                    return directory.startsWith("/") ? directory : "/" + directory;
                }

                int sep = prefix.lastIndexOf('.');
                if (sep == -1) {
                    // return to root directory by default
                    return "";
                }
                prefix = prefix.substring(0, sep);
            }
        }

        private String normalize(String directoryMapping) {
            if (directoryMapping.endsWith("/")) {
                directoryMapping = directoryMapping.substring(0, directoryMapping.length() - 1);
            }
            if (directoryMapping.startsWith("/")) {
                return directoryMapping.substring(1);
            }
            return directoryMapping;
        }

        @Override
        public String getNpmPackage(MetaElement meta) {
            String idPath = meta.getId().substring(0, meta.getId().lastIndexOf('.'));
            if (idPath.startsWith(ResourceMetaProvider.DEFAULT_ID_PREFIX)) {
                return config.getNpm().getPackageName();
            }
            String prefix = idPath;
            while (true) {
                String npmName = config.getNpm().getPackageMapping().get(prefix);
                if (npmName != null) {
                    return npmName;
                }
                int sep = prefix.lastIndexOf('.');
                if (sep == -1) {
                    // by default add to local package
                    return config.getNpm().getPackageName();
                }
                prefix = prefix.substring(0, sep);
            }
        }

        @Override
        public void addSource(TSSource source) {
            if (isGenerated(source)) {
                sources.add(source);
            }
        }

        @Override
        public void putMapping(MetaElement metaElement, TSElement tsElement) {
            elementSourceMap.put(metaElement, tsElement);
        }

        @Override
        public TSElement transform(MetaElement metaElement, TSMetaTransformationOptions options) {
            return TSGenerator.this.transform(metaElement, options);
        }

        @Override
        public MetaElement getMeta(Class<?> implClass) {
            return lookup.findElement(MetaResource.class, implClass);
        }

        @Override
        public MetaElement getMeta(String metaId) {
            return lookup.findElement(MetaElement.class, metaId);
        }

        @Override
        public TSResourceFormat getResourceFormat() {
            return config.getFormat();
        }
    }


}
