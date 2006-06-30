// $Id: TestCrawlableDatasetAlias.java,v 1.4 2005/12/30 00:18:56 edavis Exp $
package thredds.crawlabledataset;

import junit.framework.TestCase;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.IOException;

/**
 * A description
 *
 * @author edavis
 * @since 9 June 2005 13:50:59 -0600
 */
public class TestCrawlableDatasetAlias extends TestCase
{
//  private static org.apache.commons.logging.Log log =
//          org.apache.commons.logging.LogFactory.getLog( TestCrawlableDatasetAlias.class );


  public TestCrawlableDatasetAlias( String name )
  {
    super( name );
  }

  protected void setUp()
  {
  }

  public void testSingleWildcardAllDirs()
  {
    String path = "test/data/thredds/cataloggen/testData/uahRadarLevelII/200412*/KBMX";
    String resultPath1 = "test/data/thredds/cataloggen/testData/uahRadarLevelII/20041214/KBMX";
    String resultPath2 = "test/data/thredds/cataloggen/testData/uahRadarLevelII/20041215/KBMX";
    String name = "200412*/KBMX";

    // Create CrawlableDataset.
    CrawlableDataset cd = null;
    try
    {
      cd = CrawlableDatasetFactory.createCrawlableDataset( path, null, null );
    }
    catch ( Exception e )
    {
      assertTrue( "Failed to create CrawlableDataset <" + path + ">: " + e.getMessage(),
                  false );
      return;
    }

    assertTrue( "CD path <" + cd.getPath() + "> not as expected <" + path + ">.",
                cd.getPath().equals( path));
    assertTrue( "CD name <" + cd.getName() + "> not as expected <" + name + ">.",
                cd.getName().equals( name ) );

    // Test the list of datasets.
    List list = null;
    try
    {
      list = cd.listDatasets();
    }
    catch ( IOException e )
    {
      assertTrue( "IOException getting children datasets <" + cd.getName() + ">: " + e.getMessage(),
                  false );
      return;
    }

    assertTrue( "Number of datasets <" + list.size() + "> not as expected <2>.",
                list.size() == 2);
    for( Iterator it = list.iterator(); it.hasNext(); )
    {
      CrawlableDataset curCd = (CrawlableDataset) it.next();
      assertTrue( "Result path <" + curCd.getPath() + "> not as expected <" + resultPath1 + " -or- " + resultPath2 + ">.",
                  curCd.getPath().equals( resultPath1) || curCd.getPath().equals( resultPath2));
    }
  }

  public void testFileWildcard()
  {
    String path = "../netcdf-java-2.2/test/data/trajectory/aircraft/uw*nc";
    String name = "uw*nc";
    String resultPath = "../netcdf-java-2.2/test/data/trajectory/aircraft/uw_kingair-2005-01-19-113957.nc";

    // Create CrawlableDataset.
    CrawlableDataset cd = null;
    try
    {
      cd = CrawlableDatasetFactory.createCrawlableDataset( path, null, null );
    }
    catch ( Exception e )
    {
      assertTrue( "Failed to create CrawlableDataset <" + path + ">: " + e.getMessage(),
                  false );
      return;
    }

    assertTrue( "CD path <" + cd.getPath() + "> not as expected <" + path + ">.",
                cd.getPath().equals( path ) );
    assertTrue( "CD name <" + cd.getName() + "> not as expected <" + name + ">.",
                cd.getName().equals( name ) );

    // Test the list of datasets.
    List list = null;
    try
    {
      list = cd.listDatasets();
    }
    catch ( IOException e )
    {
      assertTrue( "IOException getting children datasets <" + cd.getName() + ">: " + e.getMessage(),
                  false );
      return;
    }

    assertTrue( "Number of datasets <" + list.size() + "> not as expected <1>.",
                list.size() == 1 );
    CrawlableDataset curCd = (CrawlableDataset) list.get( 0);
    assertTrue( "Result path <" + curCd.getPath() + "> not as expected <" + resultPath + ">.",
                curCd.getPath().equals( resultPath ) );
  }

  public void testTwoWildcardOneInFile()
  {
    String path = "test/data/thredds/cataloggen/testData/uahRadarLevelII/200412*/KBMX/KBMX*bz2";
    List results = new ArrayList();
    results.add( "test/data/thredds/cataloggen/testData/uahRadarLevelII/20041214/KBMX/KBMX_20041214_1014.bz2");
    results.add( "test/data/thredds/cataloggen/testData/uahRadarLevelII/20041214/KBMX/KBMX_20041214_1025.bz2");
    results.add( "test/data/thredds/cataloggen/testData/uahRadarLevelII/20041215/KBMX/KBMX_20041215_1014.bz2" );
    results.add( "test/data/thredds/cataloggen/testData/uahRadarLevelII/20041215/KBMX/KBMX_20041215_1025.bz2" );
    String name = "200412*/KBMX/KBMX*bz2";

    // Create CrawlableDataset.
    CrawlableDataset cd = null;
    try
    {
      cd = CrawlableDatasetFactory.createCrawlableDataset( path, null, null );
    }
    catch ( Exception e )
    {
      assertTrue( "Failed to create CrawlableDataset <" + path + ">: " + e.getMessage(),
                  false );
      return;
    }

    assertTrue( "CD path <" + cd.getPath() + "> not as expected <" + path + ">.",
                cd.getPath().equals( path ) );
    assertTrue( "CD name <" + cd.getName() + "> not as expected <" + name + ">.",
                cd.getName().equals( name ) );

    // Test the list of datasets.
    List list = null;
    try
    {
      list = cd.listDatasets();
    }
    catch ( IOException e )
    {
      assertTrue( "IOException getting children datasets <" + cd.getName() + ">: " + e.getMessage(),
                  false );
      return;
    }

    assertTrue( "Number of datasets <" + list.size() + "> not as expected <4>.",
                list.size() == 4 );
    for ( Iterator it = list.iterator(); it.hasNext(); )
    {
      CrawlableDataset curCd = (CrawlableDataset) it.next();
      assertTrue( "Result path <" + curCd.getPath() + "> not as expected <" + results + ">.",
                  results.contains( curCd.getPath()) );
    }
  }

//  public void testUncPaths()
//  {
//    //String dir = "\\\\Zero\\winxx";
//    //String dir = "//Zero/winxx";
//    String dir = "test///data///thredds";
//    File f = new File( dir );
//    URL furl = null;
//    boolean urlok = true;
//    try
//    {
//      furl = f.toURL();
//    }
//    catch ( MalformedURLException e )
//    {
//      System.out.println( "  Malformed URL <"+f.toString()+">" );
//      urlok = false;
//    }
//    URI furi = f.toURI();
//    System.out.println( "Dir=" + dir );
//    System.out.println( "File=" + f + (f.isDirectory() ? " - isDir" : " - notDir"));
//    if ( urlok)
//    {
//      System.out.println( "FileURL=" + furl.toString() + (new File( furl.toString()).isDirectory() ? " - isDir" : " - notDir" ) );
//    }
//    System.out.println( "FileURI=" + furi.toString() + (new File( furi).isDirectory() ? " - isDir" : " - notDir" ));
//  }
}

/*
 * $Log: TestCrawlableDatasetAlias.java,v $
 * Revision 1.4  2005/12/30 00:18:56  edavis
 * Expand the datasetScan element in the InvCatalog XML Schema and update InvCatalogFactory10
 * to handle the expanded datasetScan. Add handling of user defined CrawlableDataset implementations
 * and other interfaces in thredds.crawlabledataset (e.g., CrawlableDatasetFilter). Add tests to
 * TestInvDatasetScan for refactored datasetScan.
 *
 * Revision 1.3  2005/12/16 23:19:39  edavis
 * Convert InvDatasetScan to use CrawlableDataset and DatasetScanCatalogBuilder.
 *
 * Revision 1.2  2005/11/18 23:51:06  edavis
 * More work on CrawlableDataset refactor of CatGen.
 *
 * Revision 1.1  2005/11/15 18:40:51  edavis
 * More work on CrawlableDataset refactor of CatGen.
 *
 * Revision 1.2  2005/08/22 17:40:24  edavis
 * Another round on CrawlableDataset: make CrawlableDatasetAlias a subclass
 * of CrawlableDataset; start generating catalogs (still not using in
 * InvDatasetScan or CatalogGen, yet).
 *
 * Revision 1.1  2005/06/24 22:08:33  edavis
 * Second stab at the CrawlableDataset interface.
 *
 *
 */