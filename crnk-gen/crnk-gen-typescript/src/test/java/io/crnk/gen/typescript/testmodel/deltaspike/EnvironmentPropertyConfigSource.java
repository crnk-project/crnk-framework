/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.crnk.gen.typescript.testmodel.deltaspike;

import org.apache.deltaspike.core.impl.config.MapConfigSource;

/**
 * {@link org.apache.deltaspike.core.spi.config.ConfigSource}
 * which uses {@link System#getenv()}
 * <p>
 * We also allow to write underlines _ instead of dots _ in the
 * environment via export (unix) or SET (windows)
 */
class EnvironmentPropertyConfigSource extends MapConfigSource {

	public EnvironmentPropertyConfigSource() {
		super(System.getenv());
		initOrdinal(300);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getConfigName() {
		return "environment-properties";
	}

	@Override
	public String getPropertyValue(String key) {
		String val = super.getPropertyValue(key);
		if (val == null || val.isEmpty()) {
			val = super.getPropertyValue(key.replace('.', '_'));
		}

		return val;
	}

	@Override
	public boolean isScannable() {
		return false;
	}
}
