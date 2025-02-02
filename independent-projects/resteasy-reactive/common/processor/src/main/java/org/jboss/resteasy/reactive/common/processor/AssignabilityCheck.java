package org.jboss.resteasy.reactive.common.processor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

public final class AssignabilityCheck {

    public final DotName DOTNAME_OBJECT = DotName.createSimple(Object.class.getName());

    private final ConcurrentMap<DotName, Set<DotName>> cache;
    private final IndexView index;

    public AssignabilityCheck(IndexView beanArchiveIndex, IndexView applicationIndex) {
        this.cache = new ConcurrentHashMap<>();
        this.index = applicationIndex != null ? CompositeIndex.create(beanArchiveIndex, applicationIndex) : beanArchiveIndex;
    }

    public boolean isAssignableFrom(Type type1, Type type2) {
        return isAssignableFrom(type1.name(), type2.name());
    }

    public boolean isAssignableFrom(DotName type1, DotName type2) {
        // java.lang.Object is assignable from any type
        if (type1.equals(DOTNAME_OBJECT)) {
            return true;
        }
        // type1 is the same as type2
        if (type1.equals(type2)) {
            return true;
        }
        // type1 is a superclass
        return getSupertypes(type2).contains(type1);
    }

    private Set<DotName> getSupertypes(DotName name) {
        return cache.computeIfAbsent(name, this::findSupertypes);
    }

    private Set<DotName> findSupertypes(DotName name) {
        Set<DotName> result = new HashSet<>();

        Deque<DotName> workQueue = new ArrayDeque<>();
        workQueue.add(name);
        while (!workQueue.isEmpty()) {
            DotName type = workQueue.poll();
            if (result.contains(type)) {
                continue;
            }
            result.add(type);

            ClassInfo clazz = index.getClassByName(type);
            if (clazz == null) {
                continue;
            }
            if (clazz.superName() != null) {
                workQueue.add(clazz.superName());
            }
            workQueue.addAll(clazz.interfaceNames());
        }

        return result;
    }

}
