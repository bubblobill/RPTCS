package net.rptools.extra.addon.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WInfo {
    private String name             = "Cool Add-On";
    private String version          = "0.0.1-alpha";
    private String website          = "";
    private String gitUrl           = "github.com/" + System.getProperty("user.name") + "/test-library";
    private String license          = "Unlicense";
    private String namespace        = ("coolAddOn." + System.getProperty("user.name") + ".me").replaceAll("\\s", "_");
    private String description      = "";
    private String shortDescription = "";
    private String readMeFile       = "readme.md";
    private String licenseFile      = "license.txt";
    private List<String> authors    = new ArrayList<>(){{ add(System.getProperty("user.name"));}};
    private boolean allowsUriAccess = true;

    public WInfo(){}
    public boolean isAllowsUriAccess() {
        return allowsUriAccess;
    }

    public void setAllowsUriAccess(boolean allowsUriAccess) {
        this.allowsUriAccess = allowsUriAccess;
    }

    public String[] getAuthors() {
        return authors.toArray(String[]::new);
    }

    public void setAuthors(String[] authors) {
        this.authors.clear();
        Collections.addAll(this.authors, authors);
    }
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLicenseFile() {
        return licenseFile;
    }

    public void setLicenseFile(String licenseFile) {
        this.licenseFile = licenseFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getReadMeFile() {
        return readMeFile;
    }

    public void setReadMeFile(String readMeFile) {
        this.readMeFile = readMeFile;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return "{" +
                "\"allowsUriAccess\": \"" + allowsUriAccess +
                "\", \"name\": \"" + name + 
                "\", \"version\": \"" + version + 
                "\", \"website\": \"" + website + 
                "\", \"gitUrl\": \"" + gitUrl + 
                "\", \"license\": \"" + license + 
                "\", \"namespace\": \"" + namespace + 
                "\", \"description\": \"" + description + 
                "\", \"shortDescription\": \"" + shortDescription + 
                "\", \"readMeFile\": \"" + readMeFile + 
                "\", \"licenseFile\": \"" + licenseFile + 
                "\", \"authors\": \"" + Arrays.toString(getAuthors()) +
                '}';
    }
}
