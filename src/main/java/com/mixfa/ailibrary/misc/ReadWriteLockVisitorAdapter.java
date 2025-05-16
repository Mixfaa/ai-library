package com.mixfa.ailibrary.misc;


import org.apache.commons.lang3.concurrent.locks.LockingVisitors;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;

public interface ReadWriteLockVisitorAdapter<O> {
    LockingVisitors.ReadWriteLockVisitor<O> getReadWriteLockVisitor();

    default void acceptReadLocked(final FailableConsumer<O, ?> consumer) {
        getReadWriteLockVisitor().acceptReadLocked(consumer);
    }

    default void acceptWriteLocked(final FailableConsumer<O, ?> consumer) {
        getReadWriteLockVisitor().acceptWriteLocked(consumer);
    }

    default <T> T applyReadLocked(final FailableFunction<O, T, ?> function) {
        return getReadWriteLockVisitor().applyReadLocked(function);
    }

    default <T> T applyWriteLocked(final FailableFunction<O, T, ?> function) {
        return getReadWriteLockVisitor().applyWriteLocked(function);
    }
}
