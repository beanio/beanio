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

public class FieldConfig {
	
	private String name;
	private int position = -1;
	private Integer minLength;
	private Integer maxLength; // set to -1 for unbounded
	private boolean trim = true;
	private boolean required = false;
	private boolean recordIdentifier = false;
	private boolean ignored = false;
	private String literal;
	private String regex;
	private String getter;
	private String setter;
	private String type;
	private String handler;
	private String defaultValue;
	private int width = -1;
	private char padding = ' ';
	private String justify = "left";
	
	public String getDefault() {
		return defaultValue;
	}
	public void setDefault(String s) {
		this.defaultValue = s;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
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
	public boolean isTrim() {
		return trim;
	}
	public void setTrim(boolean trim) {
		this.trim = trim;
	}
	public boolean isRecordIdentifier() {
		return recordIdentifier;
	}
	public void setRecordIdentifier(boolean key) {
		this.recordIdentifier = key;
	}
	public boolean isIgnored() {
		return ignored;
	}
	public void setIgnored(boolean ignore) {
		this.ignored = ignore;
	}
	public String getLiteral() {
		return literal;
	}
	public void setLiteral(String literal) {
		this.literal = literal;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public String getGetter() {
		return getter;
	}
	public void setGetter(String getter) {
		this.getter = getter;
	}
	public String getSetter() {
		return setter;
	}
	public void setSetter(String setter) {
		this.setter = setter;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getHandler() {
		return handler;
	}
	public void setHandler(String handler) {
		this.handler = handler;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public char getPadding() {
		return padding;
	}
	public void setPadding(char padding) {
		this.padding = padding;
	}
	public String getJustify() {
		return justify;
	}
	public void setJustify(String justify) {
		this.justify = justify;
	}
}
