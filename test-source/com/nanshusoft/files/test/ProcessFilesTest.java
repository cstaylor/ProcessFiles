package com.nanshusoft.files.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nanshusoft.files.ProcessFiles;
import com.nanshusoft.files.ProcessFiles.Filter;
import com.nanshusoft.files.ProcessFiles.Processor;

public class ProcessFilesTest
{
    public static void main ( String[] args )
    {
        if ( args.length == 0 ) printUsage ( );
        try
        {
            Path path = Paths.get ( args[0] );
            if ( !Files.isDirectory(path) ) printUsage ( args[0] );
            runTest ( path );
        }
        catch ( InvalidPathException oops )
        {
            printUsage ( args[0] );
        }
        catch ( IOException oops )
        {
            oops.printStackTrace();
        }
    }
    
    private static void runTest ( Path directory ) throws IOException
    {
        new ProcessFiles().FILTER ( new Filter() {
            @Override
            public boolean filter ( Path source ) throws IOException
            {
                // For testing: true if we have an even number of characters
                // in our file name, false otherwise
                return source.getFileName().toString().length() % 2 == 0;
            }
            
        }).PROCESSOR ( new Processor( ) {
            @Override
            public boolean process ( Path source ) throws IOException
            {
                System.out.printf ( "File: %s\n", source.toAbsolutePath().toString() );
                return true;
            }
        }).process ( directory );
    }
    
    private static void printUsage ( )
    {
        System.err.printf ( "Usage: ProcessFilesTest [directory]" );
        System.exit(-1);
    }
    
    private static void printUsage ( String badDirectory )
    {
        System.err.printf ( "Error: %s isn't a directory", badDirectory );
        System.exit(-2);
    }
}