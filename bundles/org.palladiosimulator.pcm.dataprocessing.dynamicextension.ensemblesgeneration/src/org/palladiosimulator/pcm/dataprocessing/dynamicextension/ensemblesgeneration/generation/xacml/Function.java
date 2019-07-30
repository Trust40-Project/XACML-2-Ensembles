package org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.xacml;

import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.util.ScalaHelper;

import com.att.research.xacml.api.XACML3;

public enum Function {
	STRING_EQUALS(XACML3.ID_FUNCTION_STRING_EQUAL.stringValue(), Attribute.TYPE_STRING) {
		@Override
		protected StringBuilder getCheckCode(final String scalaAttributeName, final String value) {
			return ScalaHelper.parenthesize(new StringBuilder(AttributeExtractor.VAR_NAME)
					.append(".")
					.append(scalaAttributeName)
					.append(" == ")
					.append("\"")
					.append(value)
					.append("\""));
		}
	},
	STRING_REGEX(XACML3.ID_FUNCTION_STRING_REGEXP_MATCH.stringValue(), Attribute.TYPE_STRING) {
		@Override
		protected StringBuilder getCheckCode(String scalaAttributeName, String value) {
			final StringBuilder nullCheck = nullCheck(scalaAttributeName);
			final StringBuilder matchAgainstValue = ScalaHelper.parenthesize(new StringBuilder("\"")
					.append(value.replaceAll("\\\\", "\\\\\\\\")).append("\""));
			return ScalaHelper.parenthesize(nullCheck
					.append(AttributeExtractor.VAR_NAME)
					.append(".")
					.append(scalaAttributeName)
					.append(".")
					.append("matches")
					.append(matchAgainstValue));
		}
	},
	LESS_INT(XACML3.ID_FUNCTION_INTEGER_LESS_THAN.stringValue(), Attribute.TYPE_INT) {
		@Override
		protected StringBuilder getCheckCode(String scalaAttributeName, String value) {
			// value defined in policy ('value') must be less than the request value 
			return compare(scalaAttributeName, " > ", value);
		}
	},
	LESS_DOUBLE(XACML3.ID_FUNCTION_DOUBLE_LESS_THAN.stringValue(), Attribute.TYPE_DOUBLE) {
		@Override
		protected StringBuilder getCheckCode(String scalaAttributeName, String value) {
			// value defined in policy ('value') must be less than the request value 
			return compare(scalaAttributeName, " > ", value);
		}
	},
	GREATER_INT(XACML3.ID_FUNCTION_INTEGER_GREATER_THAN.stringValue(), Attribute.TYPE_INT) {
		@Override
		protected StringBuilder getCheckCode(String scalaAttributeName, String value) {
			// value defined in policy ('value') must be greater than the request value 
			return compare(scalaAttributeName, " < ", value);
		}
	},
	GREATER_DOUBLE(XACML3.ID_FUNCTION_DOUBLE_GREATER_THAN.stringValue(), Attribute.TYPE_DOUBLE) {
		@Override
		protected StringBuilder getCheckCode(String scalaAttributeName, String value) {
			// value defined in policy ('value') must be greater than the request value 
			return compare(scalaAttributeName, " < ", value);
		}
	},
	LESS_TIME(XACML3.ID_FUNCTION_TIME_LESS_THAN_OR_EQUAL.stringValue(), Attribute.TYPE_TIME) {
		@Override
		protected StringBuilder getCheckCode(String scalaAttributeName, String value) {
			// time defined in policy ('value') must be less than the request value
			return compareTime(scalaAttributeName, " isAfter ", value);
		}
	},
	GREATER_TIME(XACML3.ID_FUNCTION_TIME_GREATER_THAN_OR_EQUAL.stringValue(), Attribute.TYPE_TIME) {
		@Override
		protected StringBuilder getCheckCode(String scalaAttributeName, String value) {
			// time defined in policy ('value') must be greater than the request value
			return compareTime(scalaAttributeName, " isBefore ", value);
		}
	}
	;
	
	private final String matchId;
	private final String scalaType;
	
	private Function(final String matchId, final String scalaType) {
		this.matchId = matchId;
		this.scalaType = scalaType;
	}
	
	public String getMatchId() {
		return this.matchId;
	}
	
	protected String getScalaType() {
		return this.scalaType;
	}
	
	protected abstract StringBuilder getCheckCode(String scalaAttributeName, String value);
	
	public static Function of(final String matchId) {
		for (final Function function : values()) {
			if (function.getMatchId().equals(matchId)) {
				return function;
			}
		}
		return null;
	}
	
	private static StringBuilder nullCheck(final String scalaAttributeName) {
		return new StringBuilder(AttributeExtractor.VAR_NAME)
				.append(".")
				.append(scalaAttributeName)
				.append(" != null && ");
	}
	
	private static StringBuilder compare(final String scalaAttributeName, final String comparison, final String value) {
		return ScalaHelper.parenthesize(new StringBuilder(AttributeExtractor.VAR_NAME)
				.append(".")
				.append(scalaAttributeName)
				.append(comparison)
				.append(value));
	}
	
	private static StringBuilder compareTime(String scalaAttributeName, String comparison, String value) {
		final StringBuilder parsedValue = ScalaHelper.parenthesize(new StringBuilder("LocalTime.parse(\"")
				.append(value)
				.append("\", DateTimeFormatter.ISO_OFFSET_TIME)"));
		final StringBuilder comparisonStringBuilder = ScalaHelper.parenthesize(new StringBuilder(scalaAttributeName)
				.append(comparison)
				.append(parsedValue));
		final StringBuilder equalityStringBuilder = ScalaHelper.parenthesize(new StringBuilder(scalaAttributeName)
				.append(" equals ")
				.append(parsedValue));
		return ScalaHelper.parenthesize(comparisonStringBuilder
				.append(" || ")
				.append(equalityStringBuilder));
	}
}