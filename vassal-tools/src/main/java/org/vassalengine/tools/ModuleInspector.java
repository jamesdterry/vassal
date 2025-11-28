package org.vassalengine.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.cli.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import VASSAL.build.Builder;
import VASSAL.build.GameModule;
import VASSAL.build.module.metadata.ModuleMetaData;
import VASSAL.tools.DataArchive;
import VASSAL.tools.io.ZipArchive;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    private final Options exportOptions;
    private final Options importOptions;
    private final Options testOptions;
    private boolean summaryMode = true;
    private boolean detailedMode = false;
    private boolean jsonMode = false;
    private boolean imagesMode = false;
    private boolean componentsMode = false;
    private boolean filesMode = false;
    private String outputFile = null;

    public ModuleInspector() {
        options = buildOptions();
        exportOptions = buildExportOptions();
        importOptions = buildImportOptions();
        testOptions = buildTestOptions();
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

    private Options buildExportOptions() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Show help message");
        opts.addOption("o", "output", true, "Output directory (required)");
        return opts;
    }

    private Options buildImportOptions() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Show help message");
        opts.addOption("o", "output", true, "Output .vmod file (required)");
        return opts;
    }

    private Options buildTestOptions() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Show help message");
        opts.addOption("v", "verbose", false, "Show detailed component breakdown");
        opts.addOption("d", "dir", true, "Temp directory for test (default: auto)");
        opts.addOption("k", "keep", false, "Keep temp files after test");
        return opts;
    }

    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("vassal-inspector [options] <module.vmod>",
            "\nVassal Module Inspector v" + VERSION +
            "\nInspects and reports on Vassal module (.vmod) files.\n\nOptions:",
            options,
            "\nCommands:\n" +
            "  export -o <dir> <module.vmod>    Export module to directory\n" +
            "  import -o <file.vmod> <dir>      Import module from directory\n" +
            "  test [-v] <module.vmod>          Test roundtrip export/import\n" +
            "\nExamples:\n" +
            "  vassal-inspector module.vmod                    # Summary report\n" +
            "  vassal-inspector --detailed module.vmod         # Detailed report\n" +
            "  vassal-inspector --json module.vmod             # JSON output\n" +
            "  vassal-inspector --images module.vmod           # List images only\n" +
            "  vassal-inspector --output report.txt module.vmod # Save to file\n" +
            "  vassal-inspector export -o ./out module.vmod    # Export to directory\n" +
            "  vassal-inspector import -o new.vmod ./out       # Import from directory\n" +
            "  vassal-inspector test module.vmod               # Test roundtrip\n" +
            "  vassal-inspector test -v module.vmod            # Verbose test output\n",
            true);
    }

    private void printVersion() {
        System.out.println("Vassal Module Inspector v" + VERSION);
        System.out.println("Part of the Vassal Engine Tools");
    }

    public int run(String[] args) {
        // Check for subcommands first
        if (args.length > 0) {
            String cmd = args[0].toLowerCase();
            String[] remainingArgs = new String[args.length - 1];
            System.arraycopy(args, 1, remainingArgs, 0, remainingArgs.length);

            if ("export".equals(cmd)) {
                return runExport(remainingArgs);
            } else if ("import".equals(cmd)) {
                return runImport(remainingArgs);
            } else if ("test".equals(cmd)) {
                return runTest(remainingArgs);
            }
        }

        // Original inspection logic
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

    // ============= Export Command =============

    private void printExportHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("vassal-inspector export -o <output-dir> <module.vmod>",
            "\nExport a Vassal module to a directory with JSON metadata.\n\nOptions:",
            exportOptions,
            "\nExample:\n" +
            "  vassal-inspector export -o ./my_module module.vmod\n",
            true);
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
                System.err.println("Error: No input module specified");
                printExportHelp();
                return 1;
            }

            String outputDir = cmd.getOptionValue("output");
            if (outputDir == null) {
                System.err.println("Error: Output directory required (-o)");
                printExportHelp();
                return 1;
            }

            String inputPath = remaining[0];
            return doExport(inputPath, outputDir);

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printExportHelp();
            return 1;
        }
    }

    private int doExport(String modulePath, String outputDir) {
        Path modPath = Paths.get(modulePath);
        Path outPath = Paths.get(outputDir);

        try {
            if (!Files.exists(modPath)) {
                System.err.println("Error: Module file not found: " + modulePath);
                return 1;
            }

            // Create output directory
            Files.createDirectories(outPath);

            VmodExportData exportData = new VmodExportData();
            exportData.resources = new ArrayList<>();

            try (ZipFile zipFile = new ZipFile(modPath.toFile())) {
                // Read metadata
                exportData.metadata = readMetadata(zipFile);

                // Find and read buildFile
                String buildFileName = findBuildFileName(zipFile);
                exportData.buildFileName = buildFileName;

                if (buildFileName != null) {
                    ZipEntry buildEntry = zipFile.getEntry(buildFileName);
                    if (buildEntry != null) {
                        try (InputStream in = zipFile.getInputStream(buildEntry);
                             BufferedReader reader = new BufferedReader(
                                 new InputStreamReader(in, StandardCharsets.UTF_8))) {
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            exportData.buildFileXml = sb.toString();
                        }

                        // Parse to component tree
                        exportData.rootComponent = parseComponentTree(exportData.buildFileXml);
                    }
                }

                // Extract resources and build resource list
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) {
                        String name = entry.getName();

                        // Skip metadata files - they're in the JSON
                        if (name.equals("buildFile.xml") || name.equals("buildFile") ||
                            name.equals("moduledata")) {
                            continue;
                        }

                        // Add to resource list
                        ResourceEntry res = new ResourceEntry();
                        res.path = name;
                        res.size = entry.getSize();
                        exportData.resources.add(res);

                        // Extract file
                        Path outFile = outPath.resolve(name);
                        Files.createDirectories(outFile.getParent());
                        try (InputStream in = zipFile.getInputStream(entry);
                             OutputStream out = Files.newOutputStream(outFile)) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = in.read(buffer)) != -1) {
                                out.write(buffer, 0, len);
                            }
                        }
                    }
                }
            }

            // Write module.json
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Path jsonPath = outPath.resolve("module.json");
            Files.writeString(jsonPath, gson.toJson(exportData), StandardCharsets.UTF_8);

            System.out.println("Exported to: " + outPath);
            System.out.println("  - module.json (metadata + components)");
            System.out.println("  - " + exportData.resources.size() + " resource files");

            return 0;

        } catch (IOException e) {
            System.err.println("Error during export: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private String findBuildFileName(ZipFile zipFile) {
        if (zipFile.getEntry("buildFile.xml") != null) {
            return "buildFile.xml";
        } else if (zipFile.getEntry("buildFile") != null) {
            return "buildFile";
        }
        return null;
    }

    private ComponentData parseComponentTree(String buildFileXml) {
        try {
            Document doc = Builder.createDocument(
                new ByteArrayInputStream(buildFileXml.getBytes(StandardCharsets.UTF_8)));
            Element root = doc.getDocumentElement();
            return parseComponentElement(root);
        } catch (Exception e) {
            System.err.println("Warning: Failed to parse buildFile: " + e.getMessage());
            return null;
        }
    }

    private ComponentData parseComponentElement(Element element) {
        ComponentData comp = new ComponentData();
        comp.type = element.getTagName();
        comp.attributes = new LinkedHashMap<>();

        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            comp.attributes.put(attr.getName(), attr.getValue());
        }
        comp.name = comp.attributes.get("name");

        comp.children = new ArrayList<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                comp.children.add(parseComponentElement((Element) children.item(i)));
            }
        }
        return comp;
    }

    // ============= Import Command =============

    private void printImportHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("vassal-inspector import -o <output.vmod> <input-dir>",
            "\nImport a Vassal module from a directory with JSON metadata.\n\nOptions:",
            importOptions,
            "\nExample:\n" +
            "  vassal-inspector import -o new.vmod ./my_module\n",
            true);
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
                System.err.println("Error: No input directory specified");
                printImportHelp();
                return 1;
            }

            String outputFile = cmd.getOptionValue("output");
            if (outputFile == null) {
                System.err.println("Error: Output file required (-o)");
                printImportHelp();
                return 1;
            }

            String inputDir = remaining[0];
            return doImport(inputDir, outputFile);

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printImportHelp();
            return 1;
        }
    }

    private int doImport(String inputDir, String outputFile) {
        Path inPath = Paths.get(inputDir);
        Path outPath = Paths.get(outputFile);

        try {
            Path jsonPath = inPath.resolve("module.json");
            if (!Files.exists(jsonPath)) {
                System.err.println("Error: module.json not found in " + inputDir);
                return 1;
            }

            // Read module.json
            String jsonContent = Files.readString(jsonPath, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            VmodExportData exportData = gson.fromJson(jsonContent, VmodExportData.class);

            // Create the VMOD file
            try (ZipArchive zip = new ZipArchive(outPath.toFile(), true)) {
                // Generate buildFile content
                String buildFileContent;
                String buildFileName = exportData.buildFileName != null ?
                    exportData.buildFileName : "buildFile.xml";

                if (exportData.rootComponent != null) {
                    // Encode from component tree
                    buildFileContent = encodeComponents(exportData.rootComponent);
                } else if (exportData.buildFileXml != null) {
                    // Use raw XML fallback
                    buildFileContent = exportData.buildFileXml;
                } else {
                    System.err.println("Error: No component data or buildFile XML in module.json");
                    return 1;
                }

                // Write buildFile
                zip.add(buildFileName, buildFileContent.getBytes(StandardCharsets.UTF_8));

                // Generate and write moduledata
                String moduledataXml = generateModuledata(exportData.metadata);
                zip.add("moduledata", moduledataXml.getBytes(StandardCharsets.UTF_8));

                // Copy resources from input directory
                if (exportData.resources != null) {
                    for (ResourceEntry res : exportData.resources) {
                        Path resFile = inPath.resolve(res.path);
                        if (Files.exists(resFile)) {
                            byte[] data = Files.readAllBytes(resFile);
                            zip.add(res.path, data);
                        } else {
                            System.err.println("Warning: Resource not found: " + res.path);
                        }
                    }
                }
            }

            System.out.println("Created: " + outPath);
            return 0;

        } catch (IOException e) {
            System.err.println("Error during import: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private String encodeComponents(ComponentData root) {
        Document doc = Builder.createNewDocument();
        Element element = encodeElement(doc, root);
        doc.appendChild(element);
        return Builder.toString(doc);
    }

    private Element encodeElement(Document doc, ComponentData comp) {
        Element element = doc.createElement(comp.type);
        if (comp.attributes != null) {
            for (Map.Entry<String, String> attr : comp.attributes.entrySet()) {
                if (attr.getValue() != null) {
                    element.setAttribute(attr.getKey(), attr.getValue());
                }
            }
        }
        if (comp.children != null) {
            for (ComponentData child : comp.children) {
                element.appendChild(encodeElement(doc, child));
            }
        }
        return element;
    }

    private String generateModuledata(Map<String, String> metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<data>\n");
        if (metadata != null) {
            appendElement(sb, "  ", "version", metadata.get("version"));
            appendElement(sb, "  ", "VassalVersion", metadata.get("vassalVersion"));
            appendElement(sb, "  ", "name", metadata.get("name"));
            appendElement(sb, "  ", "description", metadata.get("description"));
        }
        sb.append("</data>\n");
        return sb.toString();
    }

    private void appendElement(StringBuilder sb, String indent, String tag, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(indent).append("<").append(tag).append(">");
            sb.append(escapeXml(value));
            sb.append("</").append(tag).append(">\n");
        }
    }

    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    // ============= Test Command =============

    private void printTestHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("vassal-inspector test [options] <module.vmod>",
            "\nTest roundtrip export/import of a Vassal module.\n\nOptions:",
            testOptions,
            "\nExample:\n" +
            "  vassal-inspector test module.vmod        # Summary output\n" +
            "  vassal-inspector test -v module.vmod     # Verbose output\n" +
            "  vassal-inspector test -k module.vmod     # Keep temp files\n",
            true);
    }

    private int runTest(String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(testOptions, args);

            if (cmd.hasOption("help")) {
                printTestHelp();
                return 0;
            }

            String[] remaining = cmd.getArgs();
            if (remaining.length == 0) {
                System.err.println("Error: No module file specified");
                printTestHelp();
                return 1;
            }

            String inputPath = remaining[0];
            boolean verbose = cmd.hasOption("verbose");
            boolean keepFiles = cmd.hasOption("keep");
            String tempDir = cmd.getOptionValue("dir");

            return doTest(inputPath, verbose, keepFiles, tempDir);

        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printTestHelp();
            return 1;
        }
    }

    private int doTest(String inputPath, boolean verbose, boolean keepFiles, String tempDirPath) {
        Path modPath = Paths.get(inputPath);

        if (!Files.exists(modPath)) {
            System.err.println("Error: Module file not found: " + inputPath);
            return 1;
        }

        Path tempDir = null;
        Path roundtripVmod = null;

        try {
            // Create temp directory
            if (tempDirPath != null) {
                tempDir = Paths.get(tempDirPath);
                Files.createDirectories(tempDir);
            } else {
                tempDir = Files.createTempDirectory("vmod-test-");
            }

            Path exportDir = tempDir.resolve("export");
            roundtripVmod = tempDir.resolve("roundtrip.vmod");

            System.out.println("=== VMOD Roundtrip Test ===\n");

            // Get original module info
            long originalSize = Files.size(modPath);
            Map<String, String> originalMeta;
            Map<String, Long> originalComponents;
            Map<String, Long> originalResources;

            try (ZipFile originalZip = new ZipFile(modPath.toFile())) {
                originalMeta = readMetadata(originalZip);
                originalComponents = countComponents(originalZip);
                originalResources = getResourceSizes(originalZip);
            }

            String moduleName = originalMeta.getOrDefault("name", "Unknown");
            String moduleVersion = originalMeta.getOrDefault("version", "?");

            System.out.println("Module: " + moduleName + " v" + moduleVersion);
            System.out.println("Original: " + formatBytes(originalSize));

            // Export
            System.out.print("Exporting... ");
            int exportResult = doExport(inputPath, exportDir.toString());
            if (exportResult != 0) {
                System.out.println("FAILED");
                return 1;
            }
            System.out.println("OK");

            // Import
            System.out.print("Importing... ");
            int importResult = doImport(exportDir.toString(), roundtripVmod.toString());
            if (importResult != 0) {
                System.out.println("FAILED");
                return 1;
            }
            System.out.println("OK");

            // Get roundtrip info
            long roundtripSize = Files.size(roundtripVmod);
            Map<String, String> roundtripMeta;
            Map<String, Long> roundtripComponents;
            Map<String, Long> roundtripResources;

            try (ZipFile roundtripZip = new ZipFile(roundtripVmod.toFile())) {
                roundtripMeta = readMetadata(roundtripZip);
                roundtripComponents = countComponents(roundtripZip);
                roundtripResources = getResourceSizes(roundtripZip);
            }

            System.out.println("Roundtrip: " + formatBytes(roundtripSize));
            System.out.println();

            // Compare metadata
            boolean metaPass = compareMetadata(originalMeta, roundtripMeta, verbose);

            // Compare components
            boolean compPass = compareComponentCounts(originalComponents, roundtripComponents, verbose);

            // Compare resources
            boolean resPass = compareResourceSizes(originalResources, roundtripResources, verbose);

            // Final result
            System.out.println();
            if (metaPass && compPass && resPass) {
                System.out.println("Result: PASS");
                return 0;
            } else {
                System.out.println("Result: FAIL");
                return 1;
            }

        } catch (IOException e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } finally {
            // Clean up temp files unless --keep
            if (!keepFiles && tempDir != null) {
                try {
                    deleteDirectory(tempDir);
                } catch (IOException e) {
                    System.err.println("Warning: Could not clean up temp files: " + tempDir);
                }
            } else if (keepFiles && tempDir != null) {
                System.out.println("\nTemp files kept at: " + tempDir);
            }
        }
    }

    private Map<String, Long> countComponents(ZipFile zipFile) {
        Map<String, Long> counts = new LinkedHashMap<>();

        String buildFileContent = readBuildFile(zipFile);
        if (buildFileContent == null) {
            return counts;
        }

        try {
            Document doc = Builder.createDocument(
                new ByteArrayInputStream(buildFileContent.getBytes(StandardCharsets.UTF_8)));
            countElementsRecursive(doc.getDocumentElement(), counts);
        } catch (Exception e) {
            // Ignore parsing errors
        }

        return counts;
    }

    private void countElementsRecursive(Element element, Map<String, Long> counts) {
        String tagName = element.getTagName();
        counts.merge(tagName, 1L, Long::sum);

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                countElementsRecursive((Element) children.item(i), counts);
            }
        }
    }

    private Map<String, Long> getResourceSizes(ZipFile zipFile) {
        Map<String, Long> resources = new LinkedHashMap<>();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                String name = entry.getName();
                // Skip metadata files
                if (!name.equals("buildFile.xml") && !name.equals("buildFile") &&
                    !name.equals("moduledata")) {
                    resources.put(name, entry.getSize());
                }
            }
        }

        return resources;
    }

    private boolean compareMetadata(Map<String, String> original, Map<String, String> roundtrip, boolean verbose) {
        List<String> diffs = new ArrayList<>();

        for (String key : Arrays.asList("name", "version", "description", "vassalVersion")) {
            String origVal = original.getOrDefault(key, "");
            String rtVal = roundtrip.getOrDefault(key, "");
            if (!origVal.equals(rtVal)) {
                diffs.add(String.format("  %s: '%s' -> '%s'", key, origVal, rtVal));
            }
        }

        if (diffs.isEmpty()) {
            System.out.println("[PASS] Metadata validation");
            if (verbose) {
                System.out.println("  name: " + original.getOrDefault("name", ""));
                System.out.println("  version: " + original.getOrDefault("version", ""));
            }
            return true;
        } else {
            System.out.println("[FAIL] Metadata validation");
            for (String diff : diffs) {
                System.out.println(diff);
            }
            return false;
        }
    }

    private boolean compareComponentCounts(Map<String, Long> original, Map<String, Long> roundtrip, boolean verbose) {
        List<String> diffs = new ArrayList<>();
        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(original.keySet());
        allKeys.addAll(roundtrip.keySet());

        long totalOriginal = original.values().stream().mapToLong(Long::longValue).sum();
        long totalRoundtrip = roundtrip.values().stream().mapToLong(Long::longValue).sum();

        for (String key : allKeys) {
            long origCount = original.getOrDefault(key, 0L);
            long rtCount = roundtrip.getOrDefault(key, 0L);
            if (origCount != rtCount) {
                diffs.add(String.format("  %s: %d -> %d", key, origCount, rtCount));
            }
        }

        if (diffs.isEmpty()) {
            System.out.println("[PASS] Component validation (" + totalOriginal + " components, " + allKeys.size() + " types)");
            if (verbose) {
                for (String key : allKeys) {
                    System.out.println("  " + key + ": " + original.get(key));
                }
            }
            return true;
        } else {
            System.out.println("[FAIL] Component validation (" + totalOriginal + " -> " + totalRoundtrip + " components)");
            for (String diff : diffs) {
                System.out.println(diff);
            }
            return false;
        }
    }

    private boolean compareResourceSizes(Map<String, Long> original, Map<String, Long> roundtrip, boolean verbose) {
        List<String> missing = new ArrayList<>();
        List<String> extra = new ArrayList<>();
        List<String> sizeDiffs = new ArrayList<>();

        for (String path : original.keySet()) {
            if (!roundtrip.containsKey(path)) {
                missing.add(path);
            } else if (!original.get(path).equals(roundtrip.get(path))) {
                sizeDiffs.add(String.format("  %s: %d -> %d bytes", path,
                    original.get(path), roundtrip.get(path)));
            }
        }

        for (String path : roundtrip.keySet()) {
            if (!original.containsKey(path)) {
                extra.add(path);
            }
        }

        boolean pass = missing.isEmpty() && extra.isEmpty() && sizeDiffs.isEmpty();

        if (pass) {
            System.out.println("[PASS] Resource validation (" + original.size() + " files)");
            if (verbose) {
                System.out.println("  All resources present and sizes match");
            }
            return true;
        } else {
            System.out.println("[FAIL] Resource validation");
            if (!missing.isEmpty()) {
                System.out.println("  Missing files: " + missing.size());
                for (String path : missing) {
                    System.out.println("    - " + path);
                }
            }
            if (!extra.isEmpty()) {
                System.out.println("  Extra files: " + extra.size());
                for (String path : extra) {
                    System.out.println("    + " + path);
                }
            }
            if (!sizeDiffs.isEmpty()) {
                System.out.println("  Size differences:");
                for (String diff : sizeDiffs) {
                    System.out.println(diff);
                }
            }
            return false;
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // Data classes for inspection
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

    // Data classes for export/import
    static class VmodExportData {
        String formatVersion = "1.0";
        String fileType = "vmod";
        Map<String, String> metadata;      // name, version, description, vassalVersion
        String buildFileName;              // "buildFile.xml" or "buildFile"
        String buildFileXml;               // Raw XML for perfect round-trip
        ComponentData rootComponent;       // Parsed tree for programmatic editing
        List<ResourceEntry> resources;
    }

    static class ComponentData {
        String type;                       // Java class name (XML tag)
        String name;                       // name attribute (convenience)
        Map<String, String> attributes;    // All attributes (LinkedHashMap for order)
        List<ComponentData> children;      // Child components
    }

    static class ResourceEntry {
        String path;                       // Archive path (e.g., "images/piece.png")
        long size;
    }

    public static void main(String[] args) {
        ModuleInspector inspector = new ModuleInspector();
        int exitCode = inspector.run(args);
        System.exit(exitCode);
    }
}
