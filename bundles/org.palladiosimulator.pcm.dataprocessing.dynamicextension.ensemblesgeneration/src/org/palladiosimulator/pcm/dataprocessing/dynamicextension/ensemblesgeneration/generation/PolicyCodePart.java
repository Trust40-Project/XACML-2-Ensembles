package org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation;

import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.scala.ScalaClass;
import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.scala.ScalaCode;
import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.scala.ValueDeclaration;

import java.util.Arrays;

import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.scala.Call;
import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.scala.MethodSignature;
import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.scala.ScalaBlock;
import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.scala.ValueInitialisation;
import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.xacml.AttributeExtractor;
import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.xacml.Category;
import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.generation.xacml.ComponentCode;
import org.palladiosimulator.pcm.dataprocessing.dynamicextension.ensemblesgeneration.util.ScalaHelper;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;

/**
 * Represents the code part which represents a XACML policy, i.e. an ensemble in the ensemble system.
 * 
 * @author Jonathan Schenkenberger
 * @version 1.0
 */
public class PolicyCodePart implements CodePart {
    private static final String POLICY_PREFIX = "policy:";
    private static final String MAPPING = ".map[" + ScalaHelper.KEYWORD_COMPONENT + "](x => x.getClass().cast(x))";

    public static final ValueDeclaration SHIFT_NAME = 
            new ValueDeclaration("shiftName", ScalaHelper.KEYWORD_STRING, true, true);
    
    public static final String SET_SHIFT_NAME = "setShiftName";
    
    private static final ScalaCode SET_SHIFT_NAME_METHOD = new ScalaCode() {
        @Override
        public StringBuilder getCodeDefinition() {
            final ScalaBlock block = new ScalaBlock();
            final var argument = new ValueDeclaration("name", ScalaHelper.KEYWORD_STRING);
            block.appendPreBlockCode(
                    new MethodSignature(SET_SHIFT_NAME, Arrays.asList(argument), ScalaHelper.KEYWORD_BOOLEAN));
            block.appendBlockCode(new StringBuilder("shiftName = name\n"));
            block.appendBlockCode(new StringBuilder("return true"));
            return block.getCodeDefinition();
        }
    };
    
    protected static final String COMPONENTS = "components";
    protected static final String SUBJECT_FIELD_NAME = "allowedSubjects";
    protected static final String RESOURCE_FIELD_NAME = "allowedResources";
    protected static final String SITUATION = "situation";
    
    
    private final PolicyType policy;

    private final String actionName;

    /**
     * Creates a new policy code part for the given policy.
     * 
     * @param policy - the given policy
     */
    public PolicyCodePart(final PolicyType policy) {
        this.policy = policy;
        this.actionName = this.policy.getPolicyId().replaceFirst(POLICY_PREFIX, "");
    }

    @Override
    public ScalaBlock getCode() {
        final ScalaBlock ensembleCode = new ScalaBlock();

        final var actionEnsembleClass = new ScalaClass(true, this.actionName, ScalaHelper.KEYWORD_ENSEMBLE);
        ensembleCode.appendPreBlockCode(actionEnsembleClass);

        // shift name
        ensembleCode.appendBlockCode(SHIFT_NAME);
        ensembleCode.appendBlockCode(new StringBuilder("\n"));
        
        // subjects
        final var subjectExtractor = new AttributeExtractor(this.policy, Category.SUBJECT);
        final String subjectExpression = getExpression(ComponentCode.SUBJECT_CLASS_NAME, subjectExtractor);
        ensembleCode.appendBlockCode(new ValueInitialisation(SUBJECT_FIELD_NAME, subjectExpression));

        // resources
        final var resourceExtractor = new AttributeExtractor(this.policy, Category.RESOURCE);
        final String resourceExpression = getExpression(ComponentCode.RESOURCE_CLASS_NAME, resourceExtractor);
        ensembleCode.appendBlockCode(new ValueInitialisation(RESOURCE_FIELD_NAME, resourceExpression));

        // environment
        final var environmentExtractor = new AttributeExtractor(this.policy, Category.ENVIRONMENT);
        ensembleCode.appendBlockCode(getSituation(environmentExtractor.extract()));

        // allow call
        ensembleCode.appendBlockCode(new StringBuilder("\n"));
        ensembleCode.appendBlockCode(getAllow(SUBJECT_FIELD_NAME, this.actionName, RESOURCE_FIELD_NAME));

        // setShiftName method
        ensembleCode.appendBlockCode(new StringBuilder("\n"));
        ensembleCode.appendBlockCode(SET_SHIFT_NAME_METHOD);
        
        return ensembleCode;
    }

    /**
     * Gets the expression which checks the attributes of the category with the category class name 
     * and the extraction result of the given attribute extractor.
     * 
     * @param categoryClassName - the given category class name
     * @param extractor - the given attribute extractor
     * @return the expression which checks the attributes of the category
     */
    private String getExpression(final String categoryClassName, final AttributeExtractor extractor) {
        final StringBuilder extractionResult = extractor.extract();
        return COMPONENTS + ".select[" + categoryClassName + "]." + "filter(" + AttributeExtractor.VAR_NAME + " => "
                + (extractionResult.length() == 0 ? "true" : extractionResult) + ")" + MAPPING;
    }

    /**
     * Gets the allow call.
     * 
     * @param subjectsName - the subjects field name
     * @param actionName - the action name
     * @param resourcesName - the resources field name
     * @return the allow call
     */
    private Call getAllow(final String subjectsName, final String actionName, final String resourcesName) {
        return new Call(ScalaHelper.KEYWORD_ALLOW, subjectsName + ", " + "\"" + actionName + "\", " + resourcesName);
    }

    /**
     * Gets the situation expression if environment attributes exist, i.e. the check expression is not empty.
     * Otherwise, the empty expression is returned.
     * 
     * @param expression - the check expression
     * @return the situation expression if environment attributes exist,
     * otherwise, the empty expression is returned.
     */
    private StringBuilder getSituation(final StringBuilder expression) {
        if (expression.length() > 0) {
            return expression.insert(0, SITUATION + " {\n(" + SHIFT_NAME.getName() + " == null) || ").append("\n}");
        }
        return expression;
    }

    /**
     * Gets the action name.
     * 
     * @return the action name
     */
    public String getActionName() {
        return this.actionName;
    }
}
