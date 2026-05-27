package test.orm.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
@Slf4j
public class Tasks {
    @SafeVarargs
    public static <T> T any(int threadNumber, T defaultValue, Callable<T>... tasks) {
        return any(threadNumber, defaultValue, Arrays.asList(tasks));
    }

    public static <T> T any(int threadNumber, T defaultValue, Collection<Callable<T>> tasks) {
        ForkJoinPool forkJoinPool = getForkJoinPool(threadNumber);
        T t;
        try {
            t = forkJoinPool.invokeAny(tasks);
        } catch (Exception e) {
            log.error(" error:{}", e.getMessage(), e);
            t = defaultValue;
        } finally {
            forkJoinPool.shutdownNow();
        }
        return t;
    }

    @NotNull
    private static ForkJoinPool getForkJoinPool(int threadNumber) {
        ForkJoinPool.ForkJoinWorkerThreadFactory f = pool -> {
            ForkJoinWorkerThread worker = new ForkJoinWorkerThread(pool) {
            };
            worker.setName("Tasks-" + worker.getPoolIndex());
            return worker;
        };
        return new ForkJoinPool(
                threadNumber,
                                f,
                                (t, e) -> log.error("{} error:{}", t.getName(), e.getMessage(), e),
                                true
        );
    }

    @SafeVarargs
    public static <T> T any(int threadNumber, T defaultValue, int timeout, TimeUnit timeUnit, Callable<T>... tasks) {
        return any(threadNumber, defaultValue, timeout, timeUnit, Arrays.asList(tasks));
    }

    public static <T> T any(int threadNumber, T defaultValue, int timeout, TimeUnit timeUnit, List<Callable<T>> tasks) {
        ForkJoinPool forkJoinPool = getForkJoinPool(threadNumber);
        T t;
        try {
            t = forkJoinPool.invokeAny(tasks, timeout, timeUnit);
        } catch (Exception e) {
            log.error(" error:{}", e.getMessage(), e);
            t = defaultValue;
        } finally {
            forkJoinPool.shutdownNow();
        }
        return t;
    }

    public static <T> List<T> all(int threadNumber, T defaultValue, int timeout, TimeUnit timeUnit,
            List<Callable<T>> tasks
    ) {
        ForkJoinPool forkJoinPool = getForkJoinPool(threadNumber);
        List<T> t = new ArrayList<>(tasks.size());
        try {
            List<Future<T>> futures = forkJoinPool.invokeAll(tasks, timeout, timeUnit);
            for (Future<T> future : futures) {
                T e = Optional.ofNullable(future.get()).orElse(defaultValue);
                t.add(e);
            }
        } catch (Exception e) {
            log.error(" error:{}", e.getMessage(), e);
        } finally {
            forkJoinPool.shutdownNow();
        }
        return t;
    }

    public static <T> List<T> all(int threadNumber, T defaultValue, int timeout, TimeUnit timeUnit,
            Callable<T>... tasks
    ) {
        return all(threadNumber, defaultValue, timeout, timeUnit, Arrays.asList(tasks));
    }

    public static <T> List<T> all(int threadNumber, T defaultValue, List<Callable<T>> tasks) {
        ForkJoinPool forkJoinPool = getForkJoinPool(threadNumber);
        List<T> t = new ArrayList<>(tasks.size());
        try {
            List<Future<T>> futures = forkJoinPool.invokeAll(tasks);
            for (Future<T> future : futures) {
                T e = Optional.ofNullable(future.get()).orElse(defaultValue);
                t.add(e);
            }
        } catch (Exception e) {
            log.error(" error:{}", e.getMessage(), e);
        } finally {
            forkJoinPool.shutdownNow();
        }
        return t;
    }

    public static <T> List<T> all(int threadNumber, T defaultValue, Callable<T>... tasks) {
        return all(threadNumber, defaultValue, Arrays.asList(tasks));
    }

    @SneakyThrows
    public static void main(String[] args) {
        IntStream
                .range(1, 1001)
                .parallel()
                .forEach(i -> System.out.println(i +
                                                         " - " +
                                                         all(
                                                                 3,
                                                                 "000",
                                                                 () -> sleep("1"),
                                                                 () -> sleep("2"),
                                                                 () -> sleep("3")
                                                         )))
        ;
        Thread.sleep(30000);
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    public static String sleep(String x) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        return x;
    }
}
