Threre are two main sections in this tracing, first part tracing call through native geotiff store.

# Using local geoserver layer that is a geotiff from the original farm field image of Steffand sentinel2 ndvi

## Using a local layer also goes down this same code path with gs:cropCoverage

* Start tracing at applyRenderingTransformation

public abstract class RenderingTransformationHelper 
public Object applyRenderingTransformation(

            Expression transformation,
            FeatureSource featureSource,
            Query layerQuery,
            Query renderingQuery,
            GridGeometry2D gridGeometry,
            CoordinateReferenceSystem sourceCrs,
            RenderingHints hints)

### transformation

**Note** the affine transformation is going to convert pixel coords to lon/lat and the inverse.

``` 
ProcessFunction@17641
fallbackValue:null
functionName:FunctionNameImpl@17820 "gs:CropCoverage"
args:ArrayList@17849 size=2
0:Parameter@17854 "coverage:GridCoverage2D"
1:Parameter@17855 "cropShape:Geometry"
description:SimpleInternationalString@17864 "Geometry used to crop the raster"
defaultValue:"Geometry used to crop the raster"
hash:0
value:char[32]@17906
key:"cropShape"
hash:1282522865
value:char[9]@17907
maxOccurs:1
metadata:Collections$UnmodifiableMap@17866 size=0
minOccurs:1
required:true
sample:null
```

### featuresource

**Note** in featureSource POLYGON corresponds to the geotiff backing the store

``` 
0:SimpleFeatureImpl@17700 "SimpleFeatureImpl:GridCoverage=[SimpleFeatureImpl.Attribute: geom<geom id=fid--2ea698de_170ceef4e21_-7fe1>=POLYGON ((-1.430784000000017 51.043712000000006, -1.4103040000000169 51.043712000000006, -1.4103040000000169 51.05190400000001, -1.430784000000017 51.05190400000001, -1.430784000000017 51.043712000000006)), SimpleFeatureImpl.Attribute: grid<grid id=fid--2ea698de_170ceef4e21_-7fe1>=org.geoserver.catalog.CoverageDimensionCustomizerReader@601e3f89, SimpleFeatureImpl.Attribute: params<params id=fid--2ea698de_170ceef4e21_-7fe1>=[Lorg.opengis.parameter.GeneralParameterValue;@51b71539]"
attributeUserData:null
featureType:SimpleFeatureTypeImpl@17702 "SimpleFeatureTypeImpl http://www.opengis.net/gml:GridCoverage identified extends Feature(geom:geom,grid:grid,params:params)"
id:FeatureIdImpl@17703 "fid--2ea698de_170ceef4e21_-7fe1"
index:HashMap@17704 size=4
userData:null
validating:false
values:Object[3]@17705
0:Polygon@17708 "POLYGON ((-1.430784000000017 51.043712000000006, -1.4103040000000169 51.043712000000006, -1.4103040000000169 51.05190400000001, -1.430784000000017 51.05190400000001, -1.430784000000017 51.043712000000006))"
1:CoverageDimensionCustomizerReader@17709
coverageName:"High res  imagery (ESA Sentinel 2)-Normalized difference vegetation index-02_06_2020T00_00_00"
delegate:SingleGridCoverage2DReader@17719
info:CoverageInfoImpl@17720 "CoverageInfoImpl[sentinel2ndvilayersteffan_test1]"
2:GeneralParameterValue[3]@17710
0:Parameter@17712 "InputTransparentColor = null\n"
1:Parameter@17713 "SUGGESTED_TILE_SIZE = "512,512"\n"
2:Parameter@17714 "RescalePixels = "false"\n"
hints:null
listeners:null
```

### gridGeometry

**Note** in grid geometry the envelope and gridRange values are those of the bbox and pixels on the http request

``` 
crs2D:DefaultGeographicCRS@17645 "GEOGCS["WGS 84", \n  DATUM["World Geodetic System 1984", \n    SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]], \n    AUTHORITY["EPSG","6326"]], \n  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]], \n  UNIT["degree", 0.017453292519943295], \n  AXIS["Geodetic longitude", EAST], \n  AXIS["Geodetic latitude", NORTH], \n  AUTHORITY["EPSG","4326"]]"
crsToCorner2D:null
envelope:GeneralEnvelope@17656 "GeneralEnvelope[(-1.42898162, 51.04441321), (-1.41101315, 51.05102867)]"
crs:DefaultGeographicCRS@17645 "GEOGCS["WGS 84", \n  DATUM["World Geodetic System 1984", \n    SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]], \n    AUTHORITY["EPSG","6326"]], \n  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]], \n  UNIT["degree", 0.017453292519943295], \n  AXIS["Geodetic longitude", EAST], \n  AXIS["Geodetic latitude", NORTH], \n  AUTHORITY["EPSG","4326"]]"
ordinates:double[4]@17676
gridDimensionX:0
gridDimensionY:1
gridFromCRS2D:AffineTransform2D@17657 "PARAM_MT["Affine", \n  PARAMETER["num_row", 3], \n  PARAMETER["num_col", 3], \n  PARAMETER["elt_0_0", 14247.178529947168], \n  PARAMETER["elt_0_2", 20358.456256153124], \n  PARAMETER["elt_1_1", -38697.2334501362], \n  PARAMETER["elt_1_2", 1975533.074312586]]"
gridRange:GridEnvelope2D@17658 "GridEnvelope2D[0..255, 0..255]"
height:256
width:256
x:0
y:0
gridToCRS:AffineTransform2D@17659 "PARAM_MT["Affine", \n  PARAMETER["num_row", 3], \n  PARAMETER["num_col", 3], \n  PARAMETER["elt_0_0", 0.00007018933593750006], \n  PARAMETER["elt_0_2", -1.4289465253320313], \n  PARAMETER["elt_1_1", -0.00002584164062499616], \n  PARAMETER["elt_1_2", 51.05101574917969]]"
gridToCRS2D:AffineTransform2D@17659 "PARAM_MT["Affine", \n  PARAMETER["num_row", 3], \n  PARAMETER["num_col", 3], \n  PARAMETER["elt_0_0", 0.00007018933593750006], \n  PARAMETER["elt_0_2", -1.4289465253320313], \n  PARAMETER["elt_1_1", -0.00002584164062499616], \n  PARAMETER["elt_1_2", 51.05101574917969]]"
```

* Now a reader is obtained

``` 
                   final GridCoverage2DReader reader =
                            (GridCoverage2DReader) GRID_PROPERTY_NAME.evaluate(gridWrapper);
                    // don't read more than the native resolution (in case we are oversampling)
```

**NOTE** The reader accesses the geotiff file: So reader.get... () below are the limits in lon, lat and pixels x, y of the geotiff file.

``` 

### reader.getOriginalEnvelope()

GeneralEnvelope@18004 "GeneralEnvelope[(-1.430784000000017, 51.043712000000006), (-1.4103040000000169, 51.05190400000001)]"
crs:DefaultGeographicCRS@17645 "GEOGCS["WGS 84", \n  DATUM["World Geodetic System 1984", \n    SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]], \n    AUTHORITY["EPSG","6326"]], \n  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]], \n  UNIT["degree", 0.017453292519943295], \n  AXIS["Geodetic longitude", EAST], \n  AXIS["Geodetic latitude", NORTH], \n  AUTHORITY["EPSG","4326"]]"
ordinates:double[4]@18013
0:-1.430784
1:51.043712
2:-1.410304
3:51.051904
```

### reader.getOriginalGridRange()

GridEnvelope2D@18023 "GridEnvelope2D[0..319, 0..127]"
height:128
width:320
x:0
y:0

* Continuing into the transformation code 

``` java
                if (FeatureUtilities.isWrappedCoverageReader(simpleSchema)) {
                    GeneralParameterValue[] params =
                            PARAMS_PROPERTY_NAME.evaluate(
                                    gridWrapper, GeneralParameterValue[].class);
                    final GridCoverage2DReader reader =
                            (GridCoverage2DReader) GRID_PROPERTY_NAME.evaluate(gridWrapper);
```

readGG remains request input gridGeometry (request bbox, ... )
gridWrapper is the feature source ( geotiff file)

reader is a CoverageDimensionCustomizerReader, still corresponding values of the geotiff store file
**Note** latLonBoundingBox: ReferencedEnvelope@17898 "ReferencedEnvelope[-180.0 : 180.0, -90.0 : 90.0]", as is the nativeboundingbox

**reader**
```
CoverageDimensionCustomizerReader@17860
coverageName: "High res  imagery (ESA Sentinel 2)-Normalized difference vegetation index-02_06_2020T00_00_00"
delegate: SingleGridCoverage2DReader@17884
info: CoverageInfoImpl@17885 "CoverageInfoImpl[sentinel2ndvilayersteffan_test1]"
_abstract: null
advertised: null
alias: ArrayList@17887 size=0
catalog: CatalogImpl@17888
dataLinks: ArrayList@17889 size=0
defaultInterpolationMethod: "nearest neighbor"
description: "Generated from GeoTIFF"
dimensions: ArrayList@17892 size=1
disabledServices: ArrayList@17893 size=0
enabled: true
grid: GridGeometry2D@17894 "GridGeometry2D[GeneralGridEnvelope[0..319, 0..127], PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 0.000064], 
  PARAMETER["elt_0_2", -1.430752000000017], 
  PARAMETER["elt_1_1", -0.000064], 
  PARAMETER["elt_1_2", 51.05187200000001]]]"
arbitraryToInternal: null
axisDimensionX: 0
axisDimensionY: 1
cornerToCRS: null
cornerToCRS2D: null
crs2D: DefaultGeographicCRS@17710 "GEOGCS["WGS 84", 
  DATUM["World Geodetic System 1984", 
    SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]], 
    AUTHORITY["EPSG","6326"]], 
  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]], 
  UNIT["degree", 0.017453292519943295], 
  AXIS["Geodetic longitude", EAST], 
  AXIS["Geodetic latitude", NORTH], 
  AUTHORITY["EPSG","4326"]]"
crsToCorner2D: null
envelope: GeneralEnvelope@17952 "GeneralEnvelope[(-1.430784000000017, 51.04371200000001), (-1.4103040000000169, 51.05190400000001)]"
gridDimensionX: 0
gridDimensionY: 1
gridFromCRS2D: AffineTransform2D@17953 "PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 15625.0], 
  PARAMETER["elt_0_2", 22355.500000000266], 
  PARAMETER["elt_1_1", -15625.0], 
  PARAMETER["elt_1_2", 797685.5000000002]]"
gridRange: GeneralGridEnvelope@17954 "GeneralGridEnvelope[0..319, 0..127]"
high: null
index: int[4]@17964
0: 0
1: 0
2: 320
3: 128
low: null
gridToCRS: AffineTransform2D@17955 "PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 0.000064], 
  PARAMETER["elt_0_2", -1.430752000000017], 
  PARAMETER["elt_1_1", -0.000064], 
  PARAMETER["elt_1_2", 51.05187200000001]]"
gridToCRS2D: AffineTransform2D@17955 "PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 0.000064], 
  PARAMETER["elt_0_2", -1.430752000000017], 
  PARAMETER["elt_1_1", -0.000064], 
  PARAMETER["elt_1_2", 51.05187200000001]]"
id: "CoverageInfoImpl--7552a3e3:170c640e58a:-7ffc"
interpolationMethods: ArrayList@17896 size=3
keywords: ArrayList@17897 size=3
latLonBoundingBox: ReferencedEnvelope@17898 "ReferencedEnvelope[-180.0 : 180.0, -90.0 : 90.0]"
metadata: MetadataMap@17899 size=2
metadataLinks: ArrayList@17900 size=0
name: "sentinel2ndvilayersteffan_test1"
namespace: NamespaceInfoImpl@17902 "NamespaceInfoImpl[pairs:https://pairs.res.ibm.com]"
nativeBoundingBox: ReferencedEnvelope@17903 "ReferencedEnvelope[-180.0 : 180.0, -90.0 : 90.0]"
nativeCoverageName: "High res  imagery (ESA Sentinel 2)-Normalized difference vegetation index-02_06_2020T00_00_00"
nativeCRS: DefaultGeographicCRS@17904 "GEOGCS["WGS 84", 
  DATUM["World Geodetic System 1984", 
    SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]], 
    AUTHORITY["EPSG","6326"]], 
  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]], 
  UNIT["degree", 0.017453292519943295], 
  AXIS["Geodetic longitude", EAST], 
  AXIS["Geodetic latitude", NORTH], 
  AUTHORITY["EPSG","4326"]]"
nativeFormat: "GeoTIFF"
nativeName: "sentinel2ndvilayersteffan_test1"
parameters: HashMap@17906 size=3
projectionPolicy: ProjectionPolicy$2@17907 "REPROJECT_TO_DECLARED"
requestSRS: ArrayList@17908 size=1
responseSRS: ArrayList@17909 size=1
serviceConfiguration: false
srs: "EPSG:4326"
store: CoverageStoreInfoImpl@17911 "CoverageStoreInfoImpl[sentinel2ndvi49464steffan]"
supportedFormats: ArrayList@17912 size=9
title: "High res  imagery (ESA Sentinel 2)-Normalized difference vegetation index-02_06_2020T00_00_00"
```

* Tracing the affine transformations. These xforms map pixel coordinates to (lon, lat) in some fashion

**Note** PAIRS uses the CELL_CORNER
``` java 

                        MathTransform g2w = reader.getOriginalGridToWorld(PixelInCell.CELL_CORNER);
                        if (g2w instanceof AffineTransform2D
                                && readGG.getGridToCRS2D() instanceof AffineTransform2D) {
                                                           if (g2w instanceof AffineTransform2D
                                && readGG.getGridToCRS2D() instanceof AffineTransform2D) {
                            AffineTransform2D atOriginal = (AffineTransform2D) g2w;
                            AffineTransform2D atMap = (AffineTransform2D) readGG.getGridToCRS2D();
                            if (XAffineTransform.getScale(atMap)
                                    < XAffineTransform.getScale(atOriginal)) {
                                // we need to go trough some convoluted code to make sure the new
                                // grid geometry
                                // has at least one pixel

``` 
**Note** Here the size of pixels (degrees/pixel) atOriginal is that of the backing geotiff (square pixels). It came from the mathTransform of the reader which goes to the Geoserver datastore.
XAffineTransform.getScale(atOriginal)
0.000064

**Note** The atMap xform and scale come from readGG which at this point in the code still comes from gridGeometry which is the http request bbox
XAffineTransform.getScale(atMap)
0.000048

The http WMS request was for a map of higher resolution (0.000048) than that of the datastore (0.000064).

**Note** If I change the request pixel grid to be lower resolution it shouldn't go through this code path?

* Continuing now with the transform

``` java
                            if (XAffineTransform.getScale(atMap)
                                    < XAffineTransform.getScale(atOriginal)) {
                                // we need to go trough some convoluted code to make sure the new
                                // grid geometry
                                // has at least one pixel

                                org.opengis.geometry. Envelope worldEnvelope =
                                        gridGeometry.getEnvelope(); 
                                GeneralEnvelope transformed =
                                        org.geotools.referencing. CRS.transform(
                                                atOriginal.inverse(), worldEnvelope); 
                                int minx = (int) Math.floor(transformed.getMinimum(0));
                                int miny = (int) Math.floor(transformed.getMinimum(1));
                                int maxx = (int) Math.ceil(transformed.getMaximum(0));
                                int maxy = (int) Math.ceil(transformed.getMaximum(1));
                                Rectangle rect =
                                        new Rectangle(
                                                minx - TRANSFORM_READ_BUFFER_PIXELS,
                                                miny - TRANSFORM_READ_BUFFER_PIXELS,
                                                (maxx - minx) + TRANSFORM_READ_BUFFER_PIXELS * 2,
                                                (maxy - miny) + TRANSFORM_READ_BUFFER_PIXELS * 2);
                                GridEnvelope2D gridEnvelope = new GridEnvelope2D(rect);
                                readGG =
                                        new GridGeometry2D(
                                                gridEnvelope,
                                                PixelInCell.CELL_CORNER,
                                                atOriginal,
                                                worldEnvelope.getCoordinateReferenceSystem(),
                                                null);
                            }
                        }
                    }
```

**At this point**, the requested bbox and pixel width/hieght have been rescaled so that the degrees/pixel of **readGG** geometry matches
that of the geotiff (0.000064 deg/pixel). Both the bbox and the pixel range have been changed. The original request http param bbox is compared to
the new readGG. Also note, the new envelope encompasses the original. bbox < readGG < geotiff
```
bbox http params (-1.42898162,        51.04441321 ),       (,-1.41101315,       51.05102867  )
readGG           (-1.429632000000017, 51.043712000000006), (-1.410368000000017, 51.05171200000001)
geotiff          (-1.430784000000017, 51.043712000000006), (-1.4103040000000169, 51.05190400000001)
```


**worldEnvelope** this is the envelope of bbox on the http request (i.e.the submitted BBox)
GeneralEnvelope@18274 "GeneralEnvelope[(-1.42898162, 51.04441321), (-1.41101315, 51.05102867)]"

**transformed** new pixel coords corresonding to the request bbox given the scale of the original geotiff (deg/pixel)
GeneralEnvelope@18322 "GeneralEnvelope[(28.162187500263826, 13.677031250088476), (308.9195312502634, 117.04359375010245)]"
crs:null
ordinates:double[4]@18348
0:28.162188
1:13.677031
2:308.919531
3:117.043594

**gridEnvelope** 
GridEnvelope2D@18436 "GridEnvelope2D[18..318, 3..127]"
height:125
width:301
x:18
y:3

**readGG**
```
GridGeometry2D@18497 "GridGeometry2D[GridEnvelope2D[18..318, 3..127], PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 0.000064], 
  PARAMETER["elt_0_2", -1.430752000000017], 
  PARAMETER["elt_1_1", -0.000064], 
  PARAMETER["elt_1_2", 51.05187200000001]]]"
arbitraryToInternal: null
axisDimensionX: 0
axisDimensionY: 1
cornerToCRS: AffineTransform2D@18066 "PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 0.000064], 
  PARAMETER["elt_0_2", -1.430784000000017], 
  PARAMETER["elt_1_1", -0.000064], 
  PARAMETER["elt_1_2", 51.05190400000001]]"
cornerToCRS2D: AffineTransform2D@18066 "PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 0.000064], 
  PARAMETER["elt_0_2", -1.430784000000017], 
  PARAMETER["elt_1_1", -0.000064], 
  PARAMETER["elt_1_2", 51.05190400000001]]"
crs2D: DefaultGeographicCRS@17710 "GEOGCS["WGS 84", 
  DATUM["World Geodetic System 1984", 

    SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]], 
    AUTHORITY["EPSG","6326"]], 

  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG", "8901"]], 
  UNIT["degree", 0.017453292519943295], 
  AXIS["Geodetic longitude", EAST], 
  AXIS["Geodetic latitude", NORTH], 
  AUTHORITY["EPSG", "4326"]]"
crsToCorner2D: null
envelope: GeneralEnvelope@18513 "GeneralEnvelope[(-1.429632000000017, 51.043712000000006), (-1.410368000000017, 51.05171200000001)]"
crs: DefaultGeographicCRS@17710 "GEOGCS["WGS 84", 
  DATUM["World Geodetic System 1984", 

    SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]], 
    AUTHORITY["EPSG","6326"]], 

  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG", "8901"]], 
  UNIT["degree", 0.017453292519943295], 
  AXIS["Geodetic longitude", EAST], 
  AXIS["Geodetic latitude", NORTH], 
  AUTHORITY["EPSG", "4326"]]"
ordinates: double[4]@18539
gridDimensionX: 0
gridDimensionY: 1
gridFromCRS2D: AffineTransform2D@18514 "PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 15625.0], 
  PARAMETER["elt_0_2", 22355.500000000266], 
  PARAMETER["elt_1_1", -15625.0], 
  PARAMETER["elt_1_2", 797685.5000000002]]"
gridRange: GridEnvelope2D@18515 "GridEnvelope2D[18..318, 3..127]"
height: 125
width: 301
x: 18
y: 3
gridToCRS: AffineTransform2D@18516 "PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 0.000064], 
  PARAMETER["elt_0_2", -1.430752000000017], 
  PARAMETER["elt_1_1", -0.000064], 
  PARAMETER["elt_1_2", 51.05187200000001]]"
gridToCRS2D: AffineTransform2D@18516 "PARAM_MT["Affine", 
  PARAMETER["num_row", 3], 
  PARAMETER["num_col", 3], 
  PARAMETER["elt_0_0", 0.000064], 
  PARAMETER["elt_0_2", -1.430752000000017], 
  PARAMETER["elt_1_1", -0.000064], 
  PARAMETER["elt_1_2", 51.05187200000001]]"
```

* Continuing now to read the data
reader is the original geotiff, readGG is the new scaled envelope and pixelgrid)
``` java
                    coverage = readCoverage(reader, params, readGG);
```



```
=============================================================================
=============================================================================
=============================================================================
=============================================================================
=============================================================================
=============================================================================
```

# Pairspluginlayer tracing

RenderedImageMapOutputFormat.directRasterRender() line 956

## RenderedImageMapOutputFormat.directRasterRender()

  readGG envelope -> ordinates -1.42, 51, -1, 51

         mapEnvelope -> OK

   mapRasterArea 256, 256

    transformation gs:cropCoverage
    processName gs:CropCoverage

    Note: around line 1100 in geoserver-2.16.2/src/wms/src/main/java/org/geoserver/wms/map/

RenderedImageMapOutputFormat.java

    

RenderedImageMapOutputFormat.directRasterRender()
aournd line 1098

``` java
                 useGutter = !sameCRS || !(interpolation instanceof InterpolationNearest);
               }

                if (!useGutter) {
                    readGG = new GridGeometry2D(new GridEnvelope2D(mapRasterArea), mapEnvelope);
                } else {
                    //
                    // SG added gutter to the drawing. We need to investigate much more and also we
                    // need to do this only when needed
                    //
                    // enlarge raster area
                    Rectangle bufferedTargetArea = (Rectangle) mapRasterArea.clone();
                    bufferedTargetArea.add(
                            mapRasterArea.x + mapRasterArea.width + 10,
                            mapRasterArea.y + mapRasterArea.height + 10);
                    bufferedTargetArea.add(mapRasterArea.x - 10, mapRasterArea.y - 10);
                .
                .
                .
```                
line 1130 mapEnvelope still OK
mapEnvelope.toString()
"ReferencedEnvelope[-1.42898162 : -1.41101315, 51.04441321 : 51.05102867]"

``` java
                if (transformation != null) {
 1130                   RenderingTransformationHelper helper =
                            new RenderingTransformationHelper() {

                                protected GridCoverage2D readCoverage(
                                        GridCoverage2DReader reader,
                                        Object params,
                                        GridGeometry2D readGG)
                                        throws IOException {
                                    context.reader = reader;
                                    context.params = params;
                                    return readBestCoverage(
                                            context,
                                            ReferencedEnvelope.reference(readGG.getEnvelope()),
                                            readGG.getGridRange2D(),
                                            interpolation,
                                            readerBgColor,
                                            bandIndices);
                                }
                            };
```

this is where the transformation enlarges the envelope.
geotools/modules/library/render/src/main/java/org/geotools/renderer/lite/RenderingTransformationHelper.java
applyRenderingTransformation() tranforms and then invokes @override readCoverage() above which invokes pairs geoserver extension with a modified mapEnvelope.

``` java
  1150                 Object result =
                            helper.applyRenderingTransformation(
                                    transformation,
                                    layer.getFeatureSource(),
                                    layer.getQuery(),
                                    Query.ALL,
                                    readGG,
                                    coverageCRS,
                                    interpolationHints);
                    if (result == null) {
                        coverage = null;
                    } else if (result instanceof GridCoverage2D) {
                        coverage = (GridCoverage2D) result;
                        symbolizer =
                                updateSymbolizerForBandSelection(context, symbolizer, bandIndices);
                    } else {
                        // we don't know how to handle this case, we'll let streaming renderer fall
                        // back on this one
                        return null;
                    }
  
    public Object applyRenderingTransformation(
            Expression transformation,
            FeatureSource featureSource,
            Query layerQuery,
            Query renderingQuery,
            GridGeometry2D gridGeometry,
            CoordinateReferenceSystem sourceCrs,
            RenderingHints hints)
            throws IOException, SchemaException, TransformException, FactoryException {
        Object result = null;
    ```

envelope ordinates 
0:-1.428982
1:51.044413
2:-1.411013
3:51.051029

Code then around line 116 code takes path of reader.getCoordinateReferenceSystem() == gridGeometry.getCRS()
Note comment about don't read more than the native reso

``` 
                if (FeatureUtilities.isWrappedCoverageReader(simpleSchema)) {
                    GeneralParameterValue[] params =
                            PARAMS_PROPERTY_NAME.evaluate(
                                    gridWrapper, GeneralParameterValue[].class);
                    final GridCoverage2DReader reader =
                            (GridCoverage2DReader) GRID_PROPERTY_NAME.evaluate(gridWrapper);
                    // don't read more than the native resolution (in case we are oversampling)
                  if (CRS.equalsIgnoreMetadata(
                            reader.getCoordinateReferenceSystem(),
                            gridGeometry.getCoordinateReferenceSystem())) {
                        // GEOS-8070, changed the pixel anchor to corner. CENTER can cause issues
                        // with
                        // the BBOX calculation and cause it to be incorrect
                        MathTransform g2w = reader.getOriginalGridToWorld(PixelInCell.CELL_CORNER);
                        if (g2w instanceof AffineTransform2D
                                && readGG.getGridToCRS2D() instanceof AffineTransform2D) {
                            AffineTransform2D atOriginal = (AffineTransform2D) g2w;
                            AffineTransform2D atMap = (AffineTransform2D) readGG.getGridToCRS2D();
                            if (XAffineTransform.getScale(atMap)
                                    < XAffineTransform.getScale(atOriginal)) {
                                // we need to go trough some convoluted code to make sure the new
                                // grid geometry
                                // has at least one pixel
                            .
                            .
                            .
                               org.opengis.geometry.Envelope worldEnvelope =
                                        gridGeometry.getEnvelope();
                                GeneralEnvelope transformed =
                                        org.geotools.referencing.CRS.transform(
                                                atOriginal.inverse(), worldEnvelope);
```

At this point worldEnvelope is correct
ordinates:double[4]@15189
0:-1.428982
1:51.044413
2:-1.411013
3:51.051029

However, transformed is: not sure what is going on in this calculation 
0:253.967671
1:55.394093
2:253.993226
3:55.403501

Continuing; 

``` java
                                int minx = (int) Math.floor(transformed.getMinimum(0));
                                int miny = (int) Math.floor(transformed.getMinimum(1));
                                int maxx = (int) Math.ceil(transformed.getMaximum(0));
                                int maxy = (int) Math.ceil(transformed.getMaximum(1));
                                Rectangle rect =
                                        new Rectangle(
                                                minx - TRANSFORM_READ_BUFFER_PIXELS,
                                                miny - TRANSFORM_READ_BUFFER_PIXELS,
                                                (maxx - minx) + TRANSFORM_READ_BUFFER_PIXELS * 2,
                                                (maxy - miny) + TRANSFORM_READ_BUFFER_PIXELS * 2);
                                GridEnvelope2D gridEnvelope = new GridEnvelope2D(rect);
                                readGG =
                                        new GridGeometry2D(
                                                gridEnvelope,
                                                PixelInCell.CELL_CORNER,
                                                atOriginal,
                                                worldEnvelope.getCoordinateReferenceSystem(),
                                                null);
                            }
                        }
```

And now the transformed readGG envelope is incorrect.
ordinates:double[4]@15220
0:-9.140625
1:43.593750
2:5.625000
3:58.359375

