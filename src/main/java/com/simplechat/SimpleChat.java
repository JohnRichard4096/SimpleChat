package com.simplechat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
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

    protected int Version = 32;
    protected final static Logger logger = Logger.getLogger("SimpleChat");

    Command command = new Command();


    @Override
    public void onEnable() {
        loadConfig();
        /*
        saveResourceToFile("SimpleChat/Language/messages_zh_CN.properties", "Language/messages_zh_CN.properties");
        saveResourceToFile("SimpleChat/Language/messages_en_global.properties", "Language/messages_en_global.properties");

         */

        saveResource("Language/messages_zh_CN.properties", true);
        saveResource("Language/messages_en_global.properties", true);
        saveResource("Language/messages_ru_RU.properties",true);
        // 打印操作系统信息
        command.enable();

       // Updater Updater = new Updater();	//实例化 类
        logger.info("Build Version:" + Version);
        System.out.println("""
                
                SIMPLE CHAT 1.15
                Loading plugin......
                
                """);
        System.out.println("""
                
                *
                 |---------------------------------------------------------------------|
                 |SimpleChat Build:32                                                  |
                 |View https://github.com/JohnRichard/SimpleChat/ to get newest plugin!|
                 |---------------------------------------------------------------------|
                 *
                
                """);
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
        bundle = ResourceBundle.getBundle(LanguageFile);
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

        logger.info(bundle.getString("SystemTXT"));

        banWordsFile = new File(getDataFolder(), "badwords.yml");
        getLogger().info(bundle.getString("BoardCast-Release"));
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
        loadBanWords();
        importDefaultBadWords();

    logger.info(bundle.getString("OnEnable"));
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
        String filePath = dataFolder.getAbsolutePath()+ "buildInBadWords.txt";

        // 使用临时文件来存储解码后的结果
        File tempFile = new File(filePath + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    // 对每一行进行 Base64 解码
                    byte[] decodedBytes = Base64.getDecoder().decode(line);
                    // 将解码后的字节数组转换为字符串
                    String decodedString = new String(decodedBytes);
                    // 写入解码后的字符串到临时文件
                    writer.write(decodedString);
                    writer.newLine(); // 写入换行符
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