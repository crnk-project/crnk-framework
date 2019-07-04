package io.crnk.gen.asciidoc.capture;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterListener;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.client.module.HttpAdapterAware;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.IOUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.*;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.gen.asciidoc.internal.AsciidocBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class AsciidocCaptureModule implements Module, HttpAdapterAware {


	private ThreadLocal<RequestCaptor> threadLocal = new ThreadLocal<>();

	private AsciidocCaptureConfig config;

	public AsciidocCaptureModule(AsciidocCaptureConfig config) {
		this.config = config;

		PreconditionUtil.verify(config.getGenDir() != null, "outputDir not set in config");
	}

	@Override
	public String getModuleName() {
		return "asciidoc";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepositoryDecoratorFactory(new AsciidocDecoratorFactory(context));
	}

	private int nextCaptureNum = 0;

	public RequestCaptor capture(String title) {
		String key = nextCaptureNum++ + "_" + title.toLowerCase().replace(" ", "_");

		RequestCaptor doc = new RequestCaptor(threadLocal);
		doc.setKey(key);
		doc.setTitle(title);
		return doc;
	}

	@Override
	public void setHttpAdapter(HttpAdapter httpAdapter) {
		httpAdapter.addListener(new HttpAdapterListener() {
			@Override
			public void onRequest(HttpAdapterRequest request) {
				RequestCaptor requestCaptor = threadLocal.get();
				if (requestCaptor != null) {
					requestCaptor.setRequest(request);
				}
			}

			@Override
			public void onResponse(HttpAdapterRequest request, HttpAdapterResponse response) {
				RequestCaptor requestCaptor = threadLocal.get();
				if (requestCaptor != null) {
					requestCaptor.setResponse(response);
				}
			}
		});
	}

	private class AsciidocDecoratorFactory implements RepositoryDecoratorFactory {

		private final ModuleContext context;

		public AsciidocDecoratorFactory(ModuleContext context) {
			this.context = context;
		}

		@Override
		public Object decorateRepository(Object repository) {
			if (repository instanceof ResourceRepository) {
				return new AsciidocResourceDecorator((ResourceRepository) repository, this);
			}
			if (repository instanceof OneRelationshipRepository && repository instanceof ManyRelationshipRepository) {
				return new WrappedOneManyRelationshipRepository((OneRelationshipRepository & ManyRelationshipRepository)repository,
						new AsciidocOneRelationshipDecorator((OneRelationshipRepository) repository, this),
						new AsciidocManyRelationshipDecorator((ManyRelationshipRepository) repository, this));
			}
			if (repository instanceof OneRelationshipRepository) {
				return new AsciidocOneRelationshipDecorator((OneRelationshipRepository) repository, this);
			}
			if (repository instanceof ManyRelationshipRepository) {
				return new AsciidocManyRelationshipDecorator((ManyRelationshipRepository) repository, this);
			}
			return repository;
		}

		private void finalizeDoc(Class resourceClass) {
			ResourceRegistry resourceRegistry = context.getResourceRegistry();
			RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
			ResourceInformation resourceInformation = entry.getResourceInformation();

			RequestCaptor requestCaptor = threadLocal.get();
			if (requestCaptor != null) {
				threadLocal.remove();

				// in case of internal errors a response might not be available since called by finally {}
				if (requestCaptor.getResponse() != null) {
					requestCaptor.setResourceInformation(resourceInformation);
					String resourceType = resourceInformation.getResourceType();
					File resourceDir = new File(config.getGenDir(), resourceType);
					resourceDir.mkdirs();

					writeUrlFile(resourceDir, requestCaptor);
					writeTitleFile(resourceDir, requestCaptor);
					writeDescriptionFile(resourceDir, requestCaptor);
					writeRequestFile(resourceDir, requestCaptor);
					writeResponseFile(resourceDir, requestCaptor);
				}
			}
		}

		private void writeTitleFile(File resourceDir, RequestCaptor captor) {
			File file = new File(resourceDir, "captured_" + captor.getKey() + "_title.txt");
			IOUtils.writeFile(file, captor.getTitle());
		}

		private void writeDescriptionFile(File resourceDir, RequestCaptor captor) {
			if (captor.getDescription() != null) {
				File file = new File(resourceDir, "captured_" + captor.getKey() + "_description.txt");
				IOUtils.writeFile(file, captor.getDescription());
			}
		}

		private void writeUrlFile(File resourceDir, RequestCaptor captor) {
			HttpAdapterRequest request = captor.getRequest();
			AsciidocBuilder builder = newBuilder();
			builder.startSource("bash");
			builder.append(request.getHttpMethod().toString());
			builder.append(" ");
			builder.append(request.getUrl());
			builder.appendLineBreak();
			builder.endSource();

			File file = new File(resourceDir, "captured_" + captor.getKey() + "_url.adoc");
			builder.write(file);
		}

	}


	private AsciidocBuilder newBuilder() {
		return new AsciidocBuilder(null, config.getBaseDepth());
	}


	private void writeRequestFile(File resourceDir, RequestCaptor captor) {
		HttpAdapterRequest request = captor.getRequest();
		AsciidocBuilder builder = new AsciidocBuilder(null, config.getBaseDepth());
		builder.startSource("json");
		builder.append(request.getHttpMethod().toString());
		builder.append(" ");
		builder.append(request.getUrl());
		builder.append(" HTTP/1.1");
		builder.appendLineBreak();
		request.getHeadersNames().stream().sorted()
				.forEach(name -> builder.appendLine(name + ": " + request.getHeaderValue(name)));

		String body = request.getBody();
		if (body != null) {
			builder.appendLineBreak();
			builder.append(body);
			builder.appendLineBreak();
		}
		builder.endSource();

		File file = new File(resourceDir, "captured_" + captor.getKey() + "_request.adoc");
		builder.write(file);
	}

	private void writeResponseFile(File resourceDir, RequestCaptor captor) {
		HttpAdapterResponse response = captor.getResponse();
		AsciidocBuilder builder = new AsciidocBuilder(null, config.getBaseDepth());
		builder.startSource("json");

		builder.append("HTTP/1.1 ");
		builder.append(Integer.toString(response.code()));
		builder.append(" ");
		builder.append(response.message());
		builder.appendLineBreak();

		response.getHeaderNames().stream().sorted()
				.forEach(name -> builder.appendLine(name + ": " + response.getResponseHeader(name)));

		String body;
		try {
			body = response.body();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		if (body != null) {
			builder.appendLineBreak();
			builder.append(body);
			builder.appendLineBreak();
		}
		builder.endSource();

		File file = new File(resourceDir, "captured_" + captor.getKey() + "_response.adoc");
		builder.write(file);
	}

	class AsciidocResourceDecorator<T, I> extends WrappedResourceRepository<T, I> {

		private final AsciidocDecoratorFactory factory;

		public AsciidocResourceDecorator(ResourceRepository<T, I> wrappedRepository, AsciidocDecoratorFactory factory) {
			super(wrappedRepository);
			this.factory = factory;
		}

		@Override
		public T findOne(I id, QuerySpec querySpec) {
			try {
				return super.findOne(id, querySpec);
			}
			finally {
				factory.finalizeDoc(getResourceClass());
			}
		}


		@Override
		public ResourceList<T> findAll(QuerySpec querySpec) {
			try {
				return super.findAll(querySpec);
			}
			finally {
				factory.finalizeDoc(getResourceClass());
			}
		}

		@Override
		public ResourceList<T> findAll(Collection<I> ids, QuerySpec querySpec) {
			try {
				return super.findAll(ids, querySpec);
			}
			finally {
				factory.finalizeDoc(getResourceClass());
			}
		}

		@Override
		public <S extends T> S save(S entity) {
			try {
				return super.save(entity);
			}
			finally {
				factory.finalizeDoc(getResourceClass());
			}
		}

		@Override
		public <S extends T> S create(S entity) {
			try {
				return super.create(entity);
			}
			finally {
				factory.finalizeDoc(getResourceClass());
			}
		}

		@Override
		public void delete(I id) {
			try {
				super.delete(id);
			}
			finally {
				factory.finalizeDoc(getResourceClass());
			}
		}
	}

	class AsciidocOneRelationshipDecorator<T, I, D, J> extends WrappedOneRelationshipRepository<T, I, D, J> {

		private final AsciidocDecoratorFactory factory;

		public AsciidocOneRelationshipDecorator(OneRelationshipRepository<T, I, D, J> decoratedObject,
				AsciidocDecoratorFactory factory) {
			super(decoratedObject);
			this.factory = factory;
		}

		@Override
		public void setRelation(T source, J targetId, String fieldName) {
			try {
				super.setRelation(source, targetId, fieldName);
			}
			finally {
				factory.finalizeDoc(source.getClass());
			}
		}

		@Override
		public Map<I, D> findOneRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
			try {
				return super.findOneRelations(sourceIds, fieldName, querySpec);
			}
			finally {
				factory.finalizeDoc(querySpec.getResourceClass());
			}
		}
	}


	class AsciidocManyRelationshipDecorator<T, I, D, J> extends WrappedManyRelationshipRepository<T, I, D, J> {

		private final AsciidocDecoratorFactory factory;

		public AsciidocManyRelationshipDecorator(ManyRelationshipRepository<T, I, D, J> decoratedObject,
				AsciidocDecoratorFactory factory) {
			super(decoratedObject);
			this.factory = factory;
		}

		@Override
		public void setRelations(T source, Collection<J> targetIds, String fieldName) {
			try {
				super.setRelations(source, targetIds, fieldName);
			}
			finally {
				factory.finalizeDoc(source.getClass());
			}
		}

		@Override
		public void addRelations(T source, Collection<J> targetIds, String fieldName) {
			try {
				super.addRelations(source, targetIds, fieldName);
			}
			finally {
				factory.finalizeDoc(source.getClass());
			}
		}

		@Override
		public void removeRelations(T source, Collection<J> targetIds, String fieldName) {
			try {
				super.removeRelations(source, targetIds, fieldName);
			}
			finally {
				factory.finalizeDoc(source.getClass());
			}
		}


		@Override
		public Map<I, ResourceList<D>> findManyRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
			try {
				return super.findManyRelations(sourceIds, fieldName, querySpec);
			}
			finally {
				factory.finalizeDoc(querySpec.getResourceClass());
			}
		}
	}
}