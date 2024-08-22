package com.simplechat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.simplechat.SimpleChat.*;
import static com.simplechat.Updater.Version;
import static com.simplechat.Updater.bundle;

public class Command extends JavaPlugin implements Listener {

    SimpleChat SimpleChat = new SimpleChat();
    Map<String, Boolean> playerMutedStatus = com.simplechat.SimpleChat.playerMutedStatus;
    String LanguageConfig = com.simplechat.SimpleChat.LanguageConfig;
    String LanguageFile = com.simplechat.SimpleChat.LanguageFile;
    void loadConfig() {
        SimpleChat.loadConfig();
    }
    protected void enable(){
        getServer().getPluginManager().registerEvents(new Command(), this);
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
                    loadConfig();
                    SimpleChat.loadBanWords();
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
                    SimpleChat.importDefaultBadWords();
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
}
