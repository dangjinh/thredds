// $Id: TestJplQuikScatCalendar.java,v 1.2 2006/01/23 22:11:14 edavis Exp $
package thredds.dqc.server.jplQuikSCAT;

import junit.framework.*;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Locale;
import java.text.ParseException;

/**
 * A description
 *
 * User: edavis
 * Date: Feb 11, 2004
 * Time: 4:38:46 PM
 */
public class TestJplQuikScatCalendar extends TestCase
{
  static private org.slf4j.Logger logger =
          org.slf4j.LoggerFactory.getLogger( TestJplQuikScatCalendar.class );

  private int epochStartYear = 1999;
  private int epochStartMonth = 0; // January
  private int epochStartDay = 1;
  private int epochStartHour = 0;
  private int epochStartMinute = 0;
  private int epochStartSecond = 0;
  private int epochStartMilisecond = 0;

  private String epochStartDateString = "1999-01-01";
  private String epochStartDateTimeString = "1999-01-01T00:00:00.000GMT";
  private String epochStartDatePlusOneDayString = "1999-01-02T00:00:00.000GMT";

  private Calendar calendar;
  private Date epochStartDate;
  private Date epochStartDatePlusOneDay;

  private double secondsInDay = 24.0 * 60.0 * 60.0;


  private JplQuikScatCalendar me;

  public TestJplQuikScatCalendar( String name )
  {
    super( name );
  }

  protected void setUp()
  {
    logger.debug( "setUp(): starting." );

    calendar = Calendar.getInstance( TimeZone.getTimeZone("GMT"), Locale.US);
    calendar.set( this.epochStartYear, this.epochStartMonth, this.epochStartDay,
                  this.epochStartHour, this.epochStartMinute, this.epochStartSecond);
    calendar.set( Calendar.MILLISECOND, this.epochStartMilisecond);
    this.epochStartDate = calendar.getTime();
    calendar.add( Calendar.DAY_OF_YEAR, 1);
    this.epochStartDatePlusOneDay = calendar.getTime();
  }

  /** Test ... 
   */
  public void testAll()
  {
    String tmpMsg = null;

    Date testDate;
    String testDateString;
    double testSecSinceEpoch;

    Date expectDate;
    double expectSecSinceEpoch;

    me = new JplQuikScatCalendar( this.epochStartDateTimeString );
    assertTrue( me != null );

    // Test getEpochStartDate()
    testDate = this.me.getEpochStartDate();
    tmpMsg = "testAll(): testing equality of date <" + testDate.toString() + ">" +
            " from getEpochStartDate() and the" +
            " expected epoch date <" + this.epochStartDate.toString() + ">.";
    logger.debug( tmpMsg);
    assertTrue( testDate.equals( this.epochStartDate ) );

    // Test getEpochStartDateString()
    testDateString = this.me.getEpochStartDateString();
    tmpMsg = "testAll(): testing equality of date string <" + testDateString + ">" +
            " from getEpochStartDateString() and the" +
            " expected epoch date string <" + this.epochStartDateString + ">.";
    logger.debug( tmpMsg);
    assertTrue( testDateString.equals( this.epochStartDateString ) );

    // Test getEpochStartDateTimeString()
    testDateString = this.me.getEpochStartDateTimeString();
    tmpMsg = "testAll(): testing equality of date string <" + testDateString + ">" +
            " from getEpochStartDateTimeString() and the" +
            " expected epoch date string <" + this.epochStartDateTimeString + ">.";
    logger.debug( tmpMsg);
    assertTrue( testDateString.equals( this.epochStartDateTimeString ) );

    // Testing getDateFromSecondsSinceEpoch()
    testDate = this.me.getDateFromSecondsSinceEpoch( 0.0);
    tmpMsg = "testAll(): testing equality of date <" + testDate.toString() + ">" +
            " calculated from getDateFromSecondsSinceEpoch( 0.0) and the" +
            " expected date <" + this.epochStartDate.toString() + ">.";
    logger.debug( tmpMsg);
    assertTrue( tmpMsg, this.me.epochStartDate.equals( testDate));

    testDate = this.me.getDateFromSecondsSinceEpoch( this.secondsInDay);
    tmpMsg = "testAll(): testing equality of date <" + testDate.toString() + ">" +
            " calculated from getDateFromSecondsSinceEpoch( this.secondsInDay) and the" +
            " expected date <" + this.epochStartDatePlusOneDay.toString() + ">.";
    logger.debug( tmpMsg);
    assertTrue( tmpMsg, this.epochStartDatePlusOneDay.equals( testDate));

    // Testing getSecondsSinceEpochFromDate()
    testSecSinceEpoch = this.me.getSecondsSinceEpochFromDate( this.epochStartDate);
    tmpMsg = "testAll(): testing equality of seconds since epoch date <" + testSecSinceEpoch + ">" +
            " calculated from getSecondsSinceEpochFromDate( <\"" + this.epochStartDate.toString() + "\">) and the" +
            " expected seconds since epoch date <0.0>.";
    logger.debug( tmpMsg);
    assertTrue( tmpMsg, testSecSinceEpoch == 0.0 );

    testSecSinceEpoch = this.me.getSecondsSinceEpochFromDate( this.epochStartDatePlusOneDay);
    tmpMsg = "testAll(): testing equality of seconds since epoch date <" + testSecSinceEpoch + ">" +
            " calculated from getSecondsSinceEpochFromDate( <\"" + this.epochStartDatePlusOneDay.toString() + "\">) and the" +
            " expected seconds since epoch date <" + this.secondsInDay + ">.";
    logger.debug( tmpMsg);
    assertTrue( tmpMsg, testSecSinceEpoch == this.secondsInDay );

    // Test getDateFromIsoDateTimeString()
    try
    {
      testDate = this.me.getDateFromIsoDateTimeString( this.epochStartDateTimeString );
    }
    catch (ParseException e)
    {
      tmpMsg = "testAll(): parse exception thrown on call to getDateFromIsoDateTimeString(\"" + this.epochStartDateTimeString + "\")";
      logger.debug( tmpMsg);
      assertTrue( tmpMsg, false);
    }
    tmpMsg = "testAll(): testing equality of date <" + testDate.toString() + ">" +
            " calculated from getDateFromIsoDateTimeString(\"" + this.epochStartDateTimeString + "\") and the" +
            " expected date <" + this.epochStartDate.toString() + ">.";
    logger.debug( tmpMsg);
    assertTrue( tmpMsg, testDate.equals( this.epochStartDate) );

    try
    {
      testDate = this.me.getDateFromIsoDateTimeString( this.epochStartDatePlusOneDayString );
    }
    catch (ParseException e)
    {
      tmpMsg = "testAll(): parse exception thrown on call to getDateFromIsoDateTimeString(\"" + this.epochStartDateTimeString + "\")";
      logger.debug( tmpMsg);
      assertTrue( tmpMsg, false);
    }
    tmpMsg = "testAll(): testing equality of date <" + testDate.toString() + ">" +
            " calculated from getDateFromIsoDateTimeString( \"" + this.epochStartDatePlusOneDayString + "\") and the" +
            " expected date <" + this.epochStartDatePlusOneDay.toString() + ">.";
    logger.debug( tmpMsg);
    assertTrue( tmpMsg, testDate.equals( this.epochStartDatePlusOneDay) );

    // Test getIsoDateTimeStringFromDate()
    testDateString = this.me.getIsoDateTimeStringFromDate( this.epochStartDate);
    tmpMsg = "testAll(): testing equality of the date string <" +testDateString + ">" +
            " calculated from getIsoDateTimeStringFromDate(<" + this.epochStartDate.toString() + ">)" +
            " and the expected date string <" + this.epochStartDateTimeString + ">.";
    logger.debug( tmpMsg);
    assertTrue( tmpMsg, testDateString.equals( this.epochStartDateTimeString));

    testDateString = this.me.getIsoDateTimeStringFromDate( this.epochStartDatePlusOneDay);
    tmpMsg = "testAll(): testing equality of the date string <" +testDateString + ">" +
            " calculated from getIsoDateTimeStringFromDate(<" + this.epochStartDatePlusOneDay.toString() + ">)" +
            " and the expected date string <" + this.epochStartDateTimeString + ">.";
    logger.debug( tmpMsg);
    assertTrue( tmpMsg, testDateString.equals( this.epochStartDatePlusOneDayString));

    // Test getIsoDateStringFromDate()
    // @todo Test getIsoDateStringFromDate()

    // Test getDateFromIsoDateString()
    // @todo Test getDateFromIsoDateString()
  }
}

/*
 * $Log: TestJplQuikScatCalendar.java,v $
 * Revision 1.2  2006/01/23 22:11:14  edavis
 * Switch from log4j to SLF4J logging.
 *
 * Revision 1.1  2005/03/30 05:41:20  edavis
 * Simplify build process: 1) combine all build scripts into one,
 * thredds/build.xml; 2) combine contents of all resources/ directories into
 * one, thredds/resources; 3) move all test source code and test data into
 * thredds/test/src and thredds/test/data; and 3) move all schemas (.xsd and .dtd)
 * into thredds/resources/resources/thredds/schemas.
 *
 * Revision 1.2  2004/08/23 16:45:18  edavis
 * Update DqcServlet to work with DQC spec v0.3 and InvCatalog v1.0. Folded DqcServlet into the THREDDS server framework/build/distribution. Updated documentation (DqcServlet and THREDDS server).
 *
 * Revision 1.1  2004/04/05 18:37:33  edavis
 * Added to and updated existing DqcServlet test suite.
 *
 */