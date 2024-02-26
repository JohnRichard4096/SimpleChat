package com.simplechat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
        getLogger().warning("这是短期支持开发测试版！请查看github.com/JohnRichard4096/SimpleChat 以获取最新版本。");
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
        // 在创建badwords.yml文件时检查是否导入内置违禁词列表
        try {
            if (banWordsFile.createNewFile()) {
                if (getConfig().getBoolean("banConfiguration.importDefaultBadWords", true)) {
                    // 导入内置违禁词列表
                    List<String> defaultBadWords = Arrays.asList("badword1", "badword2", "badword3");
                    banWordsConfig.set("forbiddenWords", defaultBadWords);
                    try {
                        banWordsConfig.save(banWordsFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                // 文件已存在，加载现有的违禁词列表
                loadBanWords();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void  onDisable(){
        getLogger().info("SimpleChat插件已卸载！");
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
        }

        // 根据配置决定是否加载默认敏感词
        if (getConfig().getBoolean("banConfiguration.enableDefaultBadWords", true) && getConfig().getBoolean("banConfiguration.importDefaultBadWords", true)) {
            importDefaultBadWords();
        }
    }





    private void importDefaultBadWords() {
        if (getConfig().getBoolean("banConfiguration.enableDefaultBadWords", true)) {
            InputStream badWordsStream = getClass().getResourceAsStream("/systembadword.txt");

            if (badWordsStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(badWordsStream))) {
                    List<String> defaultBadWords = Arrays.asList(reader.readLine().split(","));
                    for (String word : defaultBadWords) {
                        String newWord = word.trim().toLowerCase();
                        if (!forbiddenWords.contains(newWord)) {
                            forbiddenWords.add(newWord);
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
            if (sender.hasPermission("schat.mute")) {
                if (args.length >= 1) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        // 获取禁言原因和时长
                        String reason = args.length >= 2 ? args[1] : "无";
                        int duration = args.length >= 3 ? Integer.parseInt(args[2]) : -1; // 默认为永久禁言

                        // 执行禁言逻辑
                        // 例如：schatMutePlayer(target, reason, duration);

                        sender.sendMessage("已禁言玩家 " + target.getName() + "，原因: " + reason + "，时长: " + (duration == -1 ? "永久" : duration + "分钟"));
                    } else {
                        sender.sendMessage("玩家 " + args[0] + " 不在线或不存在.");
                    }
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
                        // 在这里重新实现禁言逻辑，保留原来的禁言时间
                        if (playerViolationCount.containsKey(targetPlayer)) {
                            int duration = playerViolationCount.get(targetPlayer);
                            // 实现重新禁言逻辑，保留原来的禁言时间
                            // 例如：schatMutePlayer(targetPlayer, "重新禁言", duration);
                            sender.sendMessage("成功重新禁言玩家 " + targetPlayer + "，时长: " + (duration == -1 ? "永久" : duration + "分钟"));
                        } else {
                            sender.sendMessage("玩家 " + targetPlayer + " 没有被禁言.");
                        }
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
            if (sender.hasPermission("schat.unmute")) {
                if (args.length == 1) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        // 执行解除禁言逻辑
                        playerViolationCount.put(target.getName(), 0); // 将该玩家的违规次数设为0，解除禁言

                        sender.sendMessage("已解除玩家 " + target.getName() + " 的禁言.");
                    } else {
                        sender.sendMessage("玩家 " + args[0] + " 不在线或不存在.");
                    }
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

                        int pageSize = 20;
                        int totalPages = (int) Math.ceil((double) forbiddenWords.size() / pageSize);

                        int startIndex = 0;
                        int endIndex = Math.min(startIndex + pageSize, forbiddenWords.size());

                        List<String> wordsToShow = forbiddenWords.subList(startIndex, endIndex);
                        String badWords = String.join(", ", wordsToShow);

                        sender.sendMessage("第 1 页 / 共 " + totalPages + " 页");
                        sender.sendMessage("违禁词: " + badWords);
                    } else {
                        sender.sendMessage("违禁词读取失败.");
                    }
                } else if (args.length == 2 && args[0].equalsIgnoreCase("page")) {
                    if (banWordsFile.exists()) {
                        YamlConfiguration banWordsConfig = YamlConfiguration.loadConfiguration(banWordsFile);
                        List<String> forbiddenWords = banWordsConfig.getStringList("forbiddenWords");

                        int pageSize = 20;
                        int totalPages = (int) Math.ceil((double) forbiddenWords.size() / pageSize);

                        int page;
                        try {
                            page = Integer.parseInt(args[1]);
                            if (page < 1 || page > totalPages) {
                                sender.sendMessage("无效的页数. 请输入 1 到 " + totalPages + " 之间的页数.");
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage("请输入有效的页数.");
                            return true;
                        }

                        int startIndex = (page - 1) * pageSize;
                        int endIndex = Math.min(startIndex + pageSize, forbiddenWords.size());

                        List<String> wordsToShow = forbiddenWords.subList(startIndex, endIndex);
                        String badWords = String.join(", ", wordsToShow);

                        sender.sendMessage("第 " + page + " 页 / 共 " + totalPages + " 页");
                        sender.sendMessage("违禁词: " + badWords);
                    } else {
                        sender.sendMessage("违禁词读取失败.");
                    }
                } else {
                    sender.sendMessage("Usage: /schat-list page <页数>");
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
                    if (!forbiddenWords.contains(newWord)) {
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
            sender.sendMessage("/schat-list [page <num>] 指定页数跳转");

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
