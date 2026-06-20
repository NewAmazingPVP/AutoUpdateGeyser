package spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class SchedulerCompat {

    interface DelayScheduler {
        boolean schedule(Plugin plugin, Runnable task, long delaySeconds);
    }

    interface MainScheduler {
        void run(Plugin plugin, Runnable task);
    }

    private SchedulerCompat() {
    }

    static boolean isFoliaLike() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            Bukkit.class.getMethod("getAsyncScheduler");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    static void scheduleUpdateRepeating(Plugin plugin, Runnable task, long bootDelaySeconds, long intervalMinutes) {
        Objects.requireNonNull(plugin);
        Objects.requireNonNull(task);

        if (isFoliaLike()) {
            try {
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                Method runAtFixedRate = asyncScheduler.getClass().getMethod(
                        "runAtFixedRate",
                        Plugin.class,
                        java.util.function.Consumer.class,
                        long.class,
                        long.class,
                        TimeUnit.class
                );

                Consumer<Object> consumer = (ignored) -> task.run();
                runAtFixedRate.invoke(asyncScheduler, plugin, consumer, bootDelaySeconds, intervalMinutes * 60L, TimeUnit.SECONDS);
                return;
            } catch (Throwable ignored) {
            }
        }

        long initialDelayTicks = bootDelaySeconds * 20L;
        long periodTicks = intervalMinutes * 60L * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, initialDelayTicks, periodTicks);
    }

    static void scheduleConsoleRestart(Plugin plugin, Runnable task, long delaySeconds) {
        scheduleConsoleRestart(
                plugin,
                task,
                delaySeconds,
                SchedulerCompat::schedulePaperAsyncDelay,
                SchedulerCompat::scheduleThreadDelay,
                SchedulerCompat::runMain
        );
    }

    static void scheduleConsoleRestart(
            Plugin plugin,
            Runnable task,
            long delaySeconds,
            DelayScheduler realTimeScheduler,
            DelayScheduler fallbackScheduler,
            MainScheduler mainScheduler
    ) {
        Objects.requireNonNull(plugin);
        Objects.requireNonNull(task);
        Objects.requireNonNull(realTimeScheduler);
        Objects.requireNonNull(fallbackScheduler);
        Objects.requireNonNull(mainScheduler);

        Runnable restartTask = () -> mainScheduler.run(plugin, task);
        if (realTimeScheduler.schedule(plugin, restartTask, delaySeconds)) {
            return;
        }

        fallbackScheduler.schedule(plugin, restartTask, delaySeconds);
    }

    private static boolean schedulePaperAsyncDelay(Plugin plugin, Runnable task, long delaySeconds) {
        try {
            Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Method runDelayed = asyncScheduler.getClass().getMethod(
                    "runDelayed",
                    Plugin.class,
                    java.util.function.Consumer.class,
                    long.class,
                    TimeUnit.class
            );
            Consumer<Object> consumer = (ignored) -> task.run();
            runDelayed.invoke(asyncScheduler, plugin, consumer, Math.max(0L, delaySeconds), TimeUnit.SECONDS);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean scheduleThreadDelay(Plugin plugin, Runnable task, long delaySeconds) {
        Thread thread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(Math.max(0L, delaySeconds));
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "AutoUpdateGeyser-RestartDelay");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    static void runMain(Plugin plugin, Runnable task) {
        Objects.requireNonNull(plugin);
        Objects.requireNonNull(task);
        if (isFoliaLike()) {
            try {
                Object globalScheduler;
                try {
                    Method m = Bukkit.class.getMethod("getGlobalRegionScheduler");
                    globalScheduler = m.invoke(null);
                } catch (NoSuchMethodException e) {
                    Method serverGetter = Bukkit.class.getMethod("getServer");
                    Object server = serverGetter.invoke(null);
                    Method m = server.getClass().getMethod("getGlobalRegionScheduler");
                    globalScheduler = m.invoke(server);
                }
                Method run = globalScheduler.getClass().getMethod(
                        "run",
                        Plugin.class,
                        java.util.function.Consumer.class
                );
                Consumer<Object> consumer = (ignored) -> task.run();
                run.invoke(globalScheduler, plugin, consumer);
                return;
            } catch (Throwable ignored) {
            }
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
