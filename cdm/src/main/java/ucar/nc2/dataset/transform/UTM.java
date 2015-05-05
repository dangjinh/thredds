/*
 * Copyright 1998-2014 University Corporation for Atmospheric Research/Unidata
 *
 *   Portions of this software were developed by the Unidata Program at the
 *   University Corporation for Atmospheric Research.
 *
 *   Access and use of this software shall impose the following obligations
 *   and understandings on the user. The user is granted the right, without
 *   any fee or cost, to use, copy, modify, alter, enhance and distribute
 *   this software, and any derivative works thereof, and its supporting
 *   documentation for any purpose whatsoever, provided that this entire
 *   notice appears in all copies of the software, derivative works and
 *   supporting documentation.  Further, UCAR requests that the user credit
 *   UCAR/Unidata in any publications that result from the use of this
 *   software or in any product that includes this software. The names UCAR
 *   and/or Unidata, however, may not be used in any advertising or publicity
 *   to endorse or promote any products or commercial entity unless specific
 *   written permission is obtained from UCAR/Unidata. The user also
 *   understands that UCAR/Unidata is not obligated to provide the user with
 *   any support, consulting, training or assistance of any kind with regard
 *   to the use, operation and performance of this software nor to provide
 *   the user with any updates, revisions, new versions or "bug fixes."
 *
 *   THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *   IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *   INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *   FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *   NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *   WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package ucar.nc2.dataset.transform;

import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainer;
import ucar.nc2.dataset.ProjectionCT;
import ucar.unidata.geoloc.projection.UtmProjection;

/**
 * Create a UTM Projection from the information in the Coordinate Transform Variable.
 *  *
 * @author caron
 */
public class UTM extends AbstractTransformBuilder implements HorizTransformBuilderIF {

  public String getTransformName() {
    return UtmProjection.GRID_MAPPING_NAME;
  }

  public ProjectionCT makeCoordinateTransform(AttributeContainer ctv, String geoCoordinateUnits) {
    double zoned = readAttributeDouble( ctv, UtmProjection.UTM_ZONE1, Double.NaN);
    if (Double.isNaN(zoned))
      zoned = readAttributeDouble( ctv, UtmProjection.UTM_ZONE2, Double.NaN);
    if (Double.isNaN(zoned))
      throw new IllegalArgumentException("No zone was specified") ;

    int zone = (int) zoned;
    boolean isNorth = zone > 0;
    zone = Math.abs(zone);

    Attribute a;
    double axis = 0.0, f = 0.0;
    if (null != (a = ctv.findAttribute( "semimajor_axis")))
      axis = a.getNumericValue().doubleValue();
    if (null != (a = ctv.findAttribute( "inverse_flattening")))
      f = a.getNumericValue().doubleValue();

    // double a, double f, int zone, boolean isNorth
    UtmProjection proj = (axis != 0.0) ? new UtmProjection(axis, f, zone, isNorth) : new UtmProjection(zone, isNorth);
    return new ProjectionCT(ctv.getName(), "FGDC", proj);
  }
}
