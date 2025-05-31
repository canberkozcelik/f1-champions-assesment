package com.f1champions.domain.usecase

/**
 * Base interface for all use cases in the domain layer.
 * This interface defines the contract for use cases that return a result of type [T].
 *
 * @param T The type of result that the use case returns
 * @param P The type of parameters that the use case requires
 */
interface UseCase<T, P> {
    /**
     * Executes the use case with the given parameters.
     *
     * @param params The parameters required by the use case
     * @return The result of the use case execution
     * @throws com.f1champions.domain.exception.F1Exception if there's an error during execution
     */
    suspend operator fun invoke(params: P): T
}

/**
 * Base interface for use cases that don't require parameters.
 *
 * @param T The type of result that the use case returns
 */
interface NoParamsUseCase<T> {
    /**
     * Executes the use case.
     *
     * @return The result of the use case execution
     * @throws com.f1champions.domain.exception.F1Exception if there's an error during execution
     */
    suspend operator fun invoke(): T
} 