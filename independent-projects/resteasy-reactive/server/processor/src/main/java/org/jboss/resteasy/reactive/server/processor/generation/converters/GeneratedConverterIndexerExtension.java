package org.jboss.resteasy.reactive.server.processor.generation.converters;

import static org.jboss.resteasy.reactive.common.processor.ResteasyReactiveDotNames.STRING;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.resteasy.reactive.common.processor.EndpointIndexer;
import org.jboss.resteasy.reactive.server.core.parameters.converters.LoadedParameterConverter;
import org.jboss.resteasy.reactive.server.core.parameters.converters.ParameterConverter;
import org.jboss.resteasy.reactive.server.core.parameters.converters.ParameterConverterSupplier;
import org.jboss.resteasy.reactive.server.core.parameters.converters.RuntimeResolvedConverter;
import org.jboss.resteasy.reactive.server.processor.ServerEndpointIndexer;

import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

public class GeneratedConverterIndexerExtension implements ServerEndpointIndexer.ConverterSupplierIndexerExtension {

    final Function<String, ClassOutput> classOutput;

    public GeneratedConverterIndexerExtension(Function<String, ClassOutput> classOutput) {
        this.classOutput = classOutput;
    }

    @Override
    public ParameterConverterSupplier extractConverterImpl(String elementType, IndexView indexView,
            Map<String, String> existingConverters, String errorLocation, boolean hasRuntimeConverters) {

        MethodInfo fromString = null;
        MethodInfo valueOf = null;
        MethodInfo stringCtor = null;
        String primitiveWrapperType = EndpointIndexer.primitiveTypes.get(elementType);
        String prefix = "";
        if (primitiveWrapperType != null) {
            //valueOf = MethodDescriptor.ofMethod(primitiveWrapperType, "valueOf", primitiveWrapperType, String.class);
            prefix = "io.quarkus.generated.";
        } else {
            ClassInfo type = indexView.getClassByName(DotName.createSimple(elementType));
            if (type != null) {
                for (MethodInfo i : type.methods()) {
                    boolean isStatic = Modifier.isStatic(i.flags());
                    boolean isNotPrivate = !Modifier.isPrivate(i.flags());
                    if ((i.parametersCount() == 1) && isNotPrivate) {
                        if (i.parameterType(0).name().equals(STRING)) {
                            if (i.name().equals("<init>")) {
                                stringCtor = i;
                            } else if (i.name().equals("valueOf") && isStatic) {
                                valueOf = i;
                            } else if (i.name().equals("fromString") && isStatic) {
                                fromString = i;
                            }
                        }
                    }
                }
                if (type.isEnum()) {
                    //spec weirdness, enums order is different
                    if (fromString != null) {
                        valueOf = null;
                    }
                }
            }
        }

        String baseName;
        ParameterConverterSupplier delegate;
        if (stringCtor != null || valueOf != null || fromString != null) {
            String effectivePrefix = prefix + elementType;
            if (effectivePrefix.startsWith("java")) {
                effectivePrefix = effectivePrefix.replace("java", "javaq"); // generated classes can't start with the java package
            }
            baseName = effectivePrefix + "$quarkusrestparamConverter$";
            try (ClassCreator classCreator = new ClassCreator(classOutput.apply(elementType),
                    baseName, null,
                    Object.class.getName(), ParameterConverter.class.getName())) {
                MethodCreator mc = classCreator.getMethodCreator("convert", Object.class, Object.class);
                if (primitiveWrapperType != null) {
                    //valueOf = MethodDescriptor.ofMethod(primitiveWrapperType, "valueOf", primitiveWrapperType, String.class);
                } else if (stringCtor != null) {
                    if (Modifier.isPublic(stringCtor.flags())) {
                        ResultHandle ret = mc.newInstance(stringCtor, mc.getMethodParam(0));
                        mc.returnValue(ret);
                    } else {
                        ResultHandle clazz = mc.loadClass(elementType);

                        ResultHandle varArgsParameter = mc.newArray(Class.class, 1);
                        mc.writeArrayValue(varArgsParameter, 0, mc.loadClass(String.class));

                        ResultHandle constructor = mc.invokeVirtualMethod(MethodDescriptor.ofMethod(Class.class,
                                "getDeclaredConstructor", Constructor.class, Class[].class), clazz, varArgsParameter);
                        mc.invokeVirtualMethod(
                                MethodDescriptor.ofMethod(Constructor.class, "setAccessible", void.class, boolean.class),
                                constructor, mc.load(true));

                        varArgsParameter = mc.newArray(Object.class, 1);
                        mc.writeArrayValue(varArgsParameter, 0, mc.getMethodParam(0));

                        ResultHandle ret = mc.invokeVirtualMethod(
                                MethodDescriptor.ofMethod(Constructor.class, "newInstance", Object.class, Object[].class),
                                constructor, varArgsParameter);
                        mc.returnValue(ret);
                    }
                } else if (valueOf != null) {
                    if (Modifier.isPublic(valueOf.flags())) {
                        ResultHandle ret = mc.invokeStaticMethod(valueOf, mc.getMethodParam(0));
                        mc.returnValue(ret);
                    } else {
                        //Object.class.getDeclaredMethod()
                    }
                } else if (fromString != null) {
                    if (true || Modifier.isPublic(fromString.flags())) {
                        ResultHandle ret = mc.invokeStaticMethod(fromString, mc.getMethodParam(0));
                        mc.returnValue(ret);
                    } else {

                        ResultHandle varArgsParameter = mc.newArray(Class.class, 1);
                        mc.writeArrayValue(varArgsParameter, 0, mc.loadClass(String.class));

                        ResultHandle clazz = mc.loadClass(elementType);
                        ResultHandle method = mc.invokeVirtualMethod(MethodDescriptor.ofMethod(Class.class,
                                "getDeclaredMethod", Method.class, String.class, Class[].class), clazz, mc.load("fromString"),
                                varArgsParameter);
                        mc.invokeVirtualMethod(
                                MethodDescriptor.ofMethod(Method.class, "setAccessible", void.class, boolean.class),
                                method, mc.load(true));

                        varArgsParameter = mc.newArray(Object.class, 1);
                        mc.writeArrayValue(varArgsParameter, 0, mc.getMethodParam(0));
                        ResultHandle ret = mc.invokeVirtualMethod(
                                MethodDescriptor.ofMethod(Method.class, "invoke", Object.class, Object.class, Object[].class),
                                method, mc.loadNull(), varArgsParameter);
                        mc.returnValue(ret);
                    }
                }
            }

            delegate = new LoadedParameterConverter().setClassName(baseName);
        } else {
            // let's not try this again
            baseName = null;
            delegate = null;
        }
        existingConverters.put(elementType, baseName);
        if (hasRuntimeConverters)
            return new RuntimeResolvedConverter.Supplier().setDelegate(delegate);
        if (delegate == null)
            throw new RuntimeException("Failed to find converter for " + elementType);
        return delegate;
    }
}
