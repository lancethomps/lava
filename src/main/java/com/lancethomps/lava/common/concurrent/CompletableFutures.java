package com.lancethomps.lava.common.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.lancethomps.lava.common.lambda.ThrowingSupplier;

public class CompletableFutures {

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
