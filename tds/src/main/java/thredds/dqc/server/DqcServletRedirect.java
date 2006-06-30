// $Id: DqcServletRedirect.java,v 1.10 2006/03/30 23:22:09 edavis Exp $

package thredds.dqc.server;

import thredds.servlet.ServletUtil;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet for redirecting /dqcServlet/dqc/* and /dqcServlet/dqcServlet/*
 * requests to /thredds/dqc/*
 *
 */

public class DqcServletRedirect extends HttpServlet
{
  private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( DqcServletRedirect.class);

  private File rootPath, dqcRootPath;
  private File contentPath, dqcContentPath;

  private String servletName = "dqcServlet";

  private String targetContextPath = "/thredds";
  private String targetServletPath = "/dqc";

  private String testTargetContextPath = "/dqcServlet";
  private String testTargetServletPath = "/dqc";

  private String testRedirectPath = "/redirect-test";
  private String testRedirectStopPath = "/redirect-stop-test";


  /** Initialize the servlet. */
  public void init()
    throws ServletException
  {
    // Initialize logging.
    ServletUtil.initLogging( this );

    // Get various paths and file names.
    this.rootPath = new File( ServletUtil.getRootPath( this ) );
    this.dqcRootPath = new File( this.rootPath,  this.servletName);

    // @todo Do we want this seperate from content/thredds?
    this.contentPath = new File( ServletUtil.getContentPath( this ) );
    this.dqcContentPath = new File( this.contentPath, this.servletName );

    // Some debug info.
    log.debug( "init(): root path        = " + this.rootPath.toString() );
    log.debug( "init(): dqc root path    = " + this.dqcRootPath.toString() );
    log.debug( "init(): content path     = " + this.contentPath.toString() );
    log.debug( "init(): dqc content path = " + this.dqcContentPath.toString() );

    // Copy initial content into content directory.
    String initialContentPath = ServletUtil.getInitialContentPath( this );
    try
    {
      ServletUtil.copyDir( initialContentPath, this.contentPath.getAbsolutePath() );
      log.debug( "init(): copied initial content directory <" + initialContentPath +
                 "> to the content directory <" + this.contentPath + ">." );
    }
    catch ( IOException ioe )
    {
      String tmpMsg = "Failed to copy " + initialContentPath + " to " + this.contentPath + ":" + ioe.getMessage();
      log.error( "init(): " + tmpMsg, ioe );
      throw new ServletException( tmpMsg, ioe );
    }

    log.debug( "init() done" );
  }

  /**
   * Redirect all GET requests.
   *
   *
   * @param req - the HttpServletRequest
   * @param res - the HttpServletResponse
   * @throws javax.servlet.ServletException if the request could not be handled for some reason.
   * @throws java.io.IOException if an I/O error is detected (when communicating with client not for servlet internal IO problems?).
   */
  public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
  {
    ServletUtil.logServerAccessSetup( req );

    // Get the request path and query information.
    String reqPath = req.getPathInfo();

    if ( reqPath == null )
    {
      doDispatch( req, res, false );
    }
    else if ( reqPath.startsWith( testRedirectPath ) )
    {
      this.handleGetRequestForRedirectTest( res, req );
    }
    else if ( reqPath.startsWith( testRedirectStopPath ) )
    {
      this.handleGetRequestForRedirectStopTest( res, req );
    }
    else
    {
      doDispatch( req, res, false );
    }

    return;
  }

  public void doPut( HttpServletRequest req, HttpServletResponse res )
          throws IOException, ServletException
  {
    ServletUtil.logServerAccessSetup( req );

    doDispatch( req, res, false );

    return;
  }

  /**
   * Dispatch the request to the target context and servlet.
   *
   * @param req
   * @param res
   */
  private void doDispatch( HttpServletRequest req, HttpServletResponse res, boolean useTestContext )
          throws IOException, ServletException
  {
    // Determine the request URI path.
    String requestURIPath = new StringBuffer()
            .append( req.getContextPath() )
            .append( req.getServletPath() )
            .append( req.getPathInfo() )
            .toString();

    String targetURIPath = convertRequestURLToResponseURL( requestURIPath, req, useTestContext );
    String targetURIPathNoContext = targetURIPath.substring( useTestContext
                                                             ? this.testTargetContextPath.length()
                                                             : this.targetContextPath.length() );

//    // Determine the target URI path without the context.
//    String targetURIPathNoContext;
//    if ( useTestContext )
//    {
//      if ( req.getPathInfo().length() > this.testRedirectPath.length())
//      {
//        targetURIPathNoContext = new StringBuffer()
//                .append( this.testTargetServletPath )
//                .append( this.testRedirectStopPath )
//                .append( req.getPathInfo().substring( this.testRedirectPath.length() ) )
//                .toString();
//      }
//      else
//      {
//        targetURIPathNoContext = new StringBuffer()
//                .append( this.testTargetServletPath )
//                .append( this.testRedirectStopPath )
//                .toString();
//      }
//    }
//    else
//    {
//      targetURIPathNoContext = new StringBuffer()
//              .append( this.targetServletPath )
//              .append( req.getPathInfo() )
//              .toString();
//    }
//
//    // Determine the target URI path with the context.
//    String targetURIPath = new StringBuffer()
//            .append( useTestContext ? this.testTargetContextPath : this.targetContextPath )
//            .append( targetURIPathNoContext )
//            .toString();

    String queryString = req.getQueryString();
    String reqURL = requestURIPath;
    String targetURL = targetURIPath;
    if ( queryString != null ) reqURL = reqURL + "?" + queryString;
    if ( queryString != null ) targetURL = targetURL + "?" + queryString;

    log.info( "doDispatch(): " + req.getRemoteHost()
              + " - dispatching request for URL \"" + reqURL
              + "\" to \"" + targetURL + "\"." );

    // Dispatch to the target URL.
    ServletContext context = this.getServletContext();
    ServletContext targetContext = context.getContext( useTestContext ? this.testTargetContextPath : this.targetContextPath );
    if ( targetContext == null )
    {
      String tmpMsg = "Null ServletContext for \"" + ( useTestContext ? this.testTargetContextPath : this.targetContextPath ) + "\".";
      log.warn( "doDispatch(): " + tmpMsg );
      res.sendError( HttpServletResponse.SC_NOT_FOUND, tmpMsg );
      ServletUtil.logServerAccess( HttpServletResponse.SC_NOT_FOUND, tmpMsg.length() );
      return;
    }
    RequestDispatcher dispatcher =
            targetContext.getRequestDispatcher( targetURIPathNoContext );
    if ( dispatcher == null )
    {
      String tmpMsg = "Null RequestDispatcher for \"" + targetURIPath + "\".";
      log.warn( "doDispatch(): " + tmpMsg );
      res.sendError( HttpServletResponse.SC_NOT_FOUND, tmpMsg );
      ServletUtil.logServerAccess( HttpServletResponse.SC_NOT_FOUND, tmpMsg.length() );
      return;
    }

    dispatcher.forward( req, res );
    ServletUtil.logServerAccess( HttpServletResponse.SC_OK, -1 );

    return;
  }

  private void handleGetRequestForRedirectTest( HttpServletResponse res, HttpServletRequest req )
          throws IOException, ServletException
  {
    String reqPath = req.getPathInfo();
    String queryString = req.getQueryString();

    log.debug( "handleGetRequestForRedirectTest(): handle GET path \"" + reqPath + "\") with query \"" + queryString + "\">." );
    if ( reqPath.equals( testRedirectPath ) )
    {
      if ( queryString == null )
      {
        log.warn( "handleGetRequestForRedirectTest(): request not understood <" + reqPath + ">." );
        res.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        ServletUtil.logServerAccess( HttpServletResponse.SC_BAD_REQUEST, 0 );
      }
      else if ( queryString.equals( "301" ) )
        this.doRedirect301( req, res, true );
      else if ( queryString.equals( "302" ) )
        this.doRedirect302( req, res, true );
      else if ( queryString.equals( "305" ) )
        this.doUseProxy305( req, res, true );
      else if ( queryString.equals( "dispatch" ) )
        this.doDispatch( req, res, true );
      else
      {
        log.warn( "handleGetRequestForRedirectTest(): request not understood <" + reqPath + " -- " + queryString + ">." );
        res.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        ServletUtil.logServerAccess( HttpServletResponse.SC_BAD_REQUEST, 0 );
      }
    }
    else if ( reqPath.equals( testRedirectPath + "/" ) )
    {
      if ( queryString == null )
      {
        log.debug( "handleGetRequestForRedirectTest(): redirect \"" + reqPath + "\") to index.html." );
        ServletUtil.returnFile( this, this.rootPath.getAbsolutePath(), "index.html", req, res, null );
      }
      else
      {
        log.warn( "handleGetRequestForRedirectTest(): request not understood <" + reqPath + " -- " + queryString + ">." );
        res.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        ServletUtil.logServerAccess( HttpServletResponse.SC_BAD_REQUEST, 0 );
      }
    }
    else if ( reqPath.equals( testRedirectPath + "/301" ) && queryString == null )
      this.doRedirect301( req, res, true );
    else if ( reqPath.equals( testRedirectPath + "/302" ) && queryString == null )
      this.doRedirect302( req, res, true );
    else if ( reqPath.equals( testRedirectPath + "/305" ) && queryString == null )
      this.doUseProxy305( req, res, true );
    else if ( reqPath.equals( testRedirectPath + "/dispatch" ) && queryString == null )
      this.doDispatch( req, res, true );
    else
    {
      log.warn( "handleGetRequestForRedirectTest(): request not understood <" + reqPath + " -- " + queryString + ">." );
      res.setStatus( HttpServletResponse.SC_BAD_REQUEST );
      ServletUtil.logServerAccess( HttpServletResponse.SC_BAD_REQUEST, 0 );
    }
    return;
  }

  private void handleGetRequestForRedirectStopTest( HttpServletResponse res, HttpServletRequest req )
          throws IOException
  {
    String reqPath = req.getPathInfo();
    String queryString = req.getQueryString();

    log.debug( "handleGetRequestForRedirectStopTest(): handle GET path \"" + reqPath + "\") with query \"" + queryString + "\">." );

    String title = "The Resource";
    String htmlResp = new StringBuffer()
            .append( "<html><head><title>" )
            .append( title )
            .append( "</title></head><body>" )
            .append( "<h1>" ).append( title ).append( "</h1>" )
            .append( "<ul>" )
            .append( "<li>" ).append( "Path : " ).append( reqPath ).append( "</li>" )
            .append( "<li>" ).append( "Query: " ).append( queryString ).append( "</li>" )
            .append( "</ul>" )
            .append( "</body></html>" )
            .toString();
    // Write the catalog out.
    PrintWriter out = res.getWriter();
    res.setContentType( "text/html" );
    out.print( htmlResp );
    res.setStatus( HttpServletResponse.SC_OK );
    ServletUtil.logServerAccess( HttpServletResponse.SC_OK, htmlResp.length() );

    return;
  }

  private void doRedirect301( HttpServletRequest req, HttpServletResponse res, boolean useTestContext )
          throws IOException
  {
    String requestURIPath = new StringBuffer()
            .append( req.getContextPath() )
            .append( req.getServletPath() )
            .append( req.getPathInfo() )
            .toString();
    String targetURIPath = convertRequestURLToResponseURL( requestURIPath, req, useTestContext );
    String targetURIPathNoContext = targetURIPath.substring( useTestContext
                                                             ? this.testTargetContextPath.length()
                                                             : this.targetContextPath.length() );
//    String targetURIPathNoContext = new StringBuffer()
//            .append( useTestContext ? this.testTargetServletPath : this.targetServletPath )
//            .append( this.testRedirectStopPath )
//            .toString();
//    String targetURIPath = new StringBuffer()
//            .append( useTestContext ? this.testTargetContextPath : this.targetContextPath )
//            .append( targetURIPathNoContext )
//            .toString();

    String queryString = req.getQueryString();
    if ( queryString != null ) targetURIPath = targetURIPath + "?" + queryString;

    targetURIPath = res.encodeRedirectURL( targetURIPath );
    log.info( "doRedirect301(): " + req.getRemoteHost() + " - requested URL \"" + requestURIPath
               + "\" permanently moved, redirect to \"" + targetURIPath + "\"." );
    res.setStatus( HttpServletResponse.SC_MOVED_PERMANENTLY );
    res.addHeader( "Location", targetURIPath );

    String title = "Permanently Moved - 301";
    String body = new StringBuffer()
            .append( "<p>" )
            .append( "The requested URL <" ).append( req.getRequestURL() )
            .append( "> has been permanently moved (HTTP status code 301)." )
            .append( " Instead, please use the following URL: <a href=\"" ).append( targetURIPath ).append( "\">" ).append( targetURIPath ).append( "</a>." )
            .append( "</p>" )
            .toString();
    String htmlResp = new StringBuffer()
            .append( "<html><head><title>" )
            .append( title )
            .append( "</title></head><body>" )
            .append( "<h1>" ).append( title ).append( "</h1>" )
            .append( body )
            .append( "</body></html>" )
            .toString();
    // Write the catalog out.
    PrintWriter out = res.getWriter();
    res.setContentType( "text/html" );
    out.print( htmlResp );

    ServletUtil.logServerAccess( HttpServletResponse.SC_MOVED_PERMANENTLY, 0 );
    return;
  }

  private void doRedirect302( HttpServletRequest req, HttpServletResponse res, boolean useTestContext )
          throws IOException
  {
    String requestURIPath = new StringBuffer()
            .append( req.getContextPath() )
            .append( req.getServletPath() )
            .append( req.getPathInfo() )
            .toString();
    String targetURIPath = convertRequestURLToResponseURL( requestURIPath, req, useTestContext );
    String targetURIPathNoContext = targetURIPath.substring( useTestContext
                                                             ? this.testTargetContextPath.length()
                                                             : this.targetContextPath.length() );

//    String targetURIPathNoContext = new StringBuffer()
//            .append( useTestContext ? this.testTargetServletPath : this.targetServletPath )
//            .append( this.testRedirectStopPath )
//            .toString();
//    String targetURIPath = new StringBuffer()
//            .append( useTestContext ? this.testTargetContextPath : this.targetContextPath )
//            .append( targetURIPathNoContext )
//            .toString();

    String queryString = req.getQueryString();
    if ( queryString != null ) targetURIPath = targetURIPath + "?" + queryString;

    targetURIPath = res.encodeRedirectURL( targetURIPath );

    log.info( "doRedirect302(): " + req.getRemoteHost() + " - requested URL \"" + requestURIPath
               + "\" temporarily moved, redirect to \"" + targetURIPath + "\"." );

    String title = "Temporarily Moved - 302";
    String body = new StringBuffer()
            .append( "<p>" )
            .append( "The requested URL <" ).append( req.getRequestURL() )
            .append( "> has been temporarily moved (HTTP status code 302)." )
            .append( " Instead, please use the following URL: <a href=\"" ).append( targetURIPath ).append( "\">" ).append( targetURIPath ).append( "</a>." )
            .append( "</p>" )
            .toString();
    String htmlResp = new StringBuffer()
            .append( "<html><head><title>" )
            .append( title )
            .append( "</title></head><body>" )
            .append( "<h1>" ).append( title ).append( "</h1>" )
            .append( body )
            .append( "</body></html>" )
            .toString();
    // Write the catalog out.
    PrintWriter out = res.getWriter();
    res.setContentType( "text/html" );
    out.print( htmlResp );

    res.sendRedirect( targetURIPath );
    //res.setStatus( HttpServletResponse.SC_MOVED_TEMPORARILY );
    //res.addHeader( "Location", targetURIPath );

    ServletUtil.logServerAccess( HttpServletResponse.SC_MOVED_TEMPORARILY, 0 );
    return;
  }

  private void doUseProxy305( HttpServletRequest req, HttpServletResponse res, boolean useTestContext )
          throws IOException
  {
    String reqURL = req.getRequestURL().toString();

    String targetURL = res.encodeRedirectURL(
            convertRequestURLToResponseURL( reqURL, req, useTestContext ) );

    String queryString = req.getQueryString();
    if ( queryString != null ) reqURL = reqURL + "?" + queryString;
    if ( queryString != null ) targetURL = targetURL + "?" + queryString;
    log.info( "doUseProxy305(): " + req.getRemoteHost() + " - proxy requested URI \"" + reqURL
               + "\" to \"" + targetURL + "\"." );
    res.addHeader( "Location", targetURL );

    String title = "Use Proxy - 305";
    String body = new StringBuffer()
            .append( "<ul>" )
            .append( "<li>" ).append( "request URL : " ).append( req.getRequestURL() ).append( "</li>" )
            .append( "<li>" ).append( "proxy URL   : <a href=\"" ).append( targetURL ).append( "\">" ).append( targetURL ).append( "</a></li>" )
            .append( "</ul>" )
            .toString();
    String htmlResp = new StringBuffer()
            .append( "<html><head><title>" )
            .append( title )
            .append( "</title></head><body>" )
            .append( "<h1>" ).append( title ).append( "</h1>" )
            .append( body )
            .append( "</body></html>" )
            .toString();
    // Write the catalog out.
    PrintWriter out = res.getWriter();
    res.setContentType( "text/html" );
    out.print( htmlResp );
    res.setStatus( HttpServletResponse.SC_USE_PROXY );
    ServletUtil.logServerAccess( HttpServletResponse.SC_USE_PROXY, 0 );
    return;
  }

  private String convertRequestURLToResponseURL( String reqURL, HttpServletRequest req, boolean useTestContext )
  {
    StringBuffer reqURLBuffer = new StringBuffer( reqURL );
    String strToReplace = useTestContext
                          ? ( req.getPathInfo().length() > this.testRedirectPath.length()
                              ? this.testRedirectPath
                              : req.getPathInfo() )
                          : req.getContextPath() + req.getServletPath();

    int strToReplaceStart = reqURL.indexOf( strToReplace );
    int strToReplaceEnd = strToReplaceStart + strToReplace.length();
    reqURLBuffer.replace( strToReplaceStart, strToReplaceEnd,
                          useTestContext
                          ? this.testRedirectStopPath
                          : targetContextPath + targetServletPath );
    return reqURLBuffer.toString();
  }
}
/*
* $Log: DqcServletRedirect.java,v $
* Revision 1.10  2006/03/30 23:22:09  edavis
* Refactor THREDDS servlet framework, especially CatalogRootHandler and ServletUtil.
*
* Revision 1.9  2006/01/20 20:42:04  caron
* convert logging
* use nj22 libs
*
* Revision 1.8  2005/10/11 19:44:29  caron
* release 3.3
*
* Revision 1.7  2005/09/27 21:48:49  edavis
* Clean up logging in DqcServletRedirect.
*
* Revision 1.6  2005/08/31 17:10:56  edavis
* Update DqcServletRedirect for release as dqcServlet.war. It forwards
* /dqcServlet/*, /dqcServlet/dqc/*, and /dqcServlet/dqcServlet/* requests
* to /thredds/dqc/*. It also provides some URLs for testing various HTTP
*  redirections (301, 302, 305) and forwarding (i.e.,
* javax.servlet.RequestDispatcher.forward()) at /dqcServlet/redirect-test/.
*
* Revision 1.5  2005/08/22 19:39:12  edavis
* Changes to switch /thredds/dqcServlet URLs to /thredds/dqc.
* Expand testing for server installations: TestServerSiteFirstInstall
* and TestServerSite. Fix problem with compound services breaking
* the filtering of datasets.
*
* Revision 1.4  2005/07/13 22:48:07  edavis
* Improve server logging, includes adding a final log message
* containing the response time for each request.
*
* Revision 1.3  2005/04/12 20:52:36  edavis
* Setup to handle logging of the response status for each
* servlet request handled (logging similar to Apache web
* server access_log).
*
* Revision 1.2  2005/04/05 22:37:03  edavis
* Convert from Log4j to Jakarta Commons Logging.
*
* Revision 1.1  2004/08/30 23:09:49  edavis
* Added DqcServletRedirect servlet to redirect /dqcServlet/dqc/* and /dqcServlet/dqcServlet/* to /thredds/dqcServlet/*.
*
*
*/
