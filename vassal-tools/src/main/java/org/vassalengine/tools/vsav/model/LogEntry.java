package org.vassalengine.tools.vsav.model;

/**
 * Represents an entry in a VLOG file.
 *
 * VLOG files contain a sequence of logged commands, each prefixed with LOG or UNDO.
 * - LOG entries wrap regular commands
 * - UNDO entries mark undo operations
 */
public class LogEntry {

    public enum EntryType {
        LOG,    // Regular logged command (LOG\t prefix)
        UNDO    // Undo marker (UNDO\t prefix)
    }

    private EntryType entryType;
    private CommandData command;      // For LOG entries - the wrapped command
    private boolean undoInProgress;   // For UNDO entries
    private String rawEntry;          // Original string for round-trip fidelity

    public LogEntry() {
    }

    public LogEntry(EntryType entryType) {
        this.entryType = entryType;
    }

    public LogEntry(EntryType entryType, CommandData command) {
        this.entryType = entryType;
        this.command = command;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public CommandData getCommand() {
        return command;
    }

    public void setCommand(CommandData command) {
        this.command = command;
    }

    public boolean isUndoInProgress() {
        return undoInProgress;
    }

    public void setUndoInProgress(boolean undoInProgress) {
        this.undoInProgress = undoInProgress;
    }

    public String getRawEntry() {
        return rawEntry;
    }

    public void setRawEntry(String rawEntry) {
        this.rawEntry = rawEntry;
    }
}
