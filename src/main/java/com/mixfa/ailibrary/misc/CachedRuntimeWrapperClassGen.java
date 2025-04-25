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

    @Getter
    @Accessors(fluent = true)
    public class ClassAndGetter {
        private final Class tClass;
        private final MethodHandle getter;

        @SneakyThrows
        public ClassAndGetter(Class tClass, Class fieldType) {
            this.tClass = tClass;
            this.getter = MethodHandles.privateLookupIn(tClass, MethodHandles.lookup()).findGetter(tClass, "result", fieldType);
        }

        @SneakyThrows
        public <T> T get(Object target) {
            return (T) getter.bindTo(target).invoke();
        }
    }

    @SneakyThrows
    public static <T> ClassAndGetter get(Class<T> fieldType) {
        var cached = cache.get(fieldType);
        if (cached != null)
            return cached;

        var tClass = new ByteBuddy()
                .makeRecord()
                .name("GeneratedClass_" + ObjectId.get().toHexString())
                .defineRecordComponent("result", fieldType)
                .make()
                .load(CachedRuntimeWrapperClassGen.class.getClassLoader())
                .getLoaded();

        cached = new ClassAndGetter(tClass, fieldType);
        cache.put(fieldType, cached);

        return cached;
    }
}
