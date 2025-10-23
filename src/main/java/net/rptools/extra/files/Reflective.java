package net.rptools.extra.files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Reflective {
    private static final Logger log = LogManager.getLogger(Reflective.class);
//    private static final Reflections REFLECTIONS = new Reflections("org.reverence.demo5th", Scanners.Resources);
    private static final List<Path> RESOURCE_PATHS = new ArrayList<>();
//    private static final List<Path> SAMPLE_PATHS = new ArrayList<>();
//    private static final List<String> RADIANCE_SKINS = new ArrayList<>();
    static  {
    }
    private Reflective(){}
    public static List<Path> getResourcePaths(){ return RESOURCE_PATHS; }

}
