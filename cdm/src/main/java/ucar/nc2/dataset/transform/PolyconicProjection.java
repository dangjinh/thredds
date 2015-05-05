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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ucar.nc2.dataset.transform;

import ucar.nc2.AttributeContainer;
import ucar.nc2.dataset.ProjectionCT;
import ucar.unidata.geoloc.Earth;

/**
 * Polyconic Projection.
 * @author ghansham@sac.isro.gov.in 1/8/2012
 */
public class PolyconicProjection extends AbstractTransformBuilder implements HorizTransformBuilderIF {

  public ProjectionCT makeCoordinateTransform(AttributeContainer ctv, String geoCoordinateUnits) {

    double lon0 = readAttributeDouble(ctv, "longitude_of_central_meridian", Double.NaN);
    double lat0 = readAttributeDouble(ctv, "latitude_of_projection_origin", Double.NaN);

    double semi_major_axis = readAttributeDouble(ctv, "semi_major_axis", Double.NaN);
    double semi_minor_axis = readAttributeDouble(ctv, "semi_minor_axis", Double.NaN);
    double inverse_flattening = readAttributeDouble(ctv, "inverse_flattening", 0.0);

    ucar.unidata.geoloc.ProjectionImpl proj;

    // check for ellipsoidal earth
    if (!Double.isNaN(semi_major_axis) && (!Double.isNaN(semi_minor_axis) || inverse_flattening != 0.0)) {
      Earth earth = new Earth(semi_major_axis, semi_minor_axis, inverse_flattening);
      proj = new ucar.unidata.geoloc.projection.proj4.PolyconicProjection(lat0, lon0, earth);
    } else {
      proj = new ucar.unidata.geoloc.projection.proj4.PolyconicProjection(lat0, lon0);
    }

    return new ProjectionCT(ctv.getName(), "FGDC", proj);
  }

  public String getTransformName() {
    return "polyconic";
  }
}
