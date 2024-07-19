package com.simplechat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import com.simplechat.SimpleChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.intellij.lang.annotations.Language;

import static org.bukkit.Bukkit.getLogger;

public class Updater {
    static int Version = 25;
    private FileConfiguration banWordsConfig;
    static SimpleChat simpleChat = new SimpleChat();
    static ResourceBundle bundle;
    private static String LangFile = simpleChat.LanguageFile;
    public static void UseUpdater() {
         bundle = ResourceBundle.getBundle(LangFile);
        String versionUrl = "http://cube.lichen0459.top:1145/Version.txt";
        try {
            Thread.sleep(10 * 60 * 3000); // 30分钟的毫秒数
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                Thread.sleep(10 * 60 * 3000); // 30分钟的毫秒数
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
                            } else if (version == Version){
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
            });
            executor.shutdown();

        }

    }


}
