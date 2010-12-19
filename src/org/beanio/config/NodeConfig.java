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

public abstract class NodeConfig {

	public static final char RECORD = 'R';
	public static final char GROUP = 'G';
	
	private String name;
	private int order = 0;
	private Integer minOccurs; // defaults to 0
	private Integer maxOccurs; // defaults to -1/unbounded
	
	public abstract char getType();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public Integer getMinOccurs() {
		return minOccurs;
	}
	public void setMinOccurs(Integer minOccurs) {
		this.minOccurs = minOccurs;
	}
	public Integer getMaxOccurs() {
		return maxOccurs;
	}
	public void setMaxOccurs(Integer maxOccurs) {
		this.maxOccurs = maxOccurs;
	}
	
	
	
}
