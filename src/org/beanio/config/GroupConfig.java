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

public class GroupConfig extends NodeConfig {

	private List<NodeConfig> nodeList = new ArrayList<NodeConfig>();

	@Override
	public char getType() {
		return NodeConfig.GROUP;
	}
	public void addNode(NodeConfig node) {
		nodeList.add(node);
	}
	public List<NodeConfig> getNodeList() {
		return nodeList;
	}
	public void setNodeList(List<NodeConfig> nodeList) {
		if (nodeList == null) {
			this.nodeList.clear();
		}
		else {
			this.nodeList = nodeList;
		}
	}
}
