# Notes on how plugin works for layers and multi-layers Feb 2021+

## Coverage plugin discovery, Coverage & image manipulation, .... etc geotools

```java
Coverage.java
CoverageProcessor.java
Operations.java
GridCoverage2D
GridGeoemetry2D
look in this geotools package: org.geotools.coverage.processing

org.geotools.coverage.processing.operation.Crop;
import org.geotools.coverage.processing.operation.Resample;

org.geotools.coverage.processing.operation.Crop;
import org.geotools.coverage.processing.operation.Resample;
```

## Creating the GridCoverage2D
Gridcoverage2D wraps a PlanarImage, doesn't seem to encourage multiband images except for ColorModel bands

``` java
public class GridCoverageFactory extends AbstractFactory {
.
.
public GridCoverage2D create(
            final CharSequence name, final float[][] matrix, final Envelope envelope) {
.
.
raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, width, height, 1, null);
```

## Create a coverageStore 
How does url verification work and can we do anything about it?

From the UI panel 'Add Raster Data Source'. We can search this String and find in the Geoserver source code its in GeoserverApplication.properties in src/.../resources. Then we can get a property name such as the error msg `FileExistsValidator.fileNotFoundError = Could not find file: ${file}'. Then we can search the java for the key 'FileExistsValidator.fileNotFoundError' and see if we can do anything about it. This key in /Users/bobroff/projects/geoserver/geoserver-2.16.2/geoserver-2.16.2/src/web/core/src/main/java/org/geoserver/web/wicket/FileExistsValidator.java. FileExistsValidator.java is the initial entry point to 
validate the url. If scheme is not 'file:' it tries to open url which surprisingly works for 'http://ibmpairs'

``` java
try {
                        URLConnection connection = uri.toURL().openConnection();
                        connection.setConnectTimeout(10000);
                        is = connection.getInputStream();
                    } catch (Exception e) {


                        is = connection.getInputStream();
                        isValid() returns true
```

and the chunk inputstream is

```
IOUtils.toString(is)
"<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><html><head><meta http-equiv="refresh" content="0;url=https://searchassist.verizon.com/main?ParticipantID=euekiz39ksg8nwp7iqj2fp5wzfwi5q76&FailedURI=http%3A%2F%2Fibmpairs%2F&FailureMode=1&Implementation=&AddInType=4&Version=pywr1.0&ClientLocation=us"/><script type="text/javascript">url="https://searchassist.verizon.com/main?ParticipantID=euekiz39ksg8nwp7iqj2fp5wzfwi5q76&FailedURI=http%3A%2F%2Fibmpairs%2F&FailureMode=1&Implementation=&AddInType=4&Version=pywr1.0&ClientLocation=us";if(top.location!=location){var w=window,d=document,e=d.documentElement,b=d.body,x=w.innerWidth||e.clientWidth||b.clientWidth,y=w.innerHeight||e.clientHeight||b.clientHeight;url+="&w="+x+"&h="+y;}window.location.replace(url);</script></head><body></body></html>"
```

So, because it goes out and tries and gets something back it thinks its OK

## Coverage and image manipulation utilities

