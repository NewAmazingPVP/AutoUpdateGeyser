package spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class SchedulerCompat {

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

                Method runDelayed = globalScheduler.getClass().getMethod(
                        "runDelayed",
                        Plugin.class,
                        java.util.function.Consumer.class,
                        long.class
                );
                Consumer<Object> consumer = (ignored) -> task.run();
                runDelayed.invoke(globalScheduler, plugin, consumer, delaySeconds * 20L);
                return;
            } catch (Throwable ignored) {
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, delaySeconds * 20L);
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
