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

Our CICD creates using the REST API, so have to see if that also ends up here or somewhere similar.

``` java
try {
                        URLConnection connection = uri.toURL().openConnection();
                        connection.setConnectTimeout(10000);
                        is = connection.getInputStream();
                    } catch (Exception e) {


                        is = connection.getInputStream();
                        isValid() returns true
```

So, because it goes out and tries and gets something back it thinks its OK.
We now have this type of verification in PairsFormat.java for files and URL. If URL hosted by pairs hbase data service
it can support global 'connections' or specific depending on the URL endpoints we provide and if we send virtual layer metadata of some
sort back in the response. This might be useful.
 * We should create a geotools 'Reader' object for accessing our layers
 * We should create specific Parameter in PairsFormat that is passed back to us in hints for better matching of our plugin to our layer connections

## Coverage and image manipulation utilities

## debugging coverage

WCSEnvelope coordinate reference system cannot be null
public WCSEnvelope(CoordinateReferenceSystem crs) {
        if (crs == null) {
            throw new IllegalArgumentException(
                    "WCSEnvelope coordinate reference system cannot be null");
        }