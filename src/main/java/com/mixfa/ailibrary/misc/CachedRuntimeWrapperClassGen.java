package com.mixfa.ailibrary.misc;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import net.bytebuddy.ByteBuddy;
import org.bson.types.ObjectId;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@UtilityClass
@SuppressWarnings("unchecked")
public class CachedRuntimeWrapperClassGen {
    private static final Map<Class, ClassAndGetter> cache = new ConcurrentHashMap<>();
    public static final String FIELD_NAME = "result";

    @Getter
    @Accessors(fluent = true)
    public class ClassAndGetter {
        private final Class tClass;
        private final MethodHandle getter;

        @SneakyThrows
        public ClassAndGetter(Class tClass, Class fieldType) {
            this.tClass = tClass;
            this.getter = MethodHandles.privateLookupIn(tClass, MethodHandles.lookup()).findGetter(tClass, FIELD_NAME, fieldType);
        }

        @SneakyThrows
        public <T> T get(Object target) {
            return (T) getter.bindTo(target).invoke();
        }
    }

    private static <T> ClassAndGetter makeWrapperClass(Class<T> fieldType) {
        var generatedClass = new ByteBuddy()
                .makeRecord()
                .name("GeneratedClass_" + ObjectId.get().toHexString())
                .defineRecordComponent(FIELD_NAME, fieldType)
                .make()
                .load(CachedRuntimeWrapperClassGen.class.getClassLoader())
                .getLoaded();

        return new ClassAndGetter(generatedClass, fieldType);
    }

    @SneakyThrows
    public static <T> ClassAndGetter get(Class<T> fieldType) {
        return cache.computeIfAbsent(fieldType, CachedRuntimeWrapperClassGen::makeWrapperClass);
    }
}
