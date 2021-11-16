package io.quarkus.resteasy.reactive.jaxb.runtime.serialisers;

import java.beans.Introspector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

@ApplicationScoped
public class JaxbContextCache {
    private Map<String, JAXBContext> contexts = new ConcurrentHashMap<>();

    public JAXBContext retrieveContext(Object jaxbObject) {
        Class<?> clazz;
        if (jaxbObject instanceof JAXBElement) {
            clazz = ((JAXBElement<?>) jaxbObject).getDeclaredType();
        } else {
            clazz = jaxbObject.getClass();
        }

        return contexts.computeIfAbsent(clazz.getName(), (name -> {
            try {
                 return JAXBContext.newInstance(clazz);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public JAXBContext retrieveContext(Class<?> clazz) {
        return contexts.computeIfAbsent(clazz.getName(), (name -> {
            try {
                return JAXBContext.newInstance(clazz);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    public Object wrap(Object jaxbObject) {
        Class<?> clazz = jaxbObject.getClass();

        if (clazz.getAnnotation(XmlRootElement.class) == null) {
            return new JAXBElement(new QName(inferName(clazz)), clazz, jaxbObject);
        }
        return jaxbObject;
    }

    private static String inferName(Class<?> clazz) {
        return Introspector.decapitalize(clazz.getSimpleName());
    }
}
