package org.vassalengine.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import VASSAL.build.Builder;
import VASSAL.build.GameModule;
import VASSAL.build.module.metadata.ModuleMetaData;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.ZipArchive;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Command-line tool for inspecting Vassal module (.vmod) files.
 *
 * This tool provides detailed information about module contents including:
 * - Basic module metadata (name, version, description)
 * - File structure and sizes
 * - Image, sound, and icon resources
 * - Component hierarchy from buildFile.xml
 *
 * Can output in multiple formats: summary, detailed, JSON, or list mode.
 */
public class ModuleInspector {

    private static final String VERSION = "1.0.0";

    private final Options options;
    private boolean summaryMode = true;
    private boolean detailedMode = false;
    private boolean jsonMode = false;
    private boolean imagesMode = false;
    private boolean componentsMode = false;
    private boolean filesMode = false;
    private String outputFile = null;

    public ModuleInspector() {
        options = buildOptions();
    }

    private Options buildOptions() {
        Options opts = new Options();

        opts.addOption("h", "help", false, "Show this help message");
        opts.addOption("v", "version", false, "Show version information");
        opts.addOption("s", "summary", false, "Show summary (default)");
        opts.addOption("d", "detailed", false, "Show detailed report");
        opts.addOption("j", "json", false, "Output in JSON format");
        opts.addOption("i", "images", false, "List only images");
        opts.addOption("c", "components", false, "List only components");
        opts.addOption("f", "files", false, "List all archive files");
        opts.addOption("o", "output", true, "Save report to file");

        return opts;
    }

    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("vassal-inspector [options] <module.vmod>",
            "\nVassal Module Inspector v" + VERSION +
            "\nInspects and reports on Vassal module (.vmod) files.\n\nOptions:",
            options,
            "\nExamples:\n" +
            "  vassal-inspector module.vmod                    # Summary report\n" +
            "  vassal-inspector --detailed module.vmod         # Detailed report\n" +
            "  vassal-inspector --json module.vmod             # JSON output\n" +
            "  vassal-inspector --images module.vmod           # List images only\n" +
            "  vassal-inspector --output report.txt module.vmod # Save to file\n",
            true);
    }

    private void printVersion() {
        System.out.println("Vassal Module Inspector v" + VERSION);
        System.out.println("Part of the Vassal Engine Tools");
    }

    public int run(String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                printUsage();
                return 0;
            }

            if (cmd.hasOption("version")) {
                printVersion();
                return 0;
            }

            // Get the module file path
            String[] remainingArgs = cmd.getArgs();
            if (remainingArgs.length == 0) {
                System.err.println("Error: No module file specified");
                printUsage();
                return 1;
            }

            String modulePath = remainingArgs[0];

            // Parse options
            if (cmd.hasOption("detailed")) {
                detailedMode = true;
                summaryMode = false;
            }
            if (cmd.hasOption("json")) {
                jsonMode = true;
                summaryMode = false;
            }
            if (cmd.hasOption("images")) {
                imagesMode = true;
                summaryMode = false;
            }
            if (cmd.hasOption("components")) {
                componentsMode = true;
                summaryMode = false;
            }
            if (cmd.hasOption("files")) {
                filesMode = true;
                summaryMode = false;
            }
            if (cmd.hasOption("output")) {
                outputFile = cmd.getOptionValue("output");
            }

            // Inspect the module
            ModuleInfo info = inspectModule(modulePath);

            // Generate and output report
            String report = generateReport(info);

            if (outputFile != null) {
                Files.writeString(Paths.get(outputFile), report, StandardCharsets.UTF_8);
                System.out.println("Report saved to: " + outputFile);
            } else {
                System.out.print(report);
            }

            return 0;

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printUsage();
            return 1;
        } catch (FileNotFoundException e) {
            System.err.println("Error: Module file not found: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error reading module: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private ModuleInfo inspectModule(String modulePath) throws IOException {
        Path path = Paths.get(modulePath);

        if (!Files.exists(path)) {
            throw new FileNotFoundException(modulePath);
        }

        if (!Files.isRegularFile(path)) {
            throw new IOException("Not a regular file: " + modulePath);
        }

        ModuleInfo info = new ModuleInfo();
        info.path = path.toAbsolutePath().toString();
        info.fileName = path.getFileName().toString();
        info.fileSize = Files.size(path);

        // Read module metadata and contents
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            // Read metadata
            info.metadata = readMetadata(zipFile);

            // List all files
            info.files = listFiles(zipFile);

            // Categorize resources
            categorizeResources(info);

            // Read buildFile.xml if present
            info.buildFileContent = readBuildFile(zipFile);
            if (info.buildFileContent != null) {
                info.components = parseComponents(info.buildFileContent);
            }
        }

        return info;
    }

    private Map<String, String> readMetadata(ZipFile zipFile) {
        Map<String, String> metadata = new LinkedHashMap<>();

        try {
            ZipEntry entry = zipFile.getEntry("moduledata");
            if (entry != null) {
                try (InputStream in = zipFile.getInputStream(entry)) {
                    // Parse XML metadata
                    Document doc = Builder.createDocument(in);
                    Element root = doc.getDocumentElement();

                    // Extract key metadata fields
                    metadata.put("name", getElementText(root, "name"));
                    metadata.put("version", getElementText(root, "version"));
                    metadata.put("description", getElementText(root, "description"));
                    metadata.put("vassalVersion", getElementText(root, "VassalVersion"));
                    metadata.put("moduleVersion", getElementText(root, "moduleVersion"));
                }
            }
        } catch (Exception e) {
            metadata.put("error", "Failed to read metadata: " + e.getMessage());
        }

        return metadata;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Node node = nodes.item(0);
            return node.getTextContent().trim();
        }
        return "";
    }

    private List<FileEntry> listFiles(ZipFile zipFile) {
        List<FileEntry> files = new ArrayList<>();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                FileEntry fe = new FileEntry();
                fe.path = entry.getName();
                fe.size = entry.getSize();
                fe.compressedSize = entry.getCompressedSize();
                files.add(fe);
            }
        }

        // Sort by path
        files.sort(Comparator.comparing(f -> f.path));

        return files;
    }

    private void categorizeResources(ModuleInfo info) {
        info.images = new ArrayList<>();
        info.sounds = new ArrayList<>();
        info.icons = new ArrayList<>();
        info.other = new ArrayList<>();

        for (FileEntry file : info.files) {
            if (file.path.startsWith("images/")) {
                info.images.add(file);
            } else if (file.path.startsWith("sounds/")) {
                info.sounds.add(file);
            } else if (file.path.startsWith("icons/")) {
                info.icons.add(file);
            } else if (!file.path.equals("buildFile.xml") &&
                       !file.path.equals("buildFile") &&
                       !file.path.equals("moduledata")) {
                info.other.add(file);
            }
        }
    }

    private String readBuildFile(ZipFile zipFile) {
        // Try buildFile.xml first, then buildFile
        for (String name : Arrays.asList("buildFile.xml", "buildFile")) {
            ZipEntry entry = zipFile.getEntry(name);
            if (entry != null) {
                try (InputStream in = zipFile.getInputStream(entry);
                     BufferedReader reader = new BufferedReader(
                         new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private List<ComponentInfo> parseComponents(String buildFileContent) {
        List<ComponentInfo> components = new ArrayList<>();

        try {
            Document doc = Builder.createDocument(
                new ByteArrayInputStream(buildFileContent.getBytes(StandardCharsets.UTF_8)));
            Element root = doc.getDocumentElement();

            parseComponentElement(root, components, 0);

        } catch (Exception e) {
            ComponentInfo error = new ComponentInfo();
            error.type = "ERROR";
            error.name = "Failed to parse buildFile: " + e.getMessage();
            error.depth = 0;
            components.add(error);
        }

        return components;
    }

    private void parseComponentElement(Element element, List<ComponentInfo> components, int depth) {
        ComponentInfo comp = new ComponentInfo();
        comp.type = element.getTagName();
        comp.name = element.getAttribute("name");
        comp.depth = depth;
        comp.attributes = new LinkedHashMap<>();

        // Get all attributes
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attr = element.getAttributes().item(i);
            if (!attr.getNodeName().equals("name")) {
                comp.attributes.put(attr.getNodeName(), attr.getNodeValue());
            }
        }

        components.add(comp);

        // Parse child elements
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                parseComponentElement((Element) child, components, depth + 1);
            }
        }
    }

    private String generateReport(ModuleInfo info) {
        if (jsonMode) {
            return generateJsonReport(info);
        } else if (imagesMode) {
            return generateImagesList(info);
        } else if (componentsMode) {
            return generateComponentsList(info);
        } else if (filesMode) {
            return generateFilesList(info);
        } else if (detailedMode) {
            return generateDetailedReport(info);
        } else {
            return generateSummaryReport(info);
        }
    }

    private String generateSummaryReport(ModuleInfo info) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Vassal Module Inspector ===\n\n");
        sb.append("File: ").append(info.fileName).append("\n");
        sb.append("Path: ").append(info.path).append("\n");
        sb.append("Size: ").append(formatBytes(info.fileSize)).append("\n\n");

        sb.append("--- Module Information ---\n");
        if (info.metadata.containsKey("name")) {
            sb.append("Name: ").append(info.metadata.get("name")).append("\n");
        }
        if (info.metadata.containsKey("version")) {
            sb.append("Version: ").append(info.metadata.get("version")).append("\n");
        }
        if (info.metadata.containsKey("description")) {
            String desc = info.metadata.get("description");
            if (!desc.isEmpty()) {
                sb.append("Description: ").append(desc).append("\n");
            }
        }
        if (info.metadata.containsKey("vassalVersion")) {
            sb.append("Vassal Version: ").append(info.metadata.get("vassalVersion")).append("\n");
        }

        sb.append("\n--- Resource Summary ---\n");
        sb.append("Total Files: ").append(info.files.size()).append("\n");
        sb.append("Images: ").append(info.images.size()).append("\n");
        sb.append("Sounds: ").append(info.sounds.size()).append("\n");
        sb.append("Icons: ").append(info.icons.size()).append("\n");

        if (info.components != null && !info.components.isEmpty()) {
            sb.append("\n--- Components ---\n");
            Map<String, Long> componentCounts = info.components.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    c -> c.type,
                    java.util.stream.Collectors.counting()));

            componentCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry ->
                    sb.append(String.format("  %-20s: %d\n", entry.getKey(), entry.getValue())));
        }

        return sb.toString();
    }

    private String generateDetailedReport(ModuleInfo info) {
        StringBuilder sb = new StringBuilder();

        // Start with summary
        sb.append(generateSummaryReport(info));

        // Add detailed file listing
        sb.append("\n--- All Files ---\n");
        for (FileEntry file : info.files) {
            sb.append(String.format("  %-50s  %10s  (compressed: %10s)\n",
                file.path,
                formatBytes(file.size),
                formatBytes(file.compressedSize)));
        }

        // Add detailed component tree
        if (info.components != null && !info.components.isEmpty()) {
            sb.append("\n--- Component Tree ---\n");
            for (ComponentInfo comp : info.components) {
                String indent = "  ".repeat(comp.depth);
                sb.append(indent).append("- ").append(comp.type);
                if (!comp.name.isEmpty()) {
                    sb.append(": ").append(comp.name);
                }
                sb.append("\n");

                if (!comp.attributes.isEmpty() && comp.depth < 2) {
                    for (Map.Entry<String, String> attr : comp.attributes.entrySet()) {
                        sb.append(indent).append("    ").append(attr.getKey())
                          .append("=").append(attr.getValue()).append("\n");
                    }
                }
            }
        }

        return sb.toString();
    }

    private String generateJsonReport(ModuleInfo info) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(info);
    }

    private String generateImagesList(ModuleInfo info) {
        StringBuilder sb = new StringBuilder();
        for (FileEntry file : info.images) {
            sb.append(file.path).append("\n");
        }
        return sb.toString();
    }

    private String generateComponentsList(ModuleInfo info) {
        StringBuilder sb = new StringBuilder();
        if (info.components != null) {
            for (ComponentInfo comp : info.components) {
                String indent = "  ".repeat(comp.depth);
                sb.append(indent).append(comp.type);
                if (!comp.name.isEmpty()) {
                    sb.append(": ").append(comp.name);
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String generateFilesList(ModuleInfo info) {
        StringBuilder sb = new StringBuilder();
        for (FileEntry file : info.files) {
            sb.append(file.path).append("\n");
        }
        return sb.toString();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // Data classes
    static class ModuleInfo {
        String path;
        String fileName;
        long fileSize;
        Map<String, String> metadata;
        List<FileEntry> files;
        List<FileEntry> images;
        List<FileEntry> sounds;
        List<FileEntry> icons;
        List<FileEntry> other;
        String buildFileContent;
        List<ComponentInfo> components;
    }

    static class FileEntry {
        String path;
        long size;
        long compressedSize;
    }

    static class ComponentInfo {
        String type;
        String name;
        int depth;
        Map<String, String> attributes;
    }

    public static void main(String[] args) {
        ModuleInspector inspector = new ModuleInspector();
        int exitCode = inspector.run(args);
        System.exit(exitCode);
    }
}
