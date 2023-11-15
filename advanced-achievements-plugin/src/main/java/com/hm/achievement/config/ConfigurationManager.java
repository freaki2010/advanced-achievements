package com.hm.achievement.config;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationManager {

    private final YamlConfiguration mainConfig;
    private final YamlConfiguration langConfig;
    private final YamlConfiguration guiConfig;
    private final AdvancedAchievements plugin;
    private final Logger logger;
    private final YamlUpdater yamlUpdater;


    public ConfigurationManager(
            @Named("main") YamlConfiguration mainConfig,
            @Named("gui") YamlConfiguration guiConfig,
            @Named("lang") YamlConfiguration langConfig,
            AdvancedAchievements plugin,
            Logger logger,
            YamlUpdater yamlUpdater
    ) {
        this.mainConfig = mainConfig;
        this.langConfig = langConfig;
        this.guiConfig = guiConfig;
        this.plugin = plugin;
        this.logger = logger;
        this.yamlUpdater = yamlUpdater;
    }

    public void loadConfiguration() throws PluginLoadError {
        logger.info("Backing up and loading configuration files...");
        backupAndLoadConfiguration("config.yml", "config.yml", mainConfig);
        backupAndLoadConfiguration("lang.yml", mainConfig.getString("LanguageFileName"), langConfig);
        backupAndLoadConfiguration("gui.yml", "gui.yml", guiConfig);
    }

    private void backupAndLoadConfiguration(String latestConfigName, String userConfigName, YamlConfiguration userConfig)
            throws PluginLoadError {
        File configFile = new File(plugin.getDataFolder(), userConfigName);

        try {
            backupConfiguration(configFile, userConfigName);
            loadConfiguration(configFile, userConfigName, userConfig, latestConfigName);
        } catch (IOException | InvalidConfigurationException e) {
            throw new PluginLoadError("Failed to load " + userConfigName
                    + ". Verify its syntax on yaml-online-parser.appspot.com and use the following logs.", e);
        }
    }

    private void backupConfiguration(File configFile, String userConfigName) throws IOException {
        File backupFile = new File(plugin.getDataFolder(), userConfigName + ".bak");

        // Überprüfen, ob es eine Datei gibt und ob die Backup-Datei älter ist.
        if (Files.isRegularFile(configFile.toPath()) && configFile.lastModified() > backupFile.lastModified()) {
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void loadConfiguration(File configFile, String userConfigName, YamlConfiguration userConfig, String latestConfigName)
            throws IOException, InvalidConfigurationException {
        // Erstellen Sie Verzeichnisse, wenn sie nicht vorhanden sind.
        Files.createDirectories(configFile.toPath().getParent());

        // Wenn die Konfigurationsdatei nicht existiert, kopieren Sie die Standardkonfiguration.
        if (!Files.isRegularFile(configFile.toPath())) {
            try (InputStream defaultConfig = plugin.getResource(userConfigName)) {
                Files.copy(defaultConfig, configFile.toPath());
            }
        }

        // Lade die Benutzerkonfiguration und führe das Update durch.
        userConfig.load(configFile);
        yamlUpdater.update(latestConfigName, userConfigName, userConfig);
    }
}
