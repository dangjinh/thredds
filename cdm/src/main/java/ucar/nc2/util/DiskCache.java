/*
 * Copyright 1997-2007 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ucar.nc2.util;

import ucar.unidata.util.StringUtil;

import java.io.*;
import java.util.*;
import java.net.URLDecoder;

/**
 * This is a general purpose utility for determining a place to write files and cache them, eg for
 * uncompressing files. This class does not scour itself.
 *
 * <p> The nj22 library sometimes needs to write files, eg
 *  to uncompress them, or for grib index files, etc. The first choice is to write these files
 *  in the same directory that the original file lives in. However, that directory may not be
 *  writeable, so we need to find a place to write them to.
 *  We also want to use the file if it already exists.
 *
 * <p> A writeable cache "root directory" is set: <ol>
 * <li>  explicitly by setRootDirectory()
 * <li>  through the system property "nj22.cache" if it exists
 * <li>  by appending "/nj22/cache" to the system property "user.home" if it exists
 * <li>  by appending "/nj22/cache" to the system property "user.dir" if it exists
 * <li>  use "./nj22/cache" if none of the above
 * </ol>
 *
 * <p> Scenario 1: want to uncompress a file; check to see if already have done so,
 *  otherwise get a File that can be written to.
 * <pre>
    // see if already uncompressed
    File uncompressedFile = FileCache.getFile( uncompressedFilename, false);
    if (!uncompressedFile.exists()) {
      // nope, uncompress it
      UncompressInputStream.uncompress( uriString, uncompressedFile);
    }
    doSomething( uncompressedFile);
  </pre>
 *
 * <p> Scenario 2: want to write a derived file always in the cache.
 * <pre>
    File derivedFile = FileCache.getCacheFile( derivedFilename);
    if (!derivedFile.exists()) {
      createDerivedFile( derivedFile);
    }
    doSomething( derivedFile);
  </pre>
 *
 * <p> Scenario 3: same as scenario 1, but use the default Cache policy:
 * <pre>
    File wf = FileCache.getFileStandardPolicy( uncompressedFilename);
    if (!wf.exists()) {
      writeToFile( wf);
      wf.close();
    }
    doSomething( wf);
  </pre>
 * @author jcaron
 */
public class DiskCache {
  static private String root = null;
  static private boolean standardPolicy = false;

  static {
    root = System.getProperty("nj22.cache");

    if (root == null) {
      String home = System.getProperty("user.home");

      if (home == null)
        home = System.getProperty("user.dir");

      if (home == null)
        home = ".";

      root = home + "/.unidata/cache/";
    }

    String policy = System.getProperty("nj22.cachePolicy");
    if (policy != null)
      standardPolicy = policy.equalsIgnoreCase("true");
  }

  /**
   * Set the cache root directory. Create it if it doesnt exist.
   * @param cacheDir the cache directory
   */
  static public void setRootDirectory(String cacheDir) {
     String oldRoot = root;
     if (!cacheDir.endsWith("/"))
       cacheDir = cacheDir + "/";
     root = StringUtil.replace(cacheDir, '\\', "/"); // no nasty backslash

     makeRootDirectory();
   }

  /**
   * Make sure that the current root directory exists.
   */
  static public void makeRootDirectory() {
     File dir = new File(root);
     if ( ! dir.exists())
       if ( ! dir.mkdirs() )
         throw new IllegalStateException( "DiskCache.setRootDirectory(): could not create root directory <" + root + ">." );
   }

    static public String getRootDirectory() { return root; }

  /**
   * Set the standard policy used in getWriteableFileStandardPolicy().
   * Default is to try to create the file in the same directory as the original.
   * Setting alwaysInCache to true means to always create it in the cache directory.
   * @param alwaysInCache make this the default policy
   */
  static public void setCachePolicy( boolean alwaysInCache) {
    standardPolicy = alwaysInCache;
  }

  /**
   * Get a File if it exists. If not, get a File that can be written to.
   * Use the standard policy to decide where to place it.
   * <p>
   * Things are a bit compilicated, because in order to guarentee a file in an arbitrary
   * location can be written to, we have to try to open it as a FileOutputStream.
   * If we do, we dont want to open it twice, so we return a WriteableFile that contains an
   *  opened FileOutputStream.
   * If it already exists, or we get it from cache, we dont need to open it.
   *
   * In any case, you must call WriteableFile.close() to make sure its closed.
   *
   * @param fileLocation normal file location
   * @return  WriteableFile holding the writeable File and a possibly opened FileOutputStream (append false)
   */
  static public File getFileStandardPolicy(String fileLocation) {
    return getFile( fileLocation, standardPolicy);
  }

  /**
   * Get a File if it exists. If not, get a File that can be written to.
   * If alwaysInCache, look only in the cache, otherwise, look in the "normal" location first,
   * then in the cache.
   * <p>
   *
   * @param fileLocation normal file location
   * @param alwaysInCache true if you want to look only in the cache
   * @return  a File that either exists or is writeable.
   */
  static public File getFile(String fileLocation, boolean alwaysInCache) {
    if (alwaysInCache) {
      return getCacheFile( fileLocation);

    } else {
      File f = new File(fileLocation);
      if (f.exists())
        return f;

      // now comes the tricky part to make sure we can open and write to it
      try {
        if (f.createNewFile()) {
          f.delete();
          return f;
        }
      } catch (IOException e) {
        // cant write to it - drop through
      }

      return getCacheFile( fileLocation);
    }
  }

  /**
   * Get a file in the cache.
   * File may or may not exist. We assume its always writeable.
   * If it does exist, set its LastModifiedDate to current time.
   *
   * @param fileLocation normal file location
   * @return equivilent File in the cache.
   */
  static public File getCacheFile(String fileLocation) {
    File f = new File(makeCachePath(fileLocation));
    if (f.exists())
      f.setLastModified( System.currentTimeMillis());

    //File dir = f.getParentFile();
    //dir.mkdirs();
    return f;
  }

  /**
   * Make the cache filename
   * @param fileLocation normal file location
   * @return cache filename
   */
  static private String makeCachePath(String fileLocation) {

    String cachePath;
    try {
      cachePath = java.net.URLEncoder.encode(fileLocation, "UTF8");
    } catch (UnsupportedEncodingException e) {
      cachePath = java.net.URLEncoder.encode(fileLocation); // shouldnt happen
    }

    //String cachePath = StringUtil.replace(fileLocation, '\\', "/");
    //cachePath = StringUtil.remove(cachePath, ':');

    return root + cachePath;
  }

  static public void showCache(PrintStream pw) {
    pw.println("Cache files");
    pw.println("Size   LastModified       Filename");
    File dir = new File(root);
    for (File file : dir.listFiles()) {
      String org = null;
      try {
        org = URLDecoder.decode(file.getName(), "UTF8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      pw.println(" " + file.length() + " " + new Date(file.lastModified()) + " " + org);
    }
  }

  /**
   * Remove all files with date < cutoff.
   * @param cutoff earliest date to allow
   * @param sbuff write results here, null is ok.
   */
  static public void cleanCache(Date cutoff, StringBuilder sbuff) {
    if (sbuff != null) sbuff.append("CleanCache files before ").append(cutoff).append("\n");
    File dir = new File(root);
    for (File file : dir.listFiles()) {
      Date lastMod = new Date(file.lastModified());
      if (lastMod.before(cutoff)) {
        file.delete();
        if (sbuff != null) sbuff.append(" delete ").append(file).append(" (").append(lastMod).append(")\n");
      }
    }
  }

  /**
   * Remove files if needed to make cache have less than maxBytes bytes file sizes.
   * This will remove oldest files first.
   * @param maxBytes max number of bytes in cache.
   * @param sbuff write results here, null is ok.
   */
  static public void cleanCache(long maxBytes, StringBuilder sbuff) {
    cleanCache( maxBytes, new FileAgeComparator(), sbuff);
  }

  /**
   * Remove files if needed to make cache have less than maxBytes bytes file sizes.
   * This will remove files in sort order defined by fileComparator.
   * The first files in the sort order are kept, until the max bytes is exceeded, then they are deleted.
   * @param maxBytes max number of bytes in cache.
   * @param fileComparator sort files first with this
   * @param sbuff write results here, null is ok.
   */
  static public void cleanCache(long maxBytes, Comparator<File> fileComparator, StringBuilder sbuff) {
    if (sbuff != null) sbuff.append("DiskCache clean maxBytes= "+maxBytes+"on dir "+root+"\n");

    File dir = new File(root);
    File[] files = dir.listFiles();
    List<File> fileList = Arrays.asList( files);
    Collections.sort( fileList, fileComparator);

    long total = 0, total_delete = 0;
    for (File file : fileList) {
      if (file.length() + total > maxBytes) {
        total_delete += file.length();
        if (sbuff != null) sbuff.append(" delete "+file+" ("+file.length()+")\n");
        file.delete();
      } else {
        total += file.length();
      }
    }
    if (sbuff != null) {
      sbuff.append("Total bytes deleted= "+total_delete+"\n");
      sbuff.append("Total bytes left in cache= "+total+"\n");
    }
  }

  static private class FileLengthComparator implements Comparator<File> {
    public int compare(File f1, File f2) {
      return (int) (f1.length() - f2.length());
    }
  }

  // reverse sort - latest come first
  static private class FileAgeComparator implements Comparator<File> {
    public int compare(File f1, File f2) {
      return (int) (f2.lastModified() - f1.lastModified());
    }
  }

  /** debug
   * @param filename look for this file
   * @throws java.io.IOException if read error
   */
  static void make(String filename) throws IOException {
    File want = DiskCache.getCacheFile(filename);
    System.out.println("make=" + want.getPath() + "; exists = " + want.exists());
    if (!want.exists())
      want.createNewFile();
    System.out.println(" canRead= " + want.canRead() + " canWrite = " + want.canWrite() + " lastMod = " + new Date(want.lastModified()));

    try {
      String enc = java.net.URLEncoder.encode(filename, "UTF8");
      System.out.println(" original=" + java.net.URLDecoder.decode(enc, "UTF8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  static public void main(String[] args) throws IOException {
    DiskCache.setRootDirectory("C:/temp/chill/");
    make("C:/junk.txt");
    make("C:/some/enchanted/evening/joots+3478.txt");
    make("http://www.unidata.ucar.edu/some/enc hanted/eve'ning/nowrite.gibberish");

    showCache( System.out);
    StringBuilder sbuff = new StringBuilder();
    cleanCache(1000*1000*10, sbuff);
    System.out.println(sbuff);
  }

}