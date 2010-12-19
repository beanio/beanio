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
package org.beanio.parser;

import java.util.*;

/**
 * 
 * @author Kevin Seim
 * @since 1.0
 */
class RecordNode extends Node {

	private RecordDefinition context;
	private int recordCount;
	
	public RecordNode(RecordDefinition context) {
		this.context = context;
	}
	
	
	public RecordDefinition getRecordContext() {
		return context;
	}
	
	@Override
	protected NodeDefinition getNodeContext() {
		return context;
	}

	@Override
	public List<Node> getChildren() {
		return Arrays.<Node>asList(this);
	}
	
	@Override
	public Node close() {
		return getGroupCount() < getMinOccurs() ? this : null;
	}
	
	@Override
	public Node matchAny(Record record) {
		return context.matches(record) ? this : null;
	}

	@Override
	public Node matchNext(Record record) {
		
		if (context.matches(record)) {
			recordCount++;
			groupCount++;
			return this;
		}
		
		return null;
	}
	
	@Override
	public void reset() {
		super.reset();
		groupCount = 0;
	}
	
	@Override
	public boolean isRecord() {
		return true;
	}
	
	public int getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}
	
	protected void print(int level) {
		for (int i=0; i<level; i++)
			System.out.print(" ");
		System.out.println(this);
	}
}
