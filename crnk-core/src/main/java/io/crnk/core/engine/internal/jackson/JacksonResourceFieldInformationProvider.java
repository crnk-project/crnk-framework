package io.crnk.core.engine.internal.jackson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.utils.Optional;

/**
 * A Jackson-backed implementation of the {@link ResourceFieldInformationProvider} interface.
 * 
 * @author Craig Setera
 */
public class JacksonResourceFieldInformationProvider implements ResourceFieldInformationProvider {
	/**
	 * A thin warpper around a Jackson {@link BeanDescription} that provides
	 * easier access to information important to serialization decisions.
	 */
	private class JacksonBeanDescription {
		private BeanDescription beanDescription;
		private Map<String, BeanPropertyDefinition> propertiesByName;
		
		JacksonBeanDescription(BeanDescription beanDescription) {
			super();
			this.beanDescription = beanDescription;
		}
		
		/**
		 * Return the {@link BeanPropertyDefinition} for the specified
		 * JSON name.
		 * 
		 * @param jsonName
		 * @return
		 */
		public BeanPropertyDefinition getProperty(String jsonName) {
			return getPropertiesByName().get(jsonName);
		}
		
		/**
		 * Return a boolean indicating whether the bean description
		 * has the specified JSON property name.  This value can inform
		 * whether a property has been ignored in some way by Jackson.
		 * 
		 * @param name
		 * @return
		 */
		public boolean hasProperty(String name) {
			return getProperty(name) != null;
		}
		
		/**
		 * Return the {@link BeanPropertyDefinition}'s keyed by JSON name.
		 * 
		 * @return
		 */
		private Map<String, BeanPropertyDefinition> getPropertiesByName() {
			if (propertiesByName == null) {
				propertiesByName = new HashMap<>();
				
				for (BeanPropertyDefinition prop : beanDescription.findProperties()) {
					propertiesByName.put(prop.getName(), prop);
				}
			}
			
			return propertiesByName;
		}
	}
	
	private ObjectMapper objectMapper;
	private Map<Class<?>, JacksonBeanDescription> classDescriptions;

	public JacksonResourceFieldInformationProvider() {
		this(new ObjectMapper());
	}
	
	public JacksonResourceFieldInformationProvider(ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
		this.classDescriptions = new HashMap<>();
	}

	/*
	 * (non-Javadoc)
	 * @see io.crnk.core.engine.information.resource.AttributeSerializationInformationProvider#isIgnored(java.lang.Class, io.crnk.core.engine.information.resource.ResourceField)
	 */
	@Override
	public Optional<Boolean> isIgnored(Class<?> resourceClass, ResourceField resourceField) {
		switch (resourceField.getResourceFieldType()) {
			case ATTRIBUTE:
			case ID:
				JacksonBeanDescription jacksonBeanDescription = getJacksonBeanDescription(resourceClass);
				return Optional.of(!jacksonBeanDescription.hasProperty(resourceField.getJsonName()));
				
			default:
				return Optional.of(false);
		}
	}
	
	/**
	 * Return the {@link JacksonBeanDescription} for the specified resource class.
	 * 
	 * @param resourceClass
	 * @return
	 */
	private JacksonBeanDescription getJacksonBeanDescription(Class<?> resourceClass) {
		JacksonBeanDescription jacksonBeanDescription = classDescriptions.get(resourceClass);
		
		if (jacksonBeanDescription == null) {
			jacksonBeanDescription = constructBeanDescription(resourceClass);
			classDescriptions.put(resourceClass, jacksonBeanDescription);
		}
		
		return jacksonBeanDescription;
	}
	
	/**
	 * Use Jackson to construct a new {@link JacksonBeanDescription} for the specified
	 * resource class.
	 * 
	 * @param resourceClass
	 * @return
	 */
	private JacksonBeanDescription constructBeanDescription(Class<?> resourceClass) {
		TypeFactory typeFactory = objectMapper.getTypeFactory();
		JavaType javaType = typeFactory.constructType(resourceClass);
		
		SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
		BeanDescription beanDescription = serializationConfig.introspect(javaType);

		return new JacksonBeanDescription(beanDescription);
	}
}
