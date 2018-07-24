package com.github.lancethomps.lava.common.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.github.lancethomps.lava.common.lambda.ThrowingSupplier;

/**
 * The Class CompletableFutures.
 */
public class CompletableFutures {

	/**
	 * Supply async with exception handling.
	 *
	 * @param <T> the generic type
	 * @param supplier the supplier
	 * @param executor the executor
	 * @return the completable future
	 */
	public static <T> CompletableFuture<T> supplyAsyncWithExceptionHandling(ThrowingSupplier<T> supplier, Executor executor) {
		CompletableFuture<T> future = new CompletableFuture<>();
		executor.execute(() -> {
			try {
				future.complete(supplier.get());
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
		});
		return future;
	}

}
