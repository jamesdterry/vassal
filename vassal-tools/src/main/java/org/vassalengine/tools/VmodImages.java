package org.vassalengine.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.cli.*;

import VASSAL.tools.DataArchive;
import VASSAL.tools.io.ZipArchive;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Command-line tool for managing images in Vassal module (.vmod) files.
 *
 * Supports the following operations:
 * - list: List all images in a module
 * - add: Add images to a module
 * - delete: Delete images from a module
 * - replace: Replace existing images in a module
 *
 * Outputs in text or JSON format.
 */
public class VmodImages {

    private static final String VERSION = "1.0.0";
    private static final String IMAGE_PREFIX = DataArchive.IMAGE_DIR;

    private enum Command {
        LIST, ADD, DELETE, REPLACE, EXPORT
    }

    private final Options globalOptions;
    private final Options listOptions;
    private final Options addOptions;
    private final Options deleteOptions;
    private final Options replaceOptions;
    private final Options exportOptions;

    // Global settings
    private boolean jsonOutput = false;
    private boolean quiet = false;

    // List settings
    private boolean detailed = false;

    // Add/Replace settings
    private String prefix = IMAGE_PREFIX;
    private boolean force = false;

    // Export settings
    private Path outputDir = Paths.get(".");

    public VmodImages() {
        globalOptions = buildGlobalOptions();
        listOptions = buildListOptions();
        addOptions = buildAddOptions();
        deleteOptions = buildDeleteOptions();
        replaceOptions = buildReplaceOptions();
        exportOptions = buildExportOptions();
    }

    private Options buildGlobalOptions() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Show this help message");
        opts.addOption("v", "version", false, "Show version information");
        opts.addOption("j", "json", false, "Output in JSON format");
        opts.addOption("q", "quiet", false, "Suppress non-error output");
        return opts;
    }

    private Options buildListOptions() {
        Options opts = new Options();
        opts.addOption("d", "detailed", false, "Show file sizes and compression info");
        return opts;
    }

    private Options buildAddOptions() {
        Options opts = new Options();
        opts.addOption("p", "prefix", true, "Target directory prefix (default: images/)");
        opts.addOption("f", "force", false, "Overwrite existing images without warning");
        return opts;
    }

    private Options buildDeleteOptions() {
        Options opts = new Options();
        opts.addOption("f", "force", false, "Delete without confirmation");
        return opts;
    }

    private Options buildReplaceOptions() {
        Options opts = new Options();
        opts.addOption("p", "prefix", true, "Target directory prefix (default: images/)");
        return opts;
    }

    private Options buildExportOptions() {
        Options opts = new Options();
        opts.addOption("o", "output", true, "Output directory (default: current directory)");
        opts.addOption("f", "force", false, "Overwrite existing files without warning");
        return opts;
    }

    private void printUsage() {
        System.out.println("Vassal Module Image Manager v" + VERSION);
        System.out.println();
        System.out.println("Usage: vmod-images <command> [options] <module.vmod> [images...]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  list      List images in the module");
        System.out.println("  add       Add images to the module");
        System.out.println("  delete    Delete images from the module");
        System.out.println("  replace   Replace existing images in the module");
        System.out.println("  export    Export images from the module to disk");
        System.out.println();
        System.out.println("Global Options:");
        System.out.println("  -h, --help       Show this help message");
        System.out.println("  -v, --version    Show version information");
        System.out.println("  -j, --json       Output in JSON format");
        System.out.println("  -q, --quiet      Suppress non-error output");
        System.out.println();
        System.out.println("List Options:");
        System.out.println("  -d, --detailed   Show file sizes and compression info");
        System.out.println();
        System.out.println("Add Options:");
        System.out.println("  -p, --prefix <dir>  Target directory prefix (default: images/)");
        System.out.println("  -f, --force         Overwrite existing images without warning");
        System.out.println();
        System.out.println("Delete Options:");
        System.out.println("  -f, --force      Delete without confirmation");
        System.out.println();
        System.out.println("Replace Options:");
        System.out.println("  -p, --prefix <dir>  Target directory prefix (default: images/)");
        System.out.println();
        System.out.println("Export Options:");
        System.out.println("  -o, --output <dir>  Output directory (default: current directory)");
        System.out.println("  -f, --force         Overwrite existing files without warning");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  vmod-images list module.vmod");
        System.out.println("  vmod-images list --detailed module.vmod");
        System.out.println("  vmod-images list --json module.vmod");
        System.out.println("  vmod-images add module.vmod image1.png image2.png");
        System.out.println("  vmod-images add --prefix icons/ module.vmod icon.png");
        System.out.println("  vmod-images add --force module.vmod existing.png");
        System.out.println("  vmod-images delete module.vmod piece.png board.png");
        System.out.println("  vmod-images replace module.vmod updated_piece.png");
        System.out.println("  vmod-images export module.vmod");
        System.out.println("  vmod-images export --output ./images module.vmod");
        System.out.println("  vmod-images export module.vmod piece.png board.png");
    }

    private void printVersion() {
        System.out.println("Vassal Module Image Manager v" + VERSION);
        System.out.println("Part of the Vassal Engine Tools");
    }

    public int run(String[] args) {
        if (args.length == 0) {
            printUsage();
            return 1;
        }

        // Check for help/version first
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                printUsage();
                return 0;
            }
            if (arg.equals("-v") || arg.equals("--version")) {
                printVersion();
                return 0;
            }
        }

        // Parse command
        String commandStr = args[0].toLowerCase();
        Command command;
        try {
            command = Command.valueOf(commandStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Unknown command: " + commandStr);
            System.err.println("Valid commands: list, add, delete, replace, export");
            return 1;
        }

        // Build combined options for parsing
        Options combinedOptions = new Options();
        for (Option opt : globalOptions.getOptions()) {
            combinedOptions.addOption(opt);
        }

        Options commandOptions = getCommandOptions(command);
        for (Option opt : commandOptions.getOptions()) {
            combinedOptions.addOption(opt);
        }

        // Parse remaining args (skip command)
        String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(combinedOptions, remainingArgs);

            // Parse global options
            jsonOutput = cmd.hasOption("json");
            quiet = cmd.hasOption("quiet");

            // Parse command-specific options
            parseCommandOptions(cmd, command);

            // Get positional arguments
            String[] positionalArgs = cmd.getArgs();
            if (positionalArgs.length == 0) {
                System.err.println("Error: No module file specified");
                return 1;
            }

            String modulePath = positionalArgs[0];
            List<String> imageArgs = Arrays.asList(
                Arrays.copyOfRange(positionalArgs, 1, positionalArgs.length)
            );

            // Validate module path
            Path path = Paths.get(modulePath);
            if (!Files.exists(path)) {
                System.err.println("Error: Module file not found: " + modulePath);
                return 1;
            }

            // Execute command
            switch (command) {
                case LIST:
                    return listImages(path);
                case ADD:
                    return addImages(path, imageArgs);
                case DELETE:
                    return deleteImages(path, imageArgs);
                case REPLACE:
                    return replaceImages(path, imageArgs);
                case EXPORT:
                    return exportImages(path, imageArgs);
                default:
                    System.err.println("Error: Unknown command");
                    return 1;
            }

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printUsage();
            return 1;
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private Options getCommandOptions(Command command) {
        switch (command) {
            case LIST:
                return listOptions;
            case ADD:
                return addOptions;
            case DELETE:
                return deleteOptions;
            case REPLACE:
                return replaceOptions;
            case EXPORT:
                return exportOptions;
            default:
                return new Options();
        }
    }

    private void parseCommandOptions(CommandLine cmd, Command command) {
        switch (command) {
            case LIST:
                detailed = cmd.hasOption("detailed");
                break;
            case ADD:
                if (cmd.hasOption("prefix")) {
                    prefix = cmd.getOptionValue("prefix");
                    if (!prefix.endsWith("/")) {
                        prefix += "/";
                    }
                }
                force = cmd.hasOption("force");
                break;
            case DELETE:
                force = cmd.hasOption("force");
                break;
            case REPLACE:
                if (cmd.hasOption("prefix")) {
                    prefix = cmd.getOptionValue("prefix");
                    if (!prefix.endsWith("/")) {
                        prefix += "/";
                    }
                }
                break;
            case EXPORT:
                if (cmd.hasOption("output")) {
                    outputDir = Paths.get(cmd.getOptionValue("output"));
                }
                force = cmd.hasOption("force");
                break;
        }
    }

    // ========== LIST COMMAND ==========

    private int listImages(Path modulePath) throws IOException {
        OperationResult result = new OperationResult();
        result.operation = "list";
        result.modulePath = modulePath.toString();
        result.images = new ArrayList<>();
        result.errors = new ArrayList<>();
        result.warnings = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(modulePath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith(IMAGE_PREFIX)) {
                    ImageInfo info = new ImageInfo();
                    info.path = entry.getName();
                    info.name = entry.getName().substring(IMAGE_PREFIX.length());
                    info.size = entry.getSize();
                    info.compressedSize = entry.getCompressedSize();
                    info.status = "existing";
                    result.images.add(info);
                }
            }
        }

        // Sort by name
        result.images.sort(Comparator.comparing(i -> i.name));
        result.success = true;

        outputResult(result);
        return 0;
    }

    // ========== ADD COMMAND ==========

    private int addImages(Path modulePath, List<String> imagePaths) throws IOException {
        if (imagePaths.isEmpty()) {
            System.err.println("Error: No images specified to add");
            return 1;
        }

        OperationResult result = new OperationResult();
        result.operation = "add";
        result.modulePath = modulePath.toString();
        result.images = new ArrayList<>();
        result.errors = new ArrayList<>();
        result.warnings = new ArrayList<>();

        // Collect existing images in module
        Set<String> existingImages = getExistingImages(modulePath, prefix);

        try (ZipArchive archive = new ZipArchive(modulePath.toFile())) {
            for (String imagePathStr : imagePaths) {
                Path imagePath = Paths.get(imagePathStr);
                ImageInfo info = new ImageInfo();
                info.name = imagePath.getFileName().toString();
                info.path = prefix + info.name;

                // Check if source file exists
                if (!Files.exists(imagePath)) {
                    info.status = "error";
                    info.reason = "Source file not found";
                    result.errors.add("Source file not found: " + imagePathStr);
                    result.images.add(info);
                    continue;
                }

                // Check if image already exists in module
                if (existingImages.contains(info.name) && !force) {
                    info.status = "skipped";
                    info.reason = "Already exists (use --force to overwrite)";
                    result.warnings.add(info.name + " already exists, use --force to overwrite");
                    result.images.add(info);
                    continue;
                }

                // Add the image
                try {
                    byte[] content = Files.readAllBytes(imagePath);
                    archive.add(info.path, content);
                    info.size = content.length;
                    info.status = existingImages.contains(info.name) ? "overwritten" : "added";
                    result.images.add(info);
                } catch (IOException e) {
                    info.status = "error";
                    info.reason = e.getMessage();
                    result.errors.add("Failed to add " + info.name + ": " + e.getMessage());
                    result.images.add(info);
                }
            }
        }

        result.success = result.errors.isEmpty();
        outputResult(result);
        return result.success ? 0 : 1;
    }

    // ========== DELETE COMMAND ==========

    private int deleteImages(Path modulePath, List<String> imageNames) throws IOException {
        if (imageNames.isEmpty()) {
            System.err.println("Error: No images specified to delete");
            return 1;
        }

        OperationResult result = new OperationResult();
        result.operation = "delete";
        result.modulePath = modulePath.toString();
        result.images = new ArrayList<>();
        result.errors = new ArrayList<>();
        result.warnings = new ArrayList<>();

        // Collect existing images in module
        Set<String> existingImages = getExistingImages(modulePath, IMAGE_PREFIX);

        try (ZipArchive archive = new ZipArchive(modulePath.toFile())) {
            for (String imageName : imageNames) {
                ImageInfo info = new ImageInfo();
                // Handle both full path and just name
                if (imageName.startsWith(IMAGE_PREFIX)) {
                    info.path = imageName;
                    info.name = imageName.substring(IMAGE_PREFIX.length());
                } else {
                    info.name = imageName;
                    info.path = IMAGE_PREFIX + imageName;
                }

                // Check if image exists in module
                if (!existingImages.contains(info.name)) {
                    info.status = "error";
                    info.reason = "Image not found in module";
                    result.errors.add("Image not found: " + info.name);
                    result.images.add(info);
                    continue;
                }

                // Delete the image
                try {
                    archive.remove(info.path);
                    info.status = "deleted";
                    result.images.add(info);
                } catch (IOException e) {
                    info.status = "error";
                    info.reason = e.getMessage();
                    result.errors.add("Failed to delete " + info.name + ": " + e.getMessage());
                    result.images.add(info);
                }
            }
        }

        result.success = result.errors.isEmpty();
        outputResult(result);
        return result.success ? 0 : 1;
    }

    // ========== REPLACE COMMAND ==========

    private int replaceImages(Path modulePath, List<String> imagePaths) throws IOException {
        if (imagePaths.isEmpty()) {
            System.err.println("Error: No images specified to replace");
            return 1;
        }

        OperationResult result = new OperationResult();
        result.operation = "replace";
        result.modulePath = modulePath.toString();
        result.images = new ArrayList<>();
        result.errors = new ArrayList<>();
        result.warnings = new ArrayList<>();

        // Collect existing images in module
        Set<String> existingImages = getExistingImages(modulePath, prefix);

        try (ZipArchive archive = new ZipArchive(modulePath.toFile())) {
            for (String imagePathStr : imagePaths) {
                Path imagePath = Paths.get(imagePathStr);
                ImageInfo info = new ImageInfo();
                info.name = imagePath.getFileName().toString();
                info.path = prefix + info.name;

                // Check if source file exists
                if (!Files.exists(imagePath)) {
                    info.status = "error";
                    info.reason = "Source file not found";
                    result.errors.add("Source file not found: " + imagePathStr);
                    result.images.add(info);
                    continue;
                }

                // Check if image exists in module (must exist for replace)
                if (!existingImages.contains(info.name)) {
                    info.status = "error";
                    info.reason = "Image not found in module (use 'add' for new images)";
                    result.errors.add(info.name + " not found in module (use 'add' for new images)");
                    result.images.add(info);
                    continue;
                }

                // Replace the image (remove old, add new)
                try {
                    archive.remove(info.path);
                    byte[] content = Files.readAllBytes(imagePath);
                    archive.add(info.path, content);
                    info.size = content.length;
                    info.status = "replaced";
                    result.images.add(info);
                } catch (IOException e) {
                    info.status = "error";
                    info.reason = e.getMessage();
                    result.errors.add("Failed to replace " + info.name + ": " + e.getMessage());
                    result.images.add(info);
                }
            }
        }

        result.success = result.errors.isEmpty();
        outputResult(result);
        return result.success ? 0 : 1;
    }

    // ========== EXPORT COMMAND ==========

    private int exportImages(Path modulePath, List<String> imageNames) throws IOException {
        OperationResult result = new OperationResult();
        result.operation = "export";
        result.modulePath = modulePath.toString();
        result.outputDir = outputDir.toString();
        result.images = new ArrayList<>();
        result.errors = new ArrayList<>();
        result.warnings = new ArrayList<>();

        // Create output directory if it doesn't exist
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        try (ZipFile zipFile = new ZipFile(modulePath.toFile())) {
            // Get list of images to export
            List<ImageInfo> imagesToExport = new ArrayList<>();

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith(IMAGE_PREFIX)) {
                    String imageName = entry.getName().substring(IMAGE_PREFIX.length());

                    // If specific images requested, filter to only those
                    if (!imageNames.isEmpty() && !imageNames.contains(imageName)) {
                        continue;
                    }

                    ImageInfo info = new ImageInfo();
                    info.path = entry.getName();
                    info.name = imageName;
                    info.size = entry.getSize();
                    imagesToExport.add(info);
                }
            }

            // Check if any requested images were not found
            if (!imageNames.isEmpty()) {
                Set<String> foundNames = new HashSet<>();
                for (ImageInfo img : imagesToExport) {
                    foundNames.add(img.name);
                }
                for (String requested : imageNames) {
                    if (!foundNames.contains(requested)) {
                        ImageInfo info = new ImageInfo();
                        info.name = requested;
                        info.status = "error";
                        info.reason = "Image not found in module";
                        result.errors.add("Image not found: " + requested);
                        result.images.add(info);
                    }
                }
            }

            // Export each image
            for (ImageInfo info : imagesToExport) {
                Path outputFile = outputDir.resolve(info.name);

                // Check if file already exists
                if (Files.exists(outputFile) && !force) {
                    info.status = "skipped";
                    info.reason = "File already exists (use --force to overwrite)";
                    result.warnings.add(info.name + " already exists, use --force to overwrite");
                    result.images.add(info);
                    continue;
                }

                // Export the image
                try {
                    ZipEntry entry = zipFile.getEntry(info.path);
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = Files.newOutputStream(outputFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    info.status = "exported";
                    result.images.add(info);
                } catch (IOException e) {
                    info.status = "error";
                    info.reason = e.getMessage();
                    result.errors.add("Failed to export " + info.name + ": " + e.getMessage());
                    result.images.add(info);
                }
            }
        }

        // Sort by name
        result.images.sort(Comparator.comparing(i -> i.name));
        result.success = result.errors.isEmpty();

        outputResult(result);
        return result.success ? 0 : 1;
    }

    // ========== HELPER METHODS ==========

    private Set<String> getExistingImages(Path modulePath, String imagePrefix) throws IOException {
        Set<String> images = new HashSet<>();
        try (ZipFile zipFile = new ZipFile(modulePath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith(imagePrefix)) {
                    images.add(entry.getName().substring(imagePrefix.length()));
                }
            }
        }
        return images;
    }

    private void outputResult(OperationResult result) {
        if (quiet && result.success) {
            return;
        }

        if (jsonOutput) {
            System.out.println(formatJson(result));
        } else {
            System.out.print(formatText(result));
        }
    }

    private String formatText(OperationResult result) {
        StringBuilder sb = new StringBuilder();

        switch (result.operation) {
            case "list":
                if (detailed) {
                    sb.append(String.format("Images in %s (%d total):%n%n",
                        result.modulePath, result.images.size()));
                    sb.append(String.format("%-40s %12s %12s%n", "Name", "Size", "Compressed"));
                    sb.append("-".repeat(66)).append("\n");
                    for (ImageInfo img : result.images) {
                        sb.append(String.format("%-40s %12s %12s%n",
                            truncate(img.name, 40),
                            formatBytes(img.size),
                            formatBytes(img.compressedSize)));
                    }
                    long totalSize = result.images.stream().mapToLong(i -> i.size).sum();
                    long totalCompressed = result.images.stream().mapToLong(i -> i.compressedSize).sum();
                    sb.append("-".repeat(66)).append("\n");
                    sb.append(String.format("%-40s %12s %12s%n", "TOTAL",
                        formatBytes(totalSize), formatBytes(totalCompressed)));
                } else {
                    for (ImageInfo img : result.images) {
                        sb.append(img.name).append("\n");
                    }
                }
                break;

            case "add":
            case "delete":
            case "replace":
            case "export":
                for (ImageInfo img : result.images) {
                    String status;
                    switch (img.status) {
                        case "added":
                            status = "[+]";
                            break;
                        case "deleted":
                            status = "[-]";
                            break;
                        case "replaced":
                            status = "[~]";
                            break;
                        case "overwritten":
                            status = "[!]";
                            break;
                        case "exported":
                            status = "[>]";
                            break;
                        case "skipped":
                            status = "[S]";
                            break;
                        case "error":
                            status = "[E]";
                            break;
                        default:
                            status = "[?]";
                    }
                    sb.append(status).append(" ").append(img.name);
                    if (img.size > 0 && "exported".equals(img.status)) {
                        sb.append(" (").append(formatBytes(img.size)).append(")");
                    }
                    if (img.reason != null) {
                        sb.append(" - ").append(img.reason);
                    }
                    sb.append("\n");
                }

                if (!result.success) {
                    sb.append("\nOperation completed with errors.\n");
                } else if (!result.warnings.isEmpty()) {
                    sb.append("\nOperation completed with warnings.\n");
                } else if ("export".equals(result.operation)) {
                    long exportedCount = result.images.stream()
                        .filter(i -> "exported".equals(i.status))
                        .count();
                    sb.append("\nExported ").append(exportedCount).append(" image(s) to ")
                      .append(result.outputDir).append("\n");
                }
                break;
        }

        return sb.toString();
    }

    private String formatJson(OperationResult result) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(result);
    }

    private String formatBytes(long bytes) {
        if (bytes < 0) return "?";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private String truncate(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    // ========== DATA CLASSES ==========

    static class OperationResult {
        String operation;
        String modulePath;
        String outputDir;
        boolean success;
        List<ImageInfo> images;
        List<String> errors;
        List<String> warnings;
    }

    static class ImageInfo {
        String name;
        String path;
        long size;
        long compressedSize;
        String status;
        String reason;
    }

    // ========== MAIN ==========

    public static void main(String[] args) {
        VmodImages tool = new VmodImages();
        int exitCode = tool.run(args);
        System.exit(exitCode);
    }
}
