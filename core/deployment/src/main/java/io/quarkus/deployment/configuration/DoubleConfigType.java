package io.quarkus.deployment.configuration;

import java.lang.reflect.Field;
import java.util.OptionalDouble;

import org.wildfly.common.Assert;

import io.quarkus.deployment.AccessorFinder;
import io.quarkus.deployment.steps.ConfigurationSetup;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.runtime.configuration.ExpandingConfigSource;
import io.quarkus.runtime.configuration.NameIterator;
import io.smallrye.config.SmallRyeConfig;

/**
 */
public class DoubleConfigType extends LeafConfigType {

    private static final MethodDescriptor OPTDOUBLE_OR_ELSE_METHOD = MethodDescriptor.ofMethod(OptionalDouble.class, "orElse",
            double.class, double.class);
    private static final MethodDescriptor DOUBLE_VALUE_METHOD = MethodDescriptor.ofMethod(Double.class, "doubleValue",
            double.class);

    final String defaultValue;

    public DoubleConfigType(final String containingName, final CompoundConfigType container, final boolean consumeSegment,
            final String defaultValue, String javadocKey, String configKey) {
        super(containingName, container, consumeSegment, javadocKey, configKey);
        Assert.checkNotEmptyParam("defaultValue", defaultValue);
        this.defaultValue = defaultValue;
    }

    public void acceptConfigurationValue(final NameIterator name, final ExpandingConfigSource.Cache cache,
            final SmallRyeConfig config) {
        final GroupConfigType container = getContainer(GroupConfigType.class);
        if (isConsumeSegment())
            name.previous();
        container.acceptConfigurationValueIntoLeaf(this, name, cache, config);
        // the iterator is not used after this point
        // if (isConsumeSegment()) name.next();
    }

    public void generateAcceptConfigurationValue(final BytecodeCreator body, final ResultHandle name,
            final ResultHandle cache, final ResultHandle config) {
        final GroupConfigType container = getContainer(GroupConfigType.class);
        if (isConsumeSegment())
            body.invokeVirtualMethod(NI_PREV_METHOD, name);
        container.generateAcceptConfigurationValueIntoLeaf(body, this, name, cache, config);
        // the iterator is not used after this point
        // if (isConsumeSegment()) body.invokeVirtualMethod(NI_NEXT_METHOD, name);
    }

    public void acceptConfigurationValueIntoGroup(final Object enclosing, final Field field, final NameIterator name,
            final SmallRyeConfig config) {
        try {
            field.setDouble(enclosing, config.getValue(name.toString(), OptionalDouble.class).orElse(0.0));
        } catch (IllegalAccessException e) {
            throw toError(e);
        }
    }

    public void generateAcceptConfigurationValueIntoGroup(final BytecodeCreator body, final ResultHandle enclosing,
            final MethodDescriptor setter, final ResultHandle name, final ResultHandle config) {
        // config.getValue(name.toString(), OptionalDouble.class).orElse(0.0)
        final ResultHandle optionalValue = body.checkCast(body.invokeVirtualMethod(
                SRC_GET_VALUE,
                config,
                body.invokeVirtualMethod(
                        OBJ_TO_STRING_METHOD,
                        name),
                body.loadClass(OptionalDouble.class)), OptionalDouble.class);
        final ResultHandle doubleValue = body.invokeVirtualMethod(
                OPTDOUBLE_OR_ELSE_METHOD,
                optionalValue,
                body.load(0.0));
        body.invokeStaticMethod(setter, enclosing, doubleValue);
    }

    public String getDefaultValueString() {
        return defaultValue;
    }

    public Class<?> getItemClass() {
        return double.class;
    }

    void getDefaultValueIntoEnclosingGroup(final Object enclosing, final ExpandingConfigSource.Cache cache,
            final SmallRyeConfig config, final Field field) {
        try {
            field.setDouble(enclosing, config.convert(
                    ExpandingConfigSource.expandValue(defaultValue, cache), Double.class).doubleValue());
        } catch (IllegalAccessException e) {
            throw toError(e);
        }
    }

    void generateGetDefaultValueIntoEnclosingGroup(final BytecodeCreator body, final ResultHandle enclosing,
            final MethodDescriptor setter, final ResultHandle cache, final ResultHandle config) {
        body.invokeStaticMethod(setter, enclosing,
                body.invokeVirtualMethod(DOUBLE_VALUE_METHOD, getConvertedDefault(body, cache, config)));
    }

    public ResultHandle writeInitialization(final BytecodeCreator body, final AccessorFinder accessorFinder,
            final ResultHandle cache, final ResultHandle smallRyeConfig) {
        return body.invokeVirtualMethod(DOUBLE_VALUE_METHOD, getConvertedDefault(body, cache, smallRyeConfig));
    }

    private ResultHandle getConvertedDefault(final BytecodeCreator body, final ResultHandle cache, final ResultHandle config) {
        return body.checkCast(body.invokeVirtualMethod(
                SRC_CONVERT_METHOD,
                config,
                cache == null ? body.load(defaultValue)
                        : body.invokeStaticMethod(
                                ConfigurationSetup.ECS_EXPAND_VALUE,
                                body.load(defaultValue),
                                cache),
                body.loadClass(Double.class)), Double.class);
    }
}
