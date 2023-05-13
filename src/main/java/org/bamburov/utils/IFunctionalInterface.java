package org.bamburov.utils;

@FunctionalInterface
public interface IFunctionalInterface<T, O> {
    O doRetryableAction(T input) throws Exception;
}
