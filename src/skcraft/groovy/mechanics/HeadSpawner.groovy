/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package mechanics

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.io.Closer
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import com.sk89q.minecraft.util.commands.CommandException
import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BindingGuard
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.ChatUtil
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.annotation.Default
import com.sk89q.rebar.util.command.annotation.Sender
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

import javax.annotation.Nullable
import java.util.concurrent.*
import java.util.regex.Matcher
import java.util.regex.Pattern

import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

class HeadSpawner extends AbstractCapsule implements Listener {

    private static final Pattern VALID_HEAD_SITES = Pattern.compile("https?://(?:www\\.)?(?:minecraft-heads\\.com|heads\\.freshcoal\\.com)/", Pattern.CASE_INSENSITIVE);

    private static final Pattern SKULL_PATTERN = Pattern.compile("/give @p skull 1 3 (\\{[^<]+\\})")
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("Name *: *\"([^\"\\\\]+)\"");
    private static final Pattern ID_PATTERN = Pattern.compile("Id *: *\"([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\"");
    private static final Pattern TEXTURE_PATTERN = Pattern.compile("Value *: *\"([A-Z0-9a-z\\-=]+)\"");

    private final BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>(4);
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(1, 10, 30,
            TimeUnit.SECONDS, linkedBlockingDeque, new ThreadPoolExecutor.CallerRunsPolicy()));
    private final MainThreadExecutor mainThreadExecutor = new MainThreadExecutor();

    private final LoadingCache<URL, String> urlCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .build(new CacheLoader<URL, String>() {
                @Override
                String load(URL url) throws Exception {
                    Closer closer = Closer.create();
                    URLConnection connection = url.openConnection();

                    try {
                        InputStream is = closer.register(connection.getInputStream());
                        InputStreamReader isr = closer.register(new InputStreamReader(is));
                        BufferedReader br = closer.register(new BufferedReader(isr));

                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }

                        return response.toString();
                    } finally {
                        closer.close();
                    }
                }
            });

    @Override
    void preBind() {
        System.out.println("SKCraft Head Spawner r24 loading...");

        BindingGuard guard = getGuard();
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), fromMethods(this, provider));
        BukkitBindings.bindListeners(guard, this);

        guard.add(new Runnable() {
            @Override
            public void run() {
                executorService.shutdown();
            }
        });
    }

    @Command(permit = "*")
    public void head(@Sender Player player, String url, @Default("1") String indexStr) {
        if (!VALID_HEAD_SITES.matcher(url).find()) {
            throw new CommandException("That URL is not on the whitelist.");
        }

        System.out.println(player.getName() + " is fetching a player head from " + url + "...");
        ChatUtil.msg(player, ChatColor.YELLOW, "Fetching head from " + url + "...");

        try {
            int index = Integer.parseInt(indexStr);

            Futures.addCallback(executorService.submit(new HeadFetcher(player, new URL(url), index)), new FutureCallback<String>() {
                @Override
                void onSuccess(@Nullable String result) {
                    if (result != null) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), result);
                    } else {
                        ChatUtil.error(player, "No head spawn code at the given index could be found on the given page.");
                    }
                }

                @Override
                void onFailure(Throwable t) {
                    ChatUtil.error(player, "Failed to get head: " + t.getMessage());
                }
            }, mainThreadExecutor);
        } catch (RejectedExecutionException e) {
            throw new CommandException("Too many ongoing head requests at the moment. Try again later.");
        } catch (NumberFormatException e) {
            throw new CommandException("Use a number for the index.");
        } catch (MalformedURLException e) {
            throw new CommandException("Invalid URL provided.");
        }
    }

    private static class MainThreadExecutor implements Executor {
        @Override
        void execute(Runnable command) {
            Rebar.getInstance().registerTimeout(command, 0);
        }
    }

    private class HeadFetcher implements Callable<String> {
        private final Player player;
        private final URL url;
        private final int index;

        HeadFetcher(Player player, URL url, int index) {
            this.player = player
            this.url = url
            this.index = index
        }

        @Override
        public String call() {
            String response = urlCache.getUnchecked(url);

            Matcher m = SKULL_PATTERN.matcher(response);
            int currentIndex = 1;
            while (m.find()) {
                if (index == currentIndex) {
                    Matcher nameMatcher = DISPLAY_NAME_PATTERN.matcher(m.group(1));
                    Matcher idMatcher = ID_PATTERN.matcher(m.group(1));
                    Matcher textureMatcher = TEXTURE_PATTERN.matcher(m.group(1));

                    if (nameMatcher.find() && idMatcher.find() && textureMatcher.find()) {
                        return String.format("give %s skull 1 3 {display:{Name:\"%s\"},SkullOwner:{Id:\"%s\",Properties:{textures:[{Value:\"%s\"}]}}}",
                                player.getName(), nameMatcher.group(1), idMatcher.group(1), textureMatcher.group(1));
                    }
                }

                currentIndex++;
            }

            return null;
        }
    }

}
