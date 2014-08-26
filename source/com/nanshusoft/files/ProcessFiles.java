package com.nanshusoft.files;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

/**
 * ProcessFiles simplifies traversing a directory, first optionally 
 * filtering files, then processing the remaining files.  Both interfaces
 * return a boolean value.  
 * 
 * If the filter returns false for a given Path object,
 * that file will not be processed, and if that Path was a directory, its
 * children will also be skipped.
 * 
 * Processors are only called on individual files, never on directories.
 * If the processor returns false, no other files will be processed. The return
 * value does not signify an error state: it means "I've done what I've come to do
 * and there's no reason to continue"
 * 
 * @author cstaylor
 *
 */
public class ProcessFiles
{
    private Processor processor = new DefaultProcessor();

    private Filter    filter    = new DefaultFilter();
    
    /**
     * Do whatever you need to do with each path here.  Only files that
     * have been accepted by the filter will be processed here.  Source
     * is always a file; never a directory.
     * @author cstaylor
     *
     */
    public interface Processor
    {
        /**
         * Application code for processing each accepted file. 
         * @param source always points to a valid regular file
         * @return true if we should process the next file, false if we want to terminate all processing
         * @throws IOException if something goes wrong during processing.
         */
        public boolean process ( Path source ) throws IOException;
    }

    private static class DefaultProcessor implements Processor
    {
        public boolean process ( Path source ) throws IOException
        {
            return true;
        }
    }

    /**
     * This is an optional method for skipping files and subdirectories: while you can
     * do this kind of thing during the processing stage, it makes your code cleaner
     * by separating them.
     * @author cstaylor
     *
     */
    public interface Filter
    {
        /**
         * Determine if we should process the source file.  If source is a directory and
         * we return false, all children of source will be ignored.  This is useful if you
         * want to skip certain directories like .cvs.
         * @param source the path to consider: can be a regular file or a directory
         * @return true if accept this file for processing, false if we should ignore it
         * @throws IOException if something goes wrong when filtering source
         */
        public boolean filter ( Path source ) throws IOException;
    }

    private static class DefaultFilter implements Filter
    {
        public boolean filter ( Path source ) throws IOException
        {
            return true;
        }
    }

    public ProcessFiles ( )
    {

    }

    /**
     * Set the processor used during the process call.
     * @param processor the processor we'll use
     * @return an instance to ourself that lets us chain calls
     */
    public ProcessFiles PROCESSOR ( Processor processor )
    {
        if (processor != null) this.processor = processor;
        return this;
    }

    /**
     * Set the filter used during the process call.
     * @param filter the filter we'll use
     * @return an instance to ourself that let's us chain calls
     */
    public ProcessFiles FILTER ( Filter filter )
    {
        if (filter != null) this.filter = filter;
        return this;
    }

    private class TreeProcessor implements FileVisitor<Path>
    {
        private Path start;

        private TreeProcessor ( Path p )
        {
            this.start = p;
        }

        @Override
        public FileVisitResult preVisitDirectory ( Path dir, BasicFileAttributes attrs ) throws IOException
        {
            if (start.equals(dir)) return FileVisitResult.CONTINUE;
            if (!filter.filter(dir)) return FileVisitResult.SKIP_SUBTREE;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException
        {
            FileVisitResult ret_val = FileVisitResult.CONTINUE;
            if (filter.filter(file))
            {
                if (!processor.process(file))
                {
                    ret_val = FileVisitResult.TERMINATE;
                }
            }
            return ret_val;
        }

        @Override
        public FileVisitResult visitFileFailed ( Path file, IOException exc ) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory ( Path dir, IOException exc ) throws IOException
        {
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Start the tree traversal, filtering, and processing here
     * @param source where we should start our traversal
     * @throws IOException if something goes wrong during our traversal
     */
    public void process ( Path source ) throws IOException
    {
        TreeProcessor tc = new TreeProcessor(source);
        Files.walkFileTree(source, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, tc);
    }
}
