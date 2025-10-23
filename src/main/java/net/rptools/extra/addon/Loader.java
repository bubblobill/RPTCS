package net.rptools.extra.addon;

//import net.rpTools.files.*;

public class Loader {
//    private static final Logger log = LogManager.getLogger(Loader.class);
//    private static final Gson GSON;
//    private static final List<Path> SAMPLE_PATHS;
//    private static int count = -1;
//
//    static {
//        SAMPLE_PATHS = Reflective.getSamplePaths();
//
//        GSON = new GsonBuilder()
//                .setPrettyPrinting()
//                .setStrictness(Strictness.LENIENT)
////                .enableComplexMapKeySerialization()
//                .create();
//    }
//
//    private Loader() {
//    }
//
//    public static WAddOnLibrary loadAddOn(Object pathOrFile) {
//        count++;
//        try {
//            if (pathOrFile instanceof Path path) {
//                if (Files.isDirectory(path)) {
//                    return buildAddOn(path);
//                } else {
//                    return buildAddOn(loadToCache(path.toFile()));
//                }
//            } else if (pathOrFile instanceof File file) {
//                return buildAddOn(loadToCache(file));
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return null;
//    }
//
//    public static void unzip(InputStream is, Path targetDir) throws IOException {
//        targetDir = targetDir.toAbsolutePath();
//        try (ZipInputStream zipIn = new ZipInputStream(is)) {
//            for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null; ) {
//                Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
//                if (!resolvedPath.startsWith(targetDir)) {
//                    // see: https://snyk.io/research/zip-slip-vulnerability
//                    throw new RuntimeException("Entry with an illegal path: " + ze.getName());
//                }
//                if (ze.isDirectory()) {
//                    Files.createDirectories(resolvedPath);
//                } else {
//                    Files.createDirectories(resolvedPath.getParent());
//                    Files.copy(zipIn, resolvedPath);
//                }
//            }
//        }
//    }
//
//
//    public static PathNode loadToCache(File file) throws IOException {
//        List<String> cacheKeys = new ArrayList<>();
//        if (file.isFile()) {
//            try (FileSystem fs = FileSystems.newFileSystem(file.toPath(), Constants.FILESYSTEM_ENV)) {
//                return PathWalker.getInstance(fs).walk().getTreeRootNode();
//            }
////                Files.walkFileTree(fs.getRootDirectories().iterator().next(), new SimpleFileVisitor<>() {
////                    @Override
////                    @Nonnull
////                    public FileVisitResult postVisitDirectory(@Nonnull Path path, @Nullable IOException exc) {
////                        return FileVisitResult.CONTINUE;
////                    }
////
////                    @Override
////                    @Nonnull
////                    public FileVisitResult preVisitDirectory(@Nonnull Path path, @Nonnull BasicFileAttributes attrs) {
////                        return FileVisitResult.CONTINUE;
////                    }
////
////                    @Override
////                    @Nonnull
////                    public FileVisitResult visitFile(@Nonnull Path path, @Nonnull BasicFileAttributes attrs) {
////                        if (attrs.isRegularFile()) {
////                            try (BufferedReader reader = Files.newBufferedReader(path);
////                                 BufferedReader bufferedReader = new BufferedReader(reader)) {
////                                byte[] targetArray = IOUtils.toByteArray(bufferedReader);
////                                Asset asset = Asset.createAssetDetectType(path.getFileName().toString(), targetArray, file);
////                                MD5Key key = asset.getMD5Key();
////                                cacheKeys.add(path.toString());
////                                PathNode pathNode = new PathNode(path);
////                                pathNode.setAssetKey(key);
////                                ACache.put(key, asset);
////
////                            } catch (IOException ioe) {
//////                                log.info(path + ", " + ioe.getLocalizedMessage(), ioe);
////                            }
////                        }
////                        return FileVisitResult.CONTINUE;
////                    }
////
////                    @Override
////                    @Nonnull
////                    public FileVisitResult visitFileFailed(@Nonnull Path path, @Nullable IOException exc) {
////                        log.info(exc.getLocalizedMessage(), exc);
////                        return FileVisitResult.CONTINUE;
////                    }
////                });
////            } catch (IOException ioe) {
////                log.info(ioe.getLocalizedMessage());
////            }
//        }
//        return null;
//    }
//
//    private static WAddOnLibrary buildAddOn(Object o) throws IOException {
//        WAddOnLibrary addOn = new WAddOnLibrary();
//        BiConsumer<String, JsonElement> setter = ((fileName, element) -> {
//            switch (fileName) {
//                case "events.json" -> addOn.getEvents().addEvents(element.getAsJsonObject());
//                case "library.json" -> addOn.setConfig(GSON.fromJson(element, WInfo.class));
//                case "mts_properties.json" -> addOn.getScripts().addMTScripts(element.getAsJsonObject());
//                case "stat_sheets.json" -> addOn.getStatSheets().addStatSheets(element.getAsJsonObject());
//            }
//        });
//        if(o instanceof PathNode node){
//            node.breadthFirstEnumeration().asIterator().forEachRemaining(treeNode -> {
//                PathNode pathNode = (PathNode) treeNode;
//                for (String fileName : Constants.CONFIG_FILES) {
//                   if(pathNode.getFilePath().endsWith(fileName)){
//                       Asset asset = (Asset) ACache.getIfPresent(pathNode.getAssetKey());
//                       if(asset != null){
//                           setter.accept(fileName, asset.getDataAsJson());
//                       }
//                   }
//                }
//            });
//        } else if (o instanceof Path path) {
//            try (Stream<Path> paths = Files.list(path)) {
//                List<File> files = paths.map(Path::toFile).toList();
//                List<String> fileNames = files.stream().map(File::getName).toList();
//                for (String fileName : Constants.CONFIG_FILES) {
//                    if (fileNames.contains(fileName)) {
//                        File file = files.get(fileNames.indexOf(fileName));
//                        try (FileReader fileReader = new FileReader(file)) {
//                            JsonReader reader = new JsonReader(fileReader);
//                            JsonElement element = JsonParser.parseReader(reader);
//                            setter.accept(fileName, element);
//                        } catch (IOException e) {
//                            log.info(e.getLocalizedMessage(), e);
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                log.info(e.getLocalizedMessage(), e);
//            }
//        } else {
//            for (String fileName : Constants.CONFIG_FILES) {
//                if (ACache.asMap().containsKey(count + fileName)) {
//                    JsonElement element = JsonParser.parseString(Objects.requireNonNull(ACache.getString(count + fileName)));
//                    setter.accept(fileName, element);
//                }
//            }
//        }
//        return addOn;
//    }
}
//fs.provider().newFileChannel(Path.of("/"), Set.of(StandardOpenOption.READ)).read()
