package org.vassalengine.tools.vsav.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Root model for exported save game data.
 * Contains metadata and all commands from the save file.
 *
 * For VSAV files:
 * - fileType = "vsav"
 * - commands contains all game state commands
 *
 * For VLOG files:
 * - fileType = "vlog"
 * - commands contains the initial game state (before the log)
 * - logEntries contains the logged command sequence
 */
public class ExportData {
    private String formatVersion = "1.0";
    private String fileType = "vsav";  // "vsav" or "vlog"
    private SaveMetadata saveMetadata;
    private ModuleMetadata moduleMetadata;
    private List<CommandData> commands = new ArrayList<>();
    private List<LogEntry> logEntries;  // Only populated for VLOG files

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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public void addLogEntry(LogEntry logEntry) {
        if (this.logEntries == null) {
            this.logEntries = new ArrayList<>();
        }
        this.logEntries.add(logEntry);
    }

    public boolean isVlog() {
        return "vlog".equals(fileType);
    }
}
