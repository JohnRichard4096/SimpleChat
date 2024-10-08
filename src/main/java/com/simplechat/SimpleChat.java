package com.simplechat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;


public class SimpleChat extends JavaPlugin implements Listener {
    protected static File banWordsFile;
    protected static Map<String, Boolean> playerMutedStatus = new HashMap<>();
    protected static FileConfiguration banWordsConfig;
    protected static List<String> forbiddenWords;
    protected static Map<String, Integer> playerViolationCount = new HashMap<>();
    protected int violationThreshold;
    protected int banDuration;
    protected static String LanguageFile ="Language/messages_en_global";
    protected static String LanguageConfig;
    protected ResourceBundle bundle;
    private static final String REQUIRED_VERSION_STRING = "1.17"; // 最低运行版本

    protected int Version = 33;
    protected final static Logger logger = Logger.getLogger("SimpleChat");




    @Override
    public void onEnable() {
        System.out.println("""
                
                 ██████████████ ██████████ ██████          ██████ ██████████████ ██████         ██████████████ 
                 ██          ██ ██      ██ ██  ██████████████  ██ ██          ██ ██  ██         ██          ██ 
                 ██  ██████████ ████  ████ ██                  ██ ██  ██████  ██ ██  ██         ██  ██████████ 
                 ██  ██           ██  ██   ██  ██████  ██████  ██ ██  ██  ██  ██ ██  ██         ██  ██         
                 ██  ██████████   ██  ██   ██  ██  ██  ██  ██  ██ ██  ██████  ██ ██  ██         ██  ██████████ 
                 ██          ██   ██  ██   ██  ██  ██  ██  ██  ██ ██          ██ ██  ██         ██          ██ 
                 ██████████  ██   ██  ██   ██  ██  ██████  ██  ██ ██  ██████████ ██  ██         ██  ██████████ 
                         ██  ██   ██  ██   ██  ██          ██  ██ ██  ██         ██  ██         ██  ██         
                 ██████████  ██ ████  ████ ██  ██          ██  ██ ██  ██         ██  ██████████ ██  ██████████ 
                 ██          ██ ██      ██ ██  ██          ██  ██ ██  ██         ██          ██ ██          ██ 
                 ██████████████ ██████████ ██████          ██████ ██████         ██████████████ ██████████████ 
                                                                                                               
                                                                             
                 ██████████████ ██████  ██████ ██████████████ ██████████████ 
                 ██          ██ ██  ██  ██  ██ ██          ██ ██          ██ 
                 ██  ██████████ ██  ██  ██  ██ ██  ██████  ██ ██████  ██████ 
                 ██  ██         ██  ██  ██  ██ ██  ██  ██  ██     ██  ██     
                 ██  ██         ██  ██████  ██ ██  ██████  ██     ██  ██     
                 ██  ██         ██          ██ ██          ██     ██  ██     
                 ██  ██         ██  ██████  ██ ██  ██████  ██     ██  ██     
                 ██  ██         ██  ██  ██  ██ ██  ██  ██  ██     ██  ██     
                 ██  ██████████ ██  ██  ██  ██ ██  ██  ██  ██     ██  ██     
                 ██          ██ ██  ██  ██  ██ ██  ██  ██  ██     ██  ██     
                 ██████████████ ██████  ██████ ██████  ██████     ██████     
                                                                             
                Build 33
                Now loading......
                Please download the latest build in https://github.com/JohnRichard4096/SimpleChat
                """);
        String serverInfo = Bukkit.getMinecraftVersion();

        int mainVersion = Integer.parseInt(serverInfo.split("\\.")[1]);
        // 检查是否兼容  
        if (mainVersion<=16){
            logger.warning("Error!SimpleChat must running on bukkit server version after 1.17+");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        logger.info("Plugin is running at "+serverInfo+" a minecraft server.");


        loadConfig();
        bundle = ResourceBundle.getBundle(LanguageFile);

        /*
        saveResourceToFile("SimpleChat/Language/messages_zh_CN.properties", "Language/messages_zh_CN.properties");
        saveResourceToFile("SimpleChat/Language/messages_en_global.properties", "Language/messages_en_global.properties");

         */

        saveResource("Language/messages_zh_CN.properties", true);
        saveResource("Language/messages_en_global.properties", true);
        saveResource("Language/messages_ru_RU.properties",true);
        // 打印操作系统信息
        //command.enable();

       // Updater Updater = new Updater();	//实例化 类
        logger.info("Build Version:" + Version);

        
        FileConfiguration config = getConfig();
        String versionUrl = "http://micro-wave.cc:58080/job/SimpleChat/lastBuild/buildNumber";
        LanguageConfig = config.getString("banConfiguration.Language");
        if(Objects.equals(LanguageConfig, "zh_CN")){
            LanguageFile =  "Language/messages_zh_CN";

        }
        else if (Objects.equals(LanguageConfig, "en_global")){
            LanguageFile = "Language/messages_en_global";
        } else if (Objects.equals(LanguageConfig,"ru_RU")) {
            LanguageFile = "Language/messages_ru_RU";
        } else {
            logger.warning("Wrong language in 'config.yml'!");
            LanguageFile = "Language/messages_en_global";
        }

        loadBanWords();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {

            try {
                URL url = new URL(versionUrl);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    String versionStr = reader.readLine();
                    if (versionStr != null) {
                        int version = Integer.parseInt(versionStr);
                        if (version > Version) {
                            getLogger().warning(bundle.getString("URAlready") + (version - Version) + bundle.getString("BuildVersion"));
                        } else if (version < Version) {
                            getLogger().warning(bundle.getString("WhatThis"));
                        } else {
                            getLogger().info(bundle.getString("NewestVersion"));
                        }
                    } else {
                        getLogger().warning(bundle.getString("CannotGetVersion"));
                    }
                } catch (IOException | NumberFormatException e) {
                    getLogger().warning(bundle.getString("unableToRead"));
                }
            } catch (MalformedURLException e) {
                getLogger().warning(bundle.getString("unableToGet"));
            }
            Updater.UseUpdater();
        });
        executor.shutdown();


        saveResource("buildInBadWords.txt", true);
        deCode();

        logger.info(bundle.getString("SystemTXT"));

        banWordsFile = new File(getDataFolder(), "badwords.yml");
        getLogger().info(bundle.getString("BoardCast-Release"));
        getServer().getPluginManager().registerEvents(this, this);
        //enable.Enable();
        /*
        getCommand("schat").setExecutor(this);
        getCommand("schat").setTabCompleter((TabCompleter) this);

        // 其他命令
        getCommand("schat-mute").setExecutor(this);
        getCommand("schat-unmute").setExecutor(this);
        getCommand("schat-undo").setExecutor(this);
        getCommand("schat-list").setExecutor(this);
        getCommand("schat-reload").setExecutor(this);
        getCommand("schat-addbadword").setExecutor(this);
        getCommand("schat-delbadword").setExecutor(this);

         */

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
        loadBanWords();
        importDefaultBadWords();
    logger.info(bundle.getString("OnEnable"));
    }

    private boolean isCompatible(String serverInfo) {
        String[] serverParts = serverInfo.split("\\.");
        String[] requiredParts = REQUIRED_VERSION_STRING.split("\\.");

        for (int i = 0; i < Math.max(serverParts.length, requiredParts.length); i++) {
            int serverPart = i < serverParts.length ? Integer.parseInt(serverParts[i]) : 0;
            int requiredPart = i < requiredParts.length ? Integer.parseInt(requiredParts[i]) : 0;

            if (serverPart < requiredPart) {
                return false;
            } else if (serverPart > requiredPart) {
                return true;
            }
        }
        return true; // 版本相同

}

    /*
        protected void saveResourceToFile(String resourceName, String targetPath) {
            File targetFile = new File(getDataFolder().getParentFile(), targetPath);
            if (targetFile.exists()) {
                getLogger().info("Target file already exists: " + targetFile.getAbsolutePath());
                return;
            }
    
            InputStream inputStream = getResource(resourceName);
            if (inputStream == null) {
                getLogger().warning("Resource not found: " + resourceName);
                return;
            }
    
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
    
            try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                getLogger().info("Resource extracted to: " + targetFile.getAbsolutePath());
            } catch (IOException e) {
                getLogger().warning("Failed to extract resource: " + e.getMessage());
            }
        }
    
    
     */
    @Override
    public void  onDisable(){
        getLogger().info(bundle.getString("OnDisable"));
        File txtFile = new File(getDataFolder(), "buildInBadWords.txt");
        if (txtFile.exists()) {
            if (txtFile.delete()) {
                getLogger().info(bundle.getString("ResourcesUnload"));
            } else {
                getLogger().warning(bundle.getString("ResourcesUnload-err"));
            }
        } else {
            getLogger().warning(bundle.getString("ResourcesUnload-err-Already"));
        }
        System.out.println("""
                
                SIMPLE CHAT 1.14.5
                Now unloaded plugin!
                Bye.
                
                """);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, String label, String[] args) {
        switch (label){
            case "schat-mute" -> {
                if (sender.hasPermission("schat.mute") || sender.isOp()) {
                    if (args.length >= 1) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target != null ) {
                            // 获取禁言原因和时长
                            if(!playerMutedStatus.getOrDefault(target.getName(),false)){
                                String reason = args.length >= 2 ? args[1] : "N/A";
                                int duration = args.length >= 3 ? Integer.parseInt(args[2]) : -1; // 默认为永久禁言
                                playerMutedStatus.put(target.getName(),false);
                                sender.sendMessage("Muted player:'" + target.getName() + "'because " + reason + "for " + (duration == -1 ? "forever" : duration + "min"));
                            }

                        } else {
                            sender.sendMessage("Player " + args[0] + " not ONLINE.");
                        }
                    } else {
                        sender.sendMessage("Usage: /schat-mute <playerName> [reason] [duration]");
                    }
                    return true;
                } else {
                    sender.sendMessage(bundle.getString("Have-no-permission"));
                    return true;
                }
            }

            case "schat-undo" -> {
                if (sender.isOp()) {
                    if (args.length >= 2) {
                        String action = args[0];
                        String targetPlayer = args[1];

                        if (action.equalsIgnoreCase("mute")) {
                            // 撤销禁言逻辑
                            if (playerViolationCount.containsKey(targetPlayer) && playerMutedStatus.getOrDefault(targetPlayer ,false)) {
                                playerViolationCount.remove(targetPlayer);
                                playerMutedStatus.put(targetPlayer,true);
                                sender.sendMessage("Successful to unMute player " + targetPlayer );
                            } else {
                                sender.sendMessage("Player " + targetPlayer + " is not in MUTED.");
                            }
                        } else if (action.equalsIgnoreCase("unmute")) {
                            // 解禁逻辑
                            // 实现解禁逻辑，例如移除玩家的禁言状态
                            // 在这里重新实现禁言逻辑，保留原来的禁言时间
                            if (playerViolationCount.containsKey(targetPlayer) && !playerMutedStatus.getOrDefault(targetPlayer,false)) {
                                int duration = playerViolationCount.get(targetPlayer);
                                playerMutedStatus.put(targetPlayer,false);
                                // 实现重新禁言逻辑，保留原来的禁言时间
                                // 例如：schatMutePlayer(targetPlayer, "重新禁言", duration);
                                sender.sendMessage("Successful to reMute player " + targetPlayer + " for " + (duration == -1 ? "forever" : duration + "min"));
                            } else {
                                sender.sendMessage("Player " + targetPlayer + " is not on muted.");
                            }
                        } else if (action.equalsIgnoreCase("restore")) {
                            // 还原违禁词列表逻辑
                            forbiddenWords = banWordsConfig.getStringList("forbiddenWords");
                            sender.sendMessage("Succeed to rollback list.");
                        } else {
                            sender.sendMessage("Error type,please type 'mute' or 'unmute' or 'restore'.");
                        }
                    } else {
                        sender.sendMessage("Usage: /schat-undo <action> <player>");
                    }
                    return true;
                } else {
                    sender.sendMessage(bundle.getString("Have-no-permission") + "but OP");
                    return true;
                }
            }


            case "schat-unmute" -> {
                if (sender.hasPermission("schat.unmute") || sender.isOp()) {
                    if (args.length == 1) {
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target != null && playerMutedStatus.getOrDefault(target.getName(),false) ) {
                            // 执行解除禁言逻辑
                            playerViolationCount.put(target.getName(), 0); // 将该玩家的违规次数设为0，解除禁言
                            playerMutedStatus.remove(target.getName());
                            sender.sendMessage("unMuted player " + target.getName() );
                        } else {
                            sender.sendMessage("Cannot found player " + args[0] );
                        }
                    } else {
                        sender.sendMessage("Usage: /schat-unmute <playerName>");
                    }
                    return true;
                } else {
                    sender.sendMessage(bundle.getString("Have-no-permission"));
                    return true;
                }
            }


            case "schat-list" -> { //列出违禁词，读取指令
                if (sender.isOp()) {
                    if (args.length == 0) {
                        if (banWordsFile.exists()) {
                            YamlConfiguration banWordsConfig = YamlConfiguration.loadConfiguration(banWordsFile);
                            List<String> forbiddenWords = banWordsConfig.getStringList("forbiddenWords");
                            //从yaml中读取
                            int pageSize = 20;//一页的显示数量
                            int totalPages = (int) Math.ceil((double) forbiddenWords.size() / pageSize);
                            //起始页
                            int startIndex = 0;
                            int endIndex = Math.min(startIndex + pageSize, forbiddenWords.size());

                            List<String> wordsToShow = forbiddenWords.subList(startIndex, endIndex);
                            String badWords = String.join(", ", wordsToShow);

                            sender.sendMessage("Page 1 / total: " + totalPages );
                            sender.sendMessage("Bad word: " + badWords);
                        } else {
                            sender.sendMessage("Unable to read bad words.");
                            logger.warning("Unable to read bad words.");
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
                                    sender.sendMessage("Please type page in " + totalPages );
                                    return true;
                                }
                            } catch (NumberFormatException e) {
                                sender.sendMessage("Please type page in." + totalPages);
                                return true;
                            }

                            int startIndex = (page - 1) * pageSize;
                            int endIndex = Math.min(startIndex + pageSize, forbiddenWords.size());

                            List<String> wordsToShow = forbiddenWords.subList(startIndex, endIndex);
                            String badWords = String.join(", ", wordsToShow);

                            sender.sendMessage("Page " + page + " total: " + totalPages + " pages");
                            sender.sendMessage("Bad word: " + badWords);
                        } else {
                            sender.sendMessage("Can't read bad words.");
                        }
                    } else {
                        sender.sendMessage("Usage: /schat-list page <page>");
                    }
                    return true;
                } else {
                    sender.sendMessage(bundle.getString("Have-no-permission")+ "(OP)");
                    return true;
                }
            }






            case "schat-reload" -> { //显而易见，重载
                if (sender.hasPermission("schat.reload") || sender.isOp()) {
                    saveResource("buildInBadWords.txt", true);
                    deCode();
                    loadConfig();
                    loadBanWords();
                    saveResource("Language/messages_zh_CN.properties", true);
                    saveResource("Language/messages_en_global.properties", true);
                    saveResource("Language/messages_ru_RU.properties",true);
                    LanguageConfig = getConfig().getString("banConfiguration.Language");
                    if(Objects.equals(LanguageConfig, "zh_CN")){
                        LanguageFile =  "Language/messages_zh_CN";

                    }
                    else if (Objects.equals(LanguageConfig, "en_global")){
                        LanguageFile = "Language/messages_en_global";
                    } else if (Objects.equals(LanguageConfig,"ru_RU")) {
                        LanguageFile = "Language/messages_ru_RU";
                    } else {
                        logger.warning("Wrong language in 'config.yml'!");
                        LanguageFile = "Language/messages_en_global";
                    }
                    bundle = ResourceBundle.getBundle(LanguageFile);
                    importDefaultBadWords();
                    logger.info(bundle.getString("Reload"));
                    String versionUrl = "http://micro-wave.cc:58080/job/SimpleChat/lastBuild/buildNumber";
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(() -> {

                        try {
                            URL url = new URL(versionUrl);
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                                String versionStr = reader.readLine();
                                if (versionStr != null) {
                                    int version = Integer.parseInt(versionStr);
                                    if (version > Version) {
                                        getLogger().warning(bundle.getString("URAlready") + (version - Version) + bundle.getString("BuildVersion"));
                                    } else if (version < Version) {
                                        getLogger().warning(bundle.getString("WhatThis"));
                                    } else {
                                        getLogger().info(bundle.getString("NewestVersion"));
                                    }
                                } else {
                                    getLogger().warning(bundle.getString("CannotGetVersion"));
                                }
                            } catch (IOException | NumberFormatException e) {
                                getLogger().warning(bundle.getString("unableToRead"));
                            }
                        } catch (MalformedURLException e) {
                            getLogger().warning(bundle.getString("unableToGet"));
                        }
                        //Updater.UseUpdater();
                    });
                    executor.shutdown();

                } else {
                    sender.sendMessage(bundle.getString("Have-no-permission"));
                }
                return true;
            }
            case "schat-addbadword" -> {
                if (sender.hasPermission("schat.addbadword") || sender.isOp()) {
                    if (args.length == 1) {
                        String newWord = args[0].toLowerCase();
                        if (newWord.startsWith("*")){
                            sender.sendMessage("You cannot add word'*'!");
                        }
                        else if (!forbiddenWords.contains(newWord)) {
                            forbiddenWords.add(newWord);
                            banWordsConfig.set("forbiddenWords", forbiddenWords);
                            try {
                                banWordsConfig.save(banWordsFile);
                                sender.sendMessage("Add word '" + newWord + "' to list.");
                            } catch (IOException e) {
                                e.printStackTrace();
                                sender.sendMessage("Something wrong while plugin was saving bad words!");
                                logger.warning("Something wrong while plugin was saving bad words!");
                            }
                        }
                    } else {
                        sender.sendMessage("Usage: /schat-addbadword <word>");
                    }
                    return true;
                } else {
                    sender.sendMessage(bundle.getString("Have-no-permission"));
                    return true;
                }
            }

            case "schat" -> sender.sendMessage("""
                Schat V1.14.5
                By JohnRicard4096
                Command Usage：
                '/schat' for usage menu
                '/schat-addbadword' add bad word
                '/schat-delbadword' remove bad word
                '/schat-reload' reload the plugin
                '/schat-list' list the bad word
                '/schat-mute <player> [reason] [time]' mute player
                '/schat-unmute <player>' unMute some player
                '/schat-undo <action> <player>' rollback some command
                '/schat-list page <num>' to some pages
                """);


            case"schat-delbadword" -> {
                if (sender.hasPermission("schat.delbadword") || sender.isOp()) {
                    if (args.length == 1) {
                        String wordToRemove = args[0].toLowerCase();
                        if (wordToRemove.equals("*")) {
                            // 移除所有违禁词
                            forbiddenWords.clear();
                            banWordsConfig.set("forbiddenWords", forbiddenWords);
                            try {
                                banWordsConfig.save(banWordsFile);
                                sender.sendMessage("remove all words!");
                            } catch (IOException e) {
                                e.printStackTrace();
                                sender.sendMessage("Something wrong while plugin was saving bad words!");
                                logger.warning("Something wrong while plugin was saving bad words!");
                            }
                        } else {
                            if (forbiddenWords.contains(wordToRemove)) {
                                forbiddenWords.remove(wordToRemove);
                                banWordsConfig.set("forbiddenWords", forbiddenWords);
                                try {
                                    banWordsConfig.save(banWordsFile);
                                    sender.sendMessage("Removed word '" + wordToRemove + "'.");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    sender.sendMessage("Something wrong while plugin was saving bad words!");
                                    logger.warning("Something wrong while plugin was saving bad words!");
                                }
                            } else {
                                sender.sendMessage("word '" + wordToRemove + "' was not in list.");
                            }
                        }
                    } else {
                        sender.sendMessage("Usage: /schat-delbadword <word>");
                    }
                    return true;
                } else {
                    sender.sendMessage(bundle.getString("Have-no-permission"));
                    return true;
                }
            }
            default -> {
                return false;
            }
        }




        return false;
    }
    void loadBanWords() {

        banWordsFile = new File(getDataFolder(), "badwords.yml");
        if (banWordsFile.exists()) {
            banWordsConfig = YamlConfiguration.loadConfiguration(banWordsFile);
            forbiddenWords = banWordsConfig.getStringList("forbiddenWords");
        } else {
            getLogger().warning(bundle.getString("NotFound-ResourcesPack"));
            banWordsConfig = new YamlConfiguration();
            saveResource("badwords.yml", false);
            forbiddenWords = banWordsConfig.getStringList("forbiddenWords");
        }

    }

    protected boolean updater() {
        return getConfig().getBoolean("UpdateCheck", true);
    }

    protected void importDefaultBadWords() {
        if (getConfig().getBoolean("banConfiguration.importDefaultBadWords", true)&&getConfig().getBoolean("banConfiguration.enableDefaultBadWords",true)) {
            InputStream badWordsStream = getClass().getResourceAsStream("buildInBadWords.txt");

            if (badWordsStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(badWordsStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 逐行读取直到没有更多行
                        String newWord = line.trim().toLowerCase();
                        if (!forbiddenWords.contains(newWord)) {
                            forbiddenWords.add(newWord);
                        }
                    }
                    banWordsConfig.set("forbiddenWords", forbiddenWords);
                    banWordsConfig.save(banWordsFile);
                } catch (IOException e) {
                    logger.warning(bundle.getString("CannotReadResourcesPack"));
                }
            } else {
                logger.warning(bundle.getString("CannotLoadResourcesPack"));
            }
        }
    }


    protected void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        violationThreshold = config.getInt("banConfiguration.violationThreshold", 5);
        banDuration = config.getInt("banConfiguration.banDuration", 6000);
    }





    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String playerName = event.getPlayer().getName();

        // 检查玩家是否为 OP
        if (event.getPlayer().isOp() ) {
            return; // 对于 OP 玩家不执行任何操作
        }

        if (playerMutedStatus.getOrDefault(playerName, false)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(bundle.getString("PlayerMuted"));
            return;
        }

        for (String word : forbiddenWords) {
            if (message.toLowerCase().contains(word.toLowerCase())) {
                int violations = playerViolationCount.getOrDefault(playerName, 0);
                violations++;
                playerViolationCount.put(playerName, violations);

                String censoredMessage = message.replaceAll(word, "*".repeat(word.length()));
                event.setMessage(censoredMessage);
                // 记录违规行为及时间戳
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = sdf.format(new Date());
                logger.info("Player" + playerName + " Use bad word: " + word + "at" + timestamp);
                event.getPlayer().sendMessage(bundle.getString("ChatEventCancel"));
                String[] words = message.split(" "); // 将消息分割成单词
                StringBuilder markedMessage = new StringBuilder();
                for (String w : words) {
                    if (w.toLowerCase().contains(word.toLowerCase())) {
                        // 标记包含违规词语的部分为红色
                        StringBuilder redWord = new StringBuilder();
                        for (int i = 0; i < w.length(); i++) {
                            redWord.append("§c").append(w.charAt(i));
                        }
                        markedMessage.append(redWord).append(" ");
                    } else {
                        markedMessage.append(w).append(" ");
                    }
                }
                event.getPlayer().sendMessage(bundle.getString("Bad-word-over-there"));
                event.getPlayer().sendMessage(markedMessage.toString());


                if (violations >= violationThreshold && !playerMutedStatus.getOrDefault(playerName,false)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(bundle.getString("PlayerMutedStatus"));
                    playerMutedStatus.put(playerName, false);

                    // 记录违规消息

                    getServer().getScheduler().runTaskLater(this, () -> {
                        playerViolationCount.put(playerName, 0);
                        event.getPlayer().sendMessage(bundle.getString("PlayerMutedStatusFalse"));
                        playerMutedStatus.remove(playerName);
                    }, banDuration);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() == ServerLoadEvent.LoadType.RELOAD) {
            getLogger().warning(bundle.getString("Unsafe-Action"));
        }
    }
    public void deCode(){
        File dataFolder = this.getDataFolder();
        String filePath = dataFolder.getAbsolutePath()+ "/buildInBadWords.txt";

        // 使用临时文件来存储解码后的结果
        File tempFile = new File(filePath + ".tmp");
        String tempFilePath = filePath + ".tmp";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    // 对每一行进行 Base64 解码
                    byte[] decodedBytes = Base64.getDecoder().decode(line.getBytes(StandardCharsets.UTF_8));
                    // 将解码后的字节数组转换为字符串
                    String decodedString = new String(decodedBytes,StandardCharsets.UTF_8);
                    // 写入解码后的字符串到临时文件
                    writer.write(decodedString);
                    Files.write(Paths.get(tempFilePath), decodedString.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE,StandardOpenOption.APPEND, StandardOpenOption.CREATE);

                    //writer.newLine(); // 写入换行符
                } catch (IllegalArgumentException e) {
                    System.err.println("Exception: illegal BASE64 at" + line);
                }
            }


        } catch (IOException e) {
            System.err.println("IO ERROR: " + e.getMessage());
        }
        
        // 删除原文件并重命名临时文件
        if (new File(filePath).delete()) {
            
            tempFile.renameTo(new File(filePath));
        } else {
            System.err.println("Can't decode file at " + filePath);
        }
    }

}