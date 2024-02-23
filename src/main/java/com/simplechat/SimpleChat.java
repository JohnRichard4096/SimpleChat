package com.simplechat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.*;
import java.util.*;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;


public class SimpleChat extends JavaPlugin implements Listener {
    private File banWordsFile;
    private FileConfiguration banWordsConfig;
    private List<String> forbiddenWords;
    private Map<String, Integer> playerViolationCount = new HashMap<>();
    private int violationThreshold;
    private int banDuration;
    private static final Logger logger = Logger.getLogger("SimpleChat");
    @Override
    public void onEnable() {

        loadConfig();
        loadBanWords();
        InputStream inputStream = getClass().getResourceAsStream("/badwords.yml");
        File badWordFile = new File(getDataFolder(), "systembadword.txt");
        if (badWordFile.exists()) {
            badWordFile.delete();
        }

        saveResource("systembadword.txt", false);
        logger.info("Created systembadword.txt");
        banWordsFile = new File(getDataFolder(), "badwords.yml");
        getLogger().warning("您正在使用开发版本！");
        getServer().getPluginManager().registerEvents(this, this);
        try {
            File logsFolder = new File(getDataFolder(), "logs");
            if (!logsFolder.exists()) {
                logsFolder.mkdirs();
            }
            FileHandler fileHandler = new FileHandler(logsFolder.getPath() + File.separator + "logs.log", true);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadBanWords() {
        banWordsFile = new File(getDataFolder(), "badwords.yml");
        if (banWordsFile.exists()) {
            banWordsConfig = YamlConfiguration.loadConfiguration(banWordsFile);
            forbiddenWords = banWordsConfig.getStringList("forbiddenWords");
        } else {
            getLogger().warning("未找到 'badwords.yml' 文件，将创建新的文件.");
            banWordsConfig = new YamlConfiguration();
            saveResource("badwords.yml", false);
            forbiddenWords = banWordsConfig.getStringList("forbiddenWords");

            // 根据配置决定是否加载默认敏感词
            if (getConfig().getBoolean("banConfiguration.enableDefaultBadWords", true)) {
                importDefaultBadWords();
            }
        }

        // 根据配置决定是否导入内置资源到badwords.yml
        if (getConfig().getBoolean("banConfiguration.importDefaultBadWords", true)) {
            importDefaultBadWords();
        }
    }

    private void importDefaultBadWords() {
        if (getConfig().getBoolean("enableDefaultBadWords", true)) {
            InputStream badWordsStream = getClass().getResourceAsStream("/systembadword.txt");

            if (badWordsStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(badWordsStream))) {
                    List<String> defaultBadWords = Arrays.asList(reader.readLine().split(","));
                    for (String word : defaultBadWords) {
                        if (!forbiddenWords.contains(word.toLowerCase())) {
                            forbiddenWords.add(word.toLowerCase());
                        }
                    }
                    banWordsConfig.set("forbiddenWords", forbiddenWords);
                    banWordsConfig.save(banWordsFile);
                } catch (IOException e) {
                    getLogger().warning("无法读取系统默认敏感词文件.");
                }
            } else {
                getLogger().warning("无法加载系统默认敏感词文件 'systembadword.txt'");
            }
        }
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        violationThreshold = config.getInt("banConfiguration.violationThreshold", 5);
        banDuration = config.getInt("banConfiguration.banDuration", 6000);
    }





    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String playerName = event.getPlayer().getName();

        for (String word : forbiddenWords) {
            if (message.toLowerCase().contains(word.toLowerCase())) {
                int violations = playerViolationCount.getOrDefault(playerName, 0);
                violations++;
                playerViolationCount.put(playerName, violations);

                // Log the violation with timestamp
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = sdf.format(new Date());
                logger.info(playerName + " violated chat rules by using the word: " + word + " at " + timestamp);

                if (violations >= violationThreshold) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("你被禁言了.");
                    getServer().getScheduler().runTaskLater(this, () -> {
                        playerViolationCount.put(playerName, 0);
                        event.getPlayer().sendMessage("你已经被解除禁言了.");
                    }, banDuration);

                    // Revoke forbidden message
                    String censoredMessage = message.replaceAll("(?i)" + word, "*censored*");
                    event.setMessage(censoredMessage);
                }
                return;
            }
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("schat-mute")) {
            // 处理禁言命令的逻辑
            if (sender.hasPermission("schat.mute")) {
                if (args.length >= 1) {
                    // 禁言玩家的逻辑
                } else {
                    sender.sendMessage("Usage: /schat-mute <playerName> [reason] [duration]");
                }
                return true;
            } else {
                sender.sendMessage("你没有执行的权限 (schat.mute).");
                return true;
            }
        }
        if (label.equalsIgnoreCase("schat-undo")) {
            if (sender.isOp()) {
                if (args.length >= 2) {
                    String action = args[0];
                    String targetPlayer = args[1];

                    if (action.equalsIgnoreCase("mute")) {
                        // 撤销禁言逻辑
                        if (playerViolationCount.containsKey(targetPlayer)) {
                            playerViolationCount.remove(targetPlayer);
                            sender.sendMessage("成功撤销玩家 " + targetPlayer + " 的禁言.");
                        } else {
                            sender.sendMessage("玩家 " + targetPlayer + " 没有被禁言.");
                        }
                    } else if (action.equalsIgnoreCase("unmute")) {
                        // 解禁逻辑
                        // 实现解禁逻辑，例如移除玩家的禁言状态
                    } else if (action.equalsIgnoreCase("restore")) {
                        // 还原违禁词列表逻辑
                        forbiddenWords = banWordsConfig.getStringList("forbiddenWords");
                        sender.sendMessage("成功还原违禁词列表.");
                    } else {
                        sender.sendMessage("未知操作，请输入 'mute', 'unmute' 或 'restore'.");
                    }
                } else {
                    sender.sendMessage("用法: /schat-undo <action> <player>");
                }
                return true;
            } else {
                sender.sendMessage("只有OP才能执行该命令.");
                return true;
            }
        }

        if (label.equalsIgnoreCase("schat-unmute")) {
            // 处理解除禁言命令的逻辑
            if (sender.hasPermission("schat.unmute")) {
                if (args.length == 1) {
                    // 解除禁言玩家的逻辑
                } else {
                    sender.sendMessage("Usage: /schat-unmute <playerName>");
                }
                return true;
            } else {
                sender.sendMessage("你没有执行的权限 (schat.unmute).");
                return true;
            }
        }

        if (label.equalsIgnoreCase("schat-list")) {
            if (sender.isOp()) {
                if (args.length == 0) {
                    if (banWordsFile.exists()) {
                        YamlConfiguration banWordsConfig = YamlConfiguration.loadConfiguration(banWordsFile);
                        List<String> forbiddenWords = banWordsConfig.getStringList("forbiddenWords");
                        String badWords = String.join(", ", forbiddenWords);
                        sender.sendMessage("目前违禁词: " + badWords);
                    } else {
                        sender.sendMessage("违禁词读取失败.");
                    }
                } else {
                    sender.sendMessage("Usage: /schat-list");
                }
                return true;
            } else {
                sender.sendMessage("您需要op权限.");
                return true;
            }
        }


        if (label.equalsIgnoreCase("schat-reload")) {
            if (sender.hasPermission("schat.reload")) {
                loadConfig();
                loadBanWords();
                sender.sendMessage("重载完成.");
                File badWordFile = new File(getDataFolder(), "systembadword.txt");
                if (badWordFile.exists()) {
                    badWordFile.delete();
                }

                saveResource("systembadword.txt", false);
                logger.info("Created systembadword.txt");
            } else {
                sender.sendMessage("你没有权限执行命令 (schat.reload).");
            }
            return true;
        }
        if (label.equalsIgnoreCase("schat-addbadword")) {
            if (sender.hasPermission("schat.addbadword")) {
                if (args.length == 1) {
                    String newWord = args[0].toLowerCase();
                    if (forbiddenWords.contains(newWord)) {
                        sender.sendMessage("违禁词 '" + newWord + "' 已经存在于列表中.");
                    } else {
                        forbiddenWords.add(newWord);
                        banWordsConfig.set("forbiddenWords", forbiddenWords);
                        try {
                            banWordsConfig.save(banWordsFile);
                            sender.sendMessage("新增违禁词 '" + newWord + "' 到列表.");
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage("在存储时发生错误！");
                        }
                    }
                } else {
                    sender.sendMessage("用法: /schat-addbadword <word>");
                }
                return true;
            } else {
                sender.sendMessage("你没有权限 (schat.addbadword).");
                return true;
            }
        }

        if (label.equalsIgnoreCase("schat")) {
            sender.sendMessage("Schat V1.00-DEV-SNAPSHOT");
            sender.sendMessage("By JohnRicahrd");
            sender.sendMessage("帮助：/schat 调出本界面");
            sender.sendMessage("/schat-addbadword 添加违禁词");
            sender.sendMessage("/schat-delbadword 删除违禁词");
            sender.sendMessage("/schat-reload 重载插件");
            sender.sendMessage("/schat-list 列出违禁词");
            sender.sendMessage("/schat-mute <player> [reason] [time] 禁言玩家");
            sender.sendMessage("/schat-unmute <player> 为某个玩家解除禁言");
            sender.sendMessage("/schat-undo <action> <player> 用于回溯操作");

        }
        if (label.equalsIgnoreCase("reload")) {
            getLogger().warning("您似乎在尝试重载服务器，但是重载后带来的一系列问题SChat团队对此不负责任");
        }
        if (label.equalsIgnoreCase("reload confirm")) {
            getLogger().warning("您似乎在尝试重载服务器，但是重载后带来的一系列问题SChat团队对此不负责任");
        }

        if (label.equalsIgnoreCase("schat-delbadword")) {
            if (sender.hasPermission("schat.delbadword")) {
                if (args.length == 1) {
                    String wordToRemove = args[0].toLowerCase();
                    if (forbiddenWords.contains(wordToRemove)) {
                        forbiddenWords.remove(wordToRemove);
                        banWordsConfig.set("forbiddenWords", forbiddenWords);
                        try {
                            banWordsConfig.save(banWordsFile);
                            sender.sendMessage("成功从违禁词列表中移除 '" + wordToRemove + "'.");
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage("保存 badwords.yml 文件时发生错误.");
                        }
                    } else {
                        sender.sendMessage("单词 '" + wordToRemove + "' 不在违禁词列表中.");
                    }
                } else {
                    sender.sendMessage("用法: /schat-delbadword <word>");
                }
                return true;
            } else {
                sender.sendMessage("你没有权限 (schat.delbadword).");
                return true;
            }
        }



        return false;
    }

}
