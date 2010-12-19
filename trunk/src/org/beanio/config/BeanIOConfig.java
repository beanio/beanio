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

/**
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class BeanIOConfig {

	private List<StreamConfig> streamList = new ArrayList<StreamConfig>();
	private List<TypeHandlerConfig> handlerList = new ArrayList<TypeHandlerConfig>();
	
	public BeanIOConfig() { }
	
	public void addStream(StreamConfig stream) {
		streamList.add(stream);
	}
	public List<StreamConfig> getStreamList() {
		return streamList;
	}
	
	public void addHandler(TypeHandlerConfig handler) {
		handlerList.add(handler);
	}
	public List<TypeHandlerConfig> getHandlerList() {
		return handlerList;
	}
}
