package org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.xacml;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.att.research.xacml.api.XACML3;

public enum Attribute {
	INTERNALSTATE(Category.RESOURCE, "context:internalstate", "internalstate", Function.STRING_EQUALS),
	SHIFT_NAME(Category.SUBJECT, "context:shift:name", "shiftName", Function.STRING_EQUALS),
	ORGANISATION(Category.SUBJECT, "context:organisation", "organisation", Function.STRING_REGEX),
	ROLE(Category.SUBJECT, "context:role", "role", Function.STRING_REGEX),
	LOCATION(Category.SUBJECT, "context:location", "location", Function.STRING_REGEX),
	PRIVACYLEVEL(Category.RESOURCE, "context:privacylevel", "privacylevel", Function.STRING_REGEX),
	LESS_INT(Category.RESOURCE, "context:comparison:int", "valueInt", Function.LESS_INT),
	GREATER_INT(Category.RESOURCE, "context:comparison:int", "valueInt", Function.GREATER_INT),
	LESS_DOUBLE(Category.RESOURCE, "context:comparison:double", "valueDouble", Function.LESS_DOUBLE),
	GREATER_DOUBLE(Category.RESOURCE, "context:comparison:double", "valueDouble", Function.GREATER_DOUBLE),
	LESS_TIME(Category.ENVIRONMENT, XACML3.ID_ENVIRONMENT_CURRENT_TIME.stringValue(), "now", Function.LESS_TIME),
	GREATER_TIME(Category.ENVIRONMENT, XACML3.ID_ENVIRONMENT_CURRENT_TIME.stringValue(), "now", Function.GREATER_TIME);
	
	protected static final String TYPE_STRING = "String";
	protected static final String TYPE_INT = "Int";
	protected static final String TYPE_DOUBLE = "Double";
	protected static final String TYPE_TIME = "LocalTime";
	
	private final Category attributeCategory;
	private final String attributeId;
	private final String scalaAttributeName;
	private final Function function;
	
	private Attribute(final Category attributeCategory, final String attributeId,
			final String scalaAttributeName, final Function function) {
		this.attributeCategory = attributeCategory;
		this.attributeId = attributeId;
		this.scalaAttributeName = scalaAttributeName;
		this.function = function;
	}
	
	public Category getAttributeCategory() {
		return this.attributeCategory;
	}
	
	public String getAttributeId() {
		return this.attributeId;
	}
	
	public String getScalaAttributeName() {
		return this.scalaAttributeName;
	}
	
	public String getScalaType() {
		return this.function.getScalaType();
	}
	
	public Function getFunction() {
		return this.function;
	}
	
	protected StringBuilder getCheckCode(final String value) {
		return this.function.getCheckCode(this.scalaAttributeName, value);
	}
	
	public static SortedSet<Attribute> getCategoryAttributes(final Category category) {
		final var set = new TreeSet<Attribute>(alphabeticComparator());
		
		for (final Attribute attribute : values()) {
			if (attribute.getAttributeCategory().equals(category)) {
				set.add(attribute);
			}
		}
		
		return set;
	}
	
	private static Comparator<? super Attribute> alphabeticComparator() {
		final var comparator = new Comparator<Attribute>() {
			@Override
			public int compare(Attribute arg0, Attribute arg1) {
				final int result = arg0.getScalaAttributeName().compareTo(arg1.getScalaAttributeName());
				if (result == 0) {
					return arg0.getFunction().getMatchId().compareTo(arg1.getFunction().getMatchId());
				}
				return result;
			}
		};
		
		return comparator;
	}

	public static Attribute of(final String attributeId, final Function function) {
		for (final Attribute attribute : values()) {
			if (attribute.getAttributeId().equals(attributeId) && attribute.getFunction().equals(function)) {
				return attribute;
			}
		}
		return null;
	}
}