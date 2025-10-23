package net.rptools.extra.app;

public class BoilerPlate {
    /*
    Structure
    library.json            <-- Configuration information for the add-on library
    mts_properties.json     <-- Properties for macro script functions in library
    events.json             <-- WEvent definition for functions in the library
    library/                <-- Content of the library
    library/public          <-- Content of the library acessable via `lib:// URI`
    library/mtscript        <-- MTSCript files
    library/mtscript/public <-- MTSCript files that can be called via `[macro(): ]` outside of the library.
     */


    /*
    config file format
    {
        "name": "test-library",
            "version": "1.0.0",
            "website": "www.rptools.net",
            "gitUrl": "github.com/RPTools/test-library",
            "authors": [ "RPTools Team" ],
        "license": "GPL 3.0",
            "namespace": "net.rptools.maptool.test-library",
            "description": "My new test library for stuff",
            "shortDescription": "test library",
            "allowsUriAccess": true,
            "readMeFile": "readme.md",
            "licenseFile": "license.txt"
    }
     */

    /*
    events config
    {
       "events": [
          { "name": "onFirstInit", "mts": "onFirstInit" },
          { "name": "onInit", "mts": "onInit"}
       ],
       "legacyEvents": [
          { "name": "onInitiativeChangeRequest", "mts": "onInitiativeChangeRequest" },
          { "name": "onInitiativeChange", "mts": "onInitiativeChange" },
          { "name": "onTokenMove", "mts": "onTokenMove" },
          { "name": "onMultipleTokensMove", "mts": "onMultipleTokensMove"}
       ]
    }
     */

    /*
    mt script properties file
    {
      "properties": [
        {
          "filename": "public/auto_exec.mts",
          "autoExecute": true,
          "description": "Auto executable macro link"
        },
        {
          "filename": "public/myUDF.mts",
          "description": "My Test UDF in a drop in lib."
        }
      ]
    }
     */
/*
    {
        "slashCommands": [
        {
            "name": "testSlashCommand",
                "description": "A Test Slash Command",
                "command": "This is a test"
        },
        {
            "name": "testSlashCommand1",
                "description": "A Test Slash Command 1",
                "command": "This is a test 1"
        }

  ]
    }*/
}
