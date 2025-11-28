package org.vassalengine.tools.vsav.export;

import org.vassalengine.tools.vsav.model.*;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Exports ExportData to human-readable text format.
 */
public class TextExporter {

    /**
     * Export to a file.
     */
    public void export(ExportData data, String outputPath) throws IOException {
        try (Writer writer = Files.newBufferedWriter(Path.of(outputPath), StandardCharsets.UTF_8)) {
            export(data, writer);
        }
    }

    /**
     * Export to a string.
     */
    public String exportToString(ExportData data) {
        StringBuilder sb = new StringBuilder();
        exportToStringBuilder(data, sb);
        return sb.toString();
    }

    /**
     * Export to a Writer.
     */
    public void export(ExportData data, Writer writer) throws IOException {
        StringBuilder sb = new StringBuilder();
        exportToStringBuilder(data, sb);
        writer.write(sb.toString());
    }

    private void exportToStringBuilder(ExportData data, StringBuilder sb) {
        if (data.isVlog()) {
            exportVlogToStringBuilder(data, sb);
        } else {
            exportVsavToStringBuilder(data, sb);
        }
    }

    private void exportVsavToStringBuilder(ExportData data, StringBuilder sb) {
        sb.append("=== VASSAL Save File Export ===\n");
        sb.append("Format Version: ").append(data.getFormatVersion()).append("\n");
        sb.append("File Type: ").append(data.getFileType()).append("\n\n");

        // Save metadata
        if (data.getSaveMetadata() != null) {
            sb.append("--- Save Metadata ---\n");
            SaveMetadata meta = data.getSaveMetadata();
            if (meta.getVersion() != null) {
                sb.append("Version: ").append(meta.getVersion()).append("\n");
            }
            if (meta.getDescription() != null && !meta.getDescription().isEmpty()) {
                sb.append("Description: ").append(meta.getDescription()).append("\n");
            }
            sb.append("\n");
        }

        // Module metadata
        if (data.getModuleMetadata() != null) {
            sb.append("--- Module Metadata ---\n");
            ModuleMetadata meta = data.getModuleMetadata();
            if (meta.getName() != null) {
                sb.append("Name: ").append(meta.getName()).append("\n");
            }
            if (meta.getVersion() != null) {
                sb.append("Version: ").append(meta.getVersion()).append("\n");
            }
            if (meta.getVassalVersion() != null) {
                sb.append("Vassal Version: ").append(meta.getVassalVersion()).append("\n");
            }
            sb.append("\n");
        }

        // Commands summary
        sb.append("--- Commands Summary ---\n");
        int addCount = 0;
        int removeCount = 0;
        int changeCount = 0;
        int moveCount = 0;
        int otherCount = 0;

        for (CommandData cmd : data.getCommands()) {
            switch (cmd.getType()) {
                case ADD_PIECE:
                    addCount++;
                    break;
                case REMOVE_PIECE:
                    removeCount++;
                    break;
                case CHANGE_PIECE:
                    changeCount++;
                    break;
                case MOVE_PIECE:
                    moveCount++;
                    break;
                default:
                    otherCount++;
            }
        }

        sb.append("Total Commands: ").append(data.getCommands().size()).append("\n");
        sb.append("  Add Piece: ").append(addCount).append("\n");
        sb.append("  Remove Piece: ").append(removeCount).append("\n");
        sb.append("  Change Piece: ").append(changeCount).append("\n");
        sb.append("  Move Piece: ").append(moveCount).append("\n");
        sb.append("  Other: ").append(otherCount).append("\n\n");

        // Detailed command listing
        sb.append("--- Commands Detail ---\n");
        int index = 0;
        for (CommandData cmd : data.getCommands()) {
            sb.append(String.format("[%d] ", index++));
            formatCommand(cmd, sb);
            sb.append("\n");
        }
    }

    private void exportVlogToStringBuilder(ExportData data, StringBuilder sb) {
        sb.append("=== VASSAL Log File Export ===\n");
        sb.append("Format Version: ").append(data.getFormatVersion()).append("\n");
        sb.append("File Type: ").append(data.getFileType()).append("\n\n");

        // Save metadata
        if (data.getSaveMetadata() != null) {
            sb.append("--- Save Metadata ---\n");
            SaveMetadata meta = data.getSaveMetadata();
            if (meta.getVersion() != null) {
                sb.append("Version: ").append(meta.getVersion()).append("\n");
            }
            if (meta.getDescription() != null && !meta.getDescription().isEmpty()) {
                sb.append("Description: ").append(meta.getDescription()).append("\n");
            }
            sb.append("\n");
        }

        // Module metadata
        if (data.getModuleMetadata() != null) {
            sb.append("--- Module Metadata ---\n");
            ModuleMetadata meta = data.getModuleMetadata();
            if (meta.getName() != null) {
                sb.append("Name: ").append(meta.getName()).append("\n");
            }
            if (meta.getVersion() != null) {
                sb.append("Version: ").append(meta.getVersion()).append("\n");
            }
            if (meta.getVassalVersion() != null) {
                sb.append("Vassal Version: ").append(meta.getVassalVersion()).append("\n");
            }
            sb.append("\n");
        }

        // Initial state summary
        sb.append("--- Initial State ---\n");
        sb.append("Initial Commands: ").append(data.getCommands().size()).append("\n");
        int addCount = 0;
        for (CommandData cmd : data.getCommands()) {
            if (cmd.getType() == CommandData.CommandType.ADD_PIECE) {
                addCount++;
            }
        }
        sb.append("  Pieces: ").append(addCount).append("\n\n");

        // Log entries summary
        sb.append("--- Log Entries ---\n");
        java.util.List<LogEntry> logEntries = data.getLogEntries();
        if (logEntries == null || logEntries.isEmpty()) {
            sb.append("No log entries.\n");
        } else {
            int logCount = 0;
            int undoCount = 0;
            for (LogEntry entry : logEntries) {
                if (entry.getEntryType() == LogEntry.EntryType.LOG) {
                    logCount++;
                } else if (entry.getEntryType() == LogEntry.EntryType.UNDO) {
                    undoCount++;
                }
            }
            sb.append("Total Entries: ").append(logEntries.size()).append("\n");
            sb.append("  LOG: ").append(logCount).append("\n");
            sb.append("  UNDO: ").append(undoCount).append("\n\n");

            // Detailed log entry listing
            sb.append("--- Log Entry Detail ---\n");
            int index = 0;
            for (LogEntry entry : logEntries) {
                sb.append(String.format("[%d] ", index++));
                formatLogEntry(entry, sb);
                sb.append("\n");
            }
        }
    }

    private void formatLogEntry(LogEntry entry, StringBuilder sb) {
        if (entry.getEntryType() == LogEntry.EntryType.UNDO) {
            sb.append("UNDO: inProgress=").append(entry.isUndoInProgress());
        } else if (entry.getEntryType() == LogEntry.EntryType.LOG) {
            sb.append("LOG: ");
            if (entry.getCommand() != null) {
                formatCommand(entry.getCommand(), sb);
            } else {
                sb.append("(no command)");
            }
        }
    }

    private void formatCommand(CommandData cmd, StringBuilder sb) {
        switch (cmd.getType()) {
            case ADD_PIECE:
                formatAddPiece((AddPieceCommand) cmd, sb);
                break;
            case REMOVE_PIECE:
                formatRemovePiece((RemovePieceCommand) cmd, sb);
                break;
            case CHANGE_PIECE:
                formatChangePiece((ChangePieceCommand) cmd, sb);
                break;
            case MOVE_PIECE:
                formatMovePiece((MovePieceCommand) cmd, sb);
                break;
            case BEGIN_SAVE:
                sb.append("BEGIN_SAVE");
                break;
            case END_SAVE:
                sb.append("END_SAVE");
                break;
            case BEGIN_LOG:
                sb.append("BEGIN_LOG");
                break;
            case END_LOG:
                sb.append("END_LOG");
                break;
            case PLAY_AUDIO:
                sb.append("PLAY_AUDIO: ").append(cmd.getRawCommand());
                break;
            default:
                sb.append(cmd.getType().name()).append(": ").append(truncate(cmd.getRawCommand(), 80));
        }
    }

    private void formatAddPiece(AddPieceCommand cmd, StringBuilder sb) {
        PieceData piece = cmd.getPiece();
        sb.append("ADD_PIECE: id=").append(piece.getId());
        sb.append("\n    type=").append(truncate(piece.getType(), 100));
        sb.append("\n    state=").append(truncate(piece.getState(), 100));
    }

    private void formatRemovePiece(RemovePieceCommand cmd, StringBuilder sb) {
        sb.append("REMOVE_PIECE: id=").append(cmd.getPieceId());
    }

    private void formatChangePiece(ChangePieceCommand cmd, StringBuilder sb) {
        sb.append("CHANGE_PIECE: id=").append(cmd.getPieceId());
        sb.append("\n    newState=").append(truncate(cmd.getNewState(), 100));
        if (cmd.getOldState() != null) {
            sb.append("\n    oldState=").append(truncate(cmd.getOldState(), 100));
        }
    }

    private void formatMovePiece(MovePieceCommand cmd, StringBuilder sb) {
        sb.append("MOVE_PIECE: id=").append(cmd.getPieceId());
        sb.append(" from=(").append(cmd.getOldMapId()).append(",").append(cmd.getOldX())
          .append(",").append(cmd.getOldY()).append(")");
        sb.append(" to=(").append(cmd.getNewMapId()).append(",").append(cmd.getNewX())
          .append(",").append(cmd.getNewY()).append(")");
        if (cmd.getPlayerId() != null) {
            sb.append(" player=").append(cmd.getPlayerId());
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) {
            return "null";
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen) + "...";
    }
}
