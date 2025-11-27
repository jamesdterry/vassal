package org.vassalengine.tools.vsav.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Root model for exported save game data.
 * Contains metadata and all commands from the save file.
 */
public class ExportData {
    private String formatVersion = "1.0";
    private SaveMetadata saveMetadata;
    private ModuleMetadata moduleMetadata;
    private List<CommandData> commands = new ArrayList<>();

    public String getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }

    public SaveMetadata getSaveMetadata() {
        return saveMetadata;
    }

    public void setSaveMetadata(SaveMetadata saveMetadata) {
        this.saveMetadata = saveMetadata;
    }

    public ModuleMetadata getModuleMetadata() {
        return moduleMetadata;
    }

    public void setModuleMetadata(ModuleMetadata moduleMetadata) {
        this.moduleMetadata = moduleMetadata;
    }

    public List<CommandData> getCommands() {
        return commands;
    }

    public void setCommands(List<CommandData> commands) {
        this.commands = commands;
    }

    public void addCommand(CommandData command) {
        this.commands.add(command);
    }
}
