package org.vassalengine.tools;

import org.apache.commons.cli.*;
import org.vassalengine.tools.vsav.*;
import org.vassalengine.tools.vsav.export.*;
import org.vassalengine.tools.vsav.import_.JsonImporter;
import org.vassalengine.tools.vsav.model.*;
import java.util.List;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command-line tool for exporting and importing VASSAL saved games (.vsav)
 * and game logs (.vlog).
 *
 * This tool can:
 * - Export .vsav and .vlog files to JSON or human-readable text format
 * - Import JSON back to .vsav or .vlog files
 * - Display information about saved game files
 *
 * Usage:
 *   vsav-exporter export [-j|-t] [-o output] input.vsav
 *   vsav-exporter export [-j|-t] [-o output] input.vlog
 *   vsav-exporter import -o output.vsav input.json
 *   vsav-exporter import -o output.vlog input.json
 *   vsav-exporter info input.vsav
 *   vsav-exporter info input.vlog
 */
public class VsavExporter {

    private static final String VERSION = "1.0.0";

    private final Options exportOptions;
    private final Options importOptions;
    private final Options infoOptions;

    public VsavExporter() {
        exportOptions = buildExportOptions();
        importOptions = buildImportOptions();
        infoOptions = buildInfoOptions();
    }

    private Options buildExportOptions() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Show help message");
        opts.addOption("j", "json", false, "Export as JSON (default)");
        opts.addOption("t", "text", false, "Export as human-readable text");
        opts.addOption("o", "output", true, "Output file (default: stdout or input.json/input.txt)");
        opts.addOption("m", "module", true, "Module file (.vmod) for prototype expansion");
        opts.addOption(null, "raw", false, "Include raw command strings in output");
        return opts;
    }

    private Options buildImportOptions() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Show help message");
        opts.addOption("o", "output", true, "Output .vsav file (required)");
        return opts;
    }

    private Options buildInfoOptions() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Show help message");
        opts.addOption("j", "json", false, "Output as JSON");
        opts.addOption(null, "commands", false, "Show command summary");
        return opts;
    }

    public int run(String[] args) {
        if (args.length == 0) {
            printUsage();
            return 1;
        }

        String command = args[0];
        String[] remainingArgs = new String[args.length - 1];
        System.arraycopy(args, 1, remainingArgs, 0, remainingArgs.length);

        switch (command.toLowerCase()) {
            case "export":
                return runExport(remainingArgs);
            case "import":
                return runImport(remainingArgs);
            case "info":
                return runInfo(remainingArgs);
            case "-h":
            case "--help":
            case "help":
                printUsage();
                return 0;
            case "-v":
            case "--version":
            case "version":
                printVersion();
                return 0;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                return 1;
        }
    }

    private int runExport(String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(exportOptions, args);

            if (cmd.hasOption("help")) {
                printExportHelp();
                return 0;
            }

            String[] remaining = cmd.getArgs();
            if (remaining.length == 0) {
                System.err.println("Error: No input file specified");
                printExportHelp();
                return 1;
            }

            String inputPath = remaining[0];
            boolean jsonFormat = !cmd.hasOption("text");
            String outputPath = cmd.getOptionValue("output");

            // Read the save/log file based on extension
            ExportData data;
            if (isVlogFile(inputPath)) {
                VlogReader reader = new VlogReader();
                data = reader.read(inputPath);
            } else {
                VsavReader reader = new VsavReader();
                data = reader.read(inputPath);
            }

            // Determine output
            String output;
            if (jsonFormat) {
                JsonExporter exporter = new JsonExporter(true);
                output = exporter.exportToString(data);
            } else {
                TextExporter exporter = new TextExporter();
                output = exporter.exportToString(data);
            }

            // Write output
            if (outputPath != null) {
                Files.writeString(Path.of(outputPath), output, StandardCharsets.UTF_8);
                System.err.println("Exported to: " + outputPath);
            } else {
                System.out.print(output);
            }

            return 0;

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printExportHelp();
            return 1;
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private int runImport(String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(importOptions, args);

            if (cmd.hasOption("help")) {
                printImportHelp();
                return 0;
            }

            String[] remaining = cmd.getArgs();
            if (remaining.length == 0) {
                System.err.println("Error: No input file specified");
                printImportHelp();
                return 1;
            }

            String inputPath = remaining[0];
            String outputPath = cmd.getOptionValue("output");

            if (outputPath == null) {
                System.err.println("Error: Output file (-o) is required");
                printImportHelp();
                return 1;
            }

            // Read the JSON file
            JsonImporter importer = new JsonImporter();
            ExportData data = importer.importFromFile(inputPath);

            // Write the save/log file based on output extension
            if (isVlogFile(outputPath)) {
                VlogWriter writer = new VlogWriter();
                writer.write(data, outputPath);
            } else {
                VsavWriter writer = new VsavWriter();
                writer.write(data, outputPath);
            }

            System.err.println("Imported to: " + outputPath);
            return 0;

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printImportHelp();
            return 1;
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private int runInfo(String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(infoOptions, args);

            if (cmd.hasOption("help")) {
                printInfoHelp();
                return 0;
            }

            String[] remaining = cmd.getArgs();
            if (remaining.length == 0) {
                System.err.println("Error: No input file specified");
                printInfoHelp();
                return 1;
            }

            String inputPath = remaining[0];
            boolean jsonFormat = cmd.hasOption("json");
            boolean showCommands = cmd.hasOption("commands");

            // Read the save/log file based on extension
            ExportData data;
            if (isVlogFile(inputPath)) {
                VlogReader reader = new VlogReader();
                data = reader.read(inputPath);
            } else {
                VsavReader reader = new VsavReader();
                data = reader.read(inputPath);
            }

            if (jsonFormat) {
                // Output as JSON (just metadata)
                StringBuilder sb = new StringBuilder();
                sb.append("{\n");
                sb.append("  \"file\": \"").append(inputPath).append("\",\n");

                if (data.getModuleMetadata() != null) {
                    ModuleMetadata mod = data.getModuleMetadata();
                    sb.append("  \"module\": {\n");
                    sb.append("    \"name\": \"").append(mod.getName()).append("\",\n");
                    sb.append("    \"version\": \"").append(mod.getVersion()).append("\",\n");
                    sb.append("    \"vassalVersion\": \"").append(mod.getVassalVersion()).append("\"\n");
                    sb.append("  },\n");
                }

                if (data.getSaveMetadata() != null) {
                    SaveMetadata save = data.getSaveMetadata();
                    sb.append("  \"save\": {\n");
                    sb.append("    \"version\": \"").append(save.getVersion()).append("\"\n");
                    sb.append("  },\n");
                }

                sb.append("  \"fileType\": \"").append(data.getFileType()).append("\",\n");
                sb.append("  \"commandCount\": ").append(data.getCommands().size());
                if (data.isVlog() && data.getLogEntries() != null) {
                    sb.append(",\n  \"logEntryCount\": ").append(data.getLogEntries().size());
                }
                sb.append("\n}\n");

                System.out.print(sb.toString());
            } else {
                // Human-readable output
                Path path = Paths.get(inputPath);
                String headerTitle = data.isVlog() ? "=== VASSAL Log File Info ===" : "=== VASSAL Save File Info ===";
                System.out.println(headerTitle);
                System.out.println("File: " + path.getFileName());
                System.out.println("Path: " + path.toAbsolutePath());
                System.out.println("Size: " + formatBytes(Files.size(path)));
                System.out.println("Type: " + data.getFileType());
                System.out.println();

                if (data.getModuleMetadata() != null) {
                    ModuleMetadata mod = data.getModuleMetadata();
                    System.out.println("--- Module ---");
                    System.out.println("Name: " + mod.getName());
                    System.out.println("Version: " + mod.getVersion());
                    System.out.println("VASSAL Version: " + mod.getVassalVersion());
                    System.out.println();
                }

                if (data.getSaveMetadata() != null) {
                    SaveMetadata save = data.getSaveMetadata();
                    System.out.println("--- Save ---");
                    System.out.println("Version: " + save.getVersion());
                    if (save.getDescription() != null && !save.getDescription().isEmpty()) {
                        System.out.println("Description: " + save.getDescription());
                    }
                    System.out.println();
                }

                // Command summary
                String commandHeader = data.isVlog() ? "--- Initial State ---" : "--- Commands ---";
                System.out.println(commandHeader);
                int addCount = 0, removeCount = 0, changeCount = 0, moveCount = 0, otherCount = 0;
                for (CommandData c : data.getCommands()) {
                    switch (c.getType()) {
                        case ADD_PIECE: addCount++; break;
                        case REMOVE_PIECE: removeCount++; break;
                        case CHANGE_PIECE: changeCount++; break;
                        case MOVE_PIECE: moveCount++; break;
                        default: otherCount++;
                    }
                }
                System.out.println("Total: " + data.getCommands().size());
                System.out.println("  Add Piece: " + addCount);
                System.out.println("  Remove Piece: " + removeCount);
                System.out.println("  Change Piece: " + changeCount);
                System.out.println("  Move Piece: " + moveCount);
                System.out.println("  Other: " + otherCount);

                // Log entries summary (for VLOG files)
                if (data.isVlog() && data.getLogEntries() != null) {
                    System.out.println("\n--- Log Entries ---");
                    List<LogEntry> logEntries = data.getLogEntries();
                    int logCount = 0, undoCount = 0;
                    for (LogEntry entry : logEntries) {
                        if (entry.getEntryType() == LogEntry.EntryType.LOG) {
                            logCount++;
                        } else if (entry.getEntryType() == LogEntry.EntryType.UNDO) {
                            undoCount++;
                        }
                    }
                    System.out.println("Total: " + logEntries.size());
                    System.out.println("  LOG: " + logCount);
                    System.out.println("  UNDO: " + undoCount);
                }

                if (showCommands) {
                    System.out.println("\n--- Command Details ---");
                    int idx = 0;
                    for (CommandData c : data.getCommands()) {
                        System.out.printf("[%d] %s%n", idx++, c.getType());
                    }
                }
            }

            return 0;

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printInfoHelp();
            return 1;
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private void printUsage() {
        System.out.println("VASSAL Save/Log File Exporter v" + VERSION);
        System.out.println();
        System.out.println("Usage: vsav-exporter <command> [options] <file>");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  export    Export .vsav/.vlog to JSON or text format");
        System.out.println("  import    Import JSON back to .vsav/.vlog format");
        System.out.println("  info      Display information about a .vsav/.vlog file");
        System.out.println();
        System.out.println("Use 'vsav-exporter <command> --help' for more information.");
    }

    private void printVersion() {
        System.out.println("VASSAL Save File Exporter v" + VERSION);
        System.out.println("Part of the VASSAL Engine Tools");
    }

    private void printExportHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("vsav-exporter export [options] <input.vsav|input.vlog>",
            "\nExport a VASSAL saved game or log to JSON or text format.\n" +
            "File type is auto-detected by extension.\n\nOptions:",
            exportOptions,
            "\nExamples:\n" +
            "  vsav-exporter export game.vsav              # Export save to JSON (stdout)\n" +
            "  vsav-exporter export -o game.json game.vsav # Export save to file\n" +
            "  vsav-exporter export game.vlog              # Export log to JSON (stdout)\n" +
            "  vsav-exporter export -o log.json game.vlog  # Export log to file\n" +
            "  vsav-exporter export -t game.vsav           # Export as text\n",
            true);
    }

    private void printImportHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("vsav-exporter import [options] <input.json>",
            "\nImport JSON back to VASSAL saved game or log format.\n" +
            "Output type is determined by the -o file extension.\n\nOptions:",
            importOptions,
            "\nExamples:\n" +
            "  vsav-exporter import -o game.vsav game.json  # Import to save file\n" +
            "  vsav-exporter import -o game.vlog log.json   # Import to log file\n",
            true);
    }

    private void printInfoHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("vsav-exporter info [options] <input.vsav|input.vlog>",
            "\nDisplay information about a VASSAL saved game or log.\n\nOptions:",
            infoOptions,
            "\nExamples:\n" +
            "  vsav-exporter info game.vsav        # Show save file info\n" +
            "  vsav-exporter info game.vlog        # Show log file info\n" +
            "  vsav-exporter info --json game.vsav # JSON output\n",
            true);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Check if the file is a VLOG file based on extension.
     */
    private boolean isVlogFile(String path) {
        return path.toLowerCase().endsWith(".vlog");
    }

    public static void main(String[] args) {
        VsavExporter exporter = new VsavExporter();
        int exitCode = exporter.run(args);
        System.exit(exitCode);
    }
}
