/*
 * Copyright 2010 Kevin Seim
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.beanio.config;

import java.util.*;

public class RecordConfig extends NodeConfig {

	private Integer minLength = null;
	private Integer maxLength = null;
	private String beanClass;
	private List<FieldConfig> fieldList = new ArrayList<FieldConfig>();
	
	@Override
	public char getType() {
		return RECORD;
	}
	public Integer getMinLength() {
		return minLength;
	}
	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}
	public Integer getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}
	public String getBeanClass() {
		return beanClass;
	}
	public void setBeanClass(String beanClass) {
		this.beanClass = beanClass;
	}
	
	public void addField(FieldConfig fieldConfig) {
		fieldList.add(fieldConfig);
	}
	public List<FieldConfig> getFieldList() {
		return fieldList;
	}
	public void setFieldList(List<FieldConfig> fieldList) {
		if (fieldList == null) {
			this.fieldList.clear();
		}
		else {
			this.fieldList = fieldList;
		}
	}
}
