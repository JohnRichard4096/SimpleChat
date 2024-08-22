package com.simplechat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.*;

import static org.bukkit.Bukkit.getLogger;

public class Updater {
    static int Version = 32;
    static ResourceBundle bundle;
    private static String LangFile = SimpleChat.LanguageFile;
    private static boolean enable = new SimpleChat().updater();
    public static void UseUpdater() {
        if(!enable) return;
        bundle = ResourceBundle.getBundle(LangFile);
        String versionUrl = "http://micro-wave.cc:58080/job/SimpleChat/lastBuild/buildNumber";

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
            });
            executor.shutdown();

        }

    }


}
