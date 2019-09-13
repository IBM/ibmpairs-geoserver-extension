## Contacts at geosolutions
[forums](http://osgeo-org.1560.x6.nabble.com/GeoServer-User-f3786390.html)

Ing. Andrea Aime 
GeoSolutions S.A.S. 
Tech lead 

## Current issue for IBMPairs
[store:](http://user:pw@www.ibm.com/?htable=hname&cf=cfn)
debugger null ptr exception in class OpenLayersMapOutputFormat extends AbstractOpenLayersMapOutputFormat
```
private boolean hasOnlyCoverages(WMSMapContent mapContent) {
        for (Layer layer : mapContent.layers()) {
            FeatureType schema = layer.getFeatureSource().getSchema();
            boolean grid =
```            

error: Unable to acquire test coverage and color model for format
[colormodel issue](http://osgeo-org.1560.x6.nabble.com/Unable-to-acquire-a-reader-for-this-coverage-with-format-ImageMosaic-td3789310.html)

## Detailed steps for PairsGeotiffPlugin
( Broad overview parent readme is [here](../../README.MD))
This details coding, operation, build, install of Pairs plugin in geoserver WEB_INF/lib/
Test files:
/Users/bobroff/projects/pairs/geospatial-datasets/normtest.tiff
/Users/bobroff/projects/pairs/geospatial-datasets/merc_ob.tiff
/Users/bobroff/projects/pairs/geospatial-datasets/HYP_LR/HYP_LR.tif
*NOTE* Use above form from root or file:/User/...  DO NOT USEthe url form file://, just the path from root
*TODO* Track this down


## Code path in our extension when new datastore is created
### Geoserver UI Add stores
< Call Stack>
```java
PairsGeoTiffFormat.<init>() (/Users/bobroff/projects/pairs/pairs-code/pairs-geoserver-plugin/plugin/geotiff-hbase/src/main/java/com/ibm/pa/pairs/geoserver/plugin/geotiff/PairsGeoTiffFormat.java:123)
PairsGeoTiffFormatFactory.createFormat() (/Users/bobroff/projects/pairs/pairs-code/pairs-geoserver-plugin/plugin/geotiff-hbase/src/main/java/com/ibm/pa/pairs/geoserver/plugin/geotiff/PairsGeoTiffFormatFactory.java:96)
GridFormatFinder.getFormatArray() (/gt-coverage-20.0.jar/org.geotools.coverage.grid.io/GridFormatFinder.class:119)
NewDataPage.getAvailableCoverageStores() (Unknown Source:202)
NewDataPage.<init>() (Unknown Source:52)
NativeConstructorAccessorImpl.newInstance0(Constructor,Object[])[native method] (Unknown Source:-1)
NativeConstructorAccessorImpl.newInstance(Object[]) (Unknown Source:62)
DelegatingConstructorAccessorImpl.newInstance(Object[]) (Unknown Source:45)
Constructor.newInstance(Object[]) (/rt.jar/java.lang.reflect/Constructor.class:423)
DefaultPageFactory.newPage(Constructor,PageParameters) (Unknown Source:175)
DefaultPageFactory.newPage(Class) (Unknown Source:67)
DefaultMapperContext.newPageInstance(Class,PageParameters) (Unknown Source:102)
PageProvider.resolvePageInstance(Integer,Class,PageParameters,Integer) (Unknown Source:271)
PageProvider.getPageInstance() (Unknown Source:169)
PageRenderer.getPage() (Unknown Source:78)
WebPageRenderer.isPageStateless() (Unknown Source:287)
WebPageRenderer.shouldRenderPageAndWriteResponse(RequestCycle,Url,Url) (Unknown Source:329)
WebPageRenderer.respond(RequestCycle) (Unknown Source:193)
RenderPageRequestHandler.respond(IRequestCycle) (Unknown Source:175)
RequestCycle$HandlerExecutor.respond(IRequestHandler) (Unknown Source:895)
```

PairsGeoTiffFormat constructor
- Fill in minfo var from parent class AbstractGridFormat
  - This sets the display name
- Fill in readParameters (maybe here we have to set our own reader?)

### UI list of stores appears, select PairsGeotiff
- We are now in create popup, but note the 'browse' is not available so something is missing
- input datastore name and file:///Users/bobroff/projects/pairs/geospatial-datasets/normtest.tiff
  - if name doesn't correspond to a file
    - Error box appears telling us so
    - The plugin is not hit yet
- Now 'save' and we are hit in getReader()
```java
PairsGeoTiffFormat.getReader(Object,Hints) (/Users/bobroff/projects/pairs/pairs-code/pairs-geoserver-plugin/plugin/geotiff-hbase/src/main/java/com/ibm/pa/pairs/geoserver/plugin/geotiff/PairsGeoTiffFormat.java:294)
PairsGeoTiffFormat.getReader(Object,Hints) (/Users/bobroff/projects/pairs/pairs-code/pairs-geoserver-plugin/plugin/geotiff-hbase/src/main/java/com/ibm/pa/pairs/geoserver/plugin/geotiff/PairsGeoTiffFormat.java:76)
ResourcePool.getGridCoverageReader(CoverageStoreInfo,CoverageInfo,String,Hints) (Unknown Source:1528)
ResourcePool.getGridCoverageReader(CoverageStoreInfo,String,Hints) (Unknown Source:1474)
CoverageStoreInfoImpl.getGridCoverageReader(ProgressListener,Hints) (Unknown Source:53)
NativeMethodAccessorImpl.invoke0(Method,Object,Object[])[native method] (Unknown Source:-1)
NativeMethodAccessorImpl.invoke(Object,Object[]) (Unknown Source:62)
DelegatingMethodAccessorImpl.invoke(Object,Object[]) (Unknown Source:43)
Method.invoke(Object,Object[]) (/rt.jar/java.lang.reflect/Method.class:498)
ModificationProxy.invoke(Object,Method,Object[]) (Unknown Source:128)
$Proxy25.getGridCoverageReader(ProgressListener,Hints) (Unknown Source:-1)
NewLayerPageProvider.getItemsInternal() (Unknown Source:100)
NewLayerPageProvider.getItems() (Unknown Source:61)
GeoServerDataProvider.getFilteredItems() (Unknown Source:202)
NewLayerPageProvider.getFilteredItems() (Unknown Source:200)
GeoServerDataProvider.size() (Unknown Source:220)
GeoServerTablePanel$PagerDelegate.updateMatched() (Unknown Source:597)
GeoServerTablePanel$PagerDelegate.<init>(GeoServerTablePanel) (Unknown Source:592)
GeoServerTablePanel.<init>(String,GeoServerDataProvider,boolean) (Unknown Source:182)
GeoServerTablePanel.<init>(String,GeoServerDataProvider) (Unknown Source:96)
```

- Source is a file
((File) source).getAbsolutePath()
"/Users/bobroff/projects/pairs/geospatial-datasets/normtest.tiff" (id=943)

- return new PairsGeoTiffReaderBuildCoverage(source, hints);
  - Constructor needs the (image)inputStreams set up for the source
        // /////////////////////////////////////////////////////////////////////
            //
            // Get a stream in order to read from it for getting the basic
            // information for this coverage
            //
            // /////////////////////////////////////////////////////////////////////
            if ((source instanceof InputStream) || (source instanceof ImageInputStream))
            .
            .
            .
- Since the datastore source is a file this code looks for a registered extender of ImageInputStreamSPI (service provider interface) that has a class type (e.g File) that handles this input type (File)

```java
inStreamSPI = ImageIOExt.getImageInputStreamSPI(source);
                if (inStreamSPI == null)
                    throw new IllegalArgumentException("No input stream for the provided source");
                inStream =
                        inStreamSPI.createInputStreamInstance(
                                source, ImageIO.getUseCache(), ImageIO.getCacheDirectory());
```

- ImageIOExt uses IIORegistry so we could add a custom ImageInputStreamSPi class there 
 
 ```java
 // Ensure category is present
        try {
            iter =
                    IIORegistry.getDefaultInstance()
                            .getServiceProviders(ImageInputStreamSpi.class, true);
        } catch (IllegalArgumentException e) {
            return null;
        }

        boolean usecache = ImageIO.getUseCache();

        ImageInputStreamSpi spi = null;
        while (iter.hasNext()) {
            spi = iter.next();
            if (spi.getInputClass().isInstance(input)) 
```

- Here if verifies that the Spi which is a *FileImageInputStreamExtImplSpi* can handle a File.class input

- Then verifies that an ImageInputStream can be created from that spi. Note the test stream is not used

```java
try {
                        stream =
                                spi.createInputStreamInstance(
                                        input, usecache, ImageIO.getCacheDirectory());
```

- (We could add our own provider class for inStreamSPI and inStream, but I don't think we need to)
If we do, I believe we make a new inStreamSPI and give it the name of our custom reader class then
call the IIORegistry or service registry to add it. I don't think META-INF/interface name technique works

- The stream reader returned here from spi.createInputStreamInstance is:

```java
public  class FileImageInputStreamExtImpl extends ImageInputStreamImpl
        implements FileImageInputStreamExt {
```

- *** The file source is verified that it can be read and decoded. This is very important. ***
I see the breakpoints in public  class FileImageInputStreamExtImpl extends ImageInputStreamImpl
        implements FileImageInputStreamExt { 
being hit in the read method and looking back on the stack it checks if file is JPEG etc by reading a few bites

The caller is javax.image.ImageIO.canDecode(). It also looks for an overview file *.ovr

This is the stack:
```
AsciiGridRaster.getKey(ImageInputStream,int,int,byte) (Unknown Source:1202)
EsriAsciiGridRaster.parseHeader() (Unknown Source:152)
AsciiGridsImageReaderSpi.canDecodeInput(Object) (Unknown Source:217)
ImageIO$CanDecodeInputFilter.filter(Object) (/rt.jar/javax.imageio/ImageIO.class:567)
FilterIterator.advance() (/rt.jar/javax.imageio.spi/ServiceRegistry.class:834)
FilterIterator.next() (/rt.jar/javax.imageio.spi/ServiceRegistry.class:852)
ImageIO$ImageReaderIterator.next() (/rt.jar/javax.imageio/ImageIO.class:528)
ImageIO$ImageReaderIterator.next() (/rt.jar/javax.imageio/ImageIO.class:513)
ImageIOExt.getImageioReader(ImageInputStream) (/gt-coverage-20.0.jar/org.geotools.image.io/ImageIOExt.class:299)
MaskOverviewProvider.getReaderSpiFromStream(ImageReaderSpi,ImageInputStream) (/gt-coverage-20.0.jar/org.geotools.coverage.grid.io.imageio/MaskOverviewProvider.class:653)
MaskOverviewProvider$SpiHelper.<init>(URL,ImageReaderSpi,ImageInputStreamSpi) (/gt-coverage-20.0.jar/org.geotools.coverage.grid.io.imageio/MaskOverviewProvider.class:753)
MaskOverviewProvider$SpiHelper.<init>(URL,ImageReaderSpi) (/gt-coverage-20.0.jar/org.geotools.coverage.grid.io.imageio/MaskOverviewProvider.class:732)
MaskOverviewProvider.<init>(DatasetLayout,File,ImageReaderSpi) (/gt-coverage-20.0.jar/org.geotools.coverage.grid.io.imageio/MaskOverviewProvider.class:103)
MaskOverviewProvider.<init>(DatasetLayout,File) (/gt-coverage-20.0.jar/org.geotools.coverage.grid.io.imageio/MaskOverviewProvider.class:98)
PairsGeoTiffReaderBuildCoverage.getHRInfo(Hints) (/Users/bobroff/projects/pairs/pairs-code/pairs-geoserver-plugin/plugin/geotiff-hbase/src/main/java/com/ibm/pa/pairs/geoserver/plugin/geotiff/PairsGeoTiffReaderBuildCoverage.java:363)
PairsGeoTiffReaderBuildCoverage.<init>(Object,Hints) (/Users/bobroff/projects/pairs/pairs-code/pairs-geoserver-plugin/plugin/geotiff-hbase/src/main/java/com/ibm/pa/pairs/geoserver/plugin/geotiff/PairsGeoTiffReaderBuildCoverage.java:256)
PairsGeoTiffFormat.getReader(Object,Hints) (/Users/bobroff/projects/pairs/pairs-code/pairs-geoserver-plugin/plugin/geotiff-hbase/src/main/java/com/ibm/pa/pairs/geoserver/plugin/geotiff/PairsGeoTiffFormat.java:307)
PairsGeoTiffFormat.getReader(Object,Hints) (/Users/bobroff/projects/pairs/pairs-code/pairs-geoserver-plugin/plugin/geotiff-hbase/src/main/java/com/ibm/pa/pairs/geoserver/plugin/geotiff/PairsGeoTiffFormat.java:76)
ResourcePool.getGridCoverageReader(CoverageStoreInfo,CoverageInfo,String,Hints) (Unknown Source:1528)
ResourcePool.getGridCoverageReader(CoverageStoreInfo,String,Hints) (Unknown Source:1474)
```


- Now back in our ParisGeoTiffReaderBuildCoverageStore we can create the streamreader again
                inStream =
                        inStreamSPI.createInputStreamInstance(
                                source, ImageIO.getUseCache(), ImageIO.getCacheDirectory());



### Once this is done, the PairsG..ReaderBuildCoverage is constructed and returned




### Now we publish a new layer from this store

- GeotiffFormatFactory invoked isAvailable()
- """ createFormat()
- The publish completes, but what is odd is the FileImageInputStream  read breakpoints not hit 

### Layer preview
Now we hit in PairsGeoTiffReaderBuildCoverage.read()
Input parameters

```java
params[0].toString()
"InputTransparentColor = null\n" (id=7080)
params[1].toString()
"SUGGESTED_TILE_SIZE = "512,512"\n" (id=7082)
params[2].toString()
"Interpolation = "javax.media.jai.InterpolationNearest@550a0160"\n" (id=7084)
params[3].toString()
"BackgroundColor = "java.awt.Color[r=255,g=255,b=255]"\n" (id=7087)
params[4].toString()
"ReadGridGeometry2D = "GridGeometry2D[GridEnvelope2D[0..767, 0..383], PARAM_MT["Affine", \n     


requestedEnvelope.toString()
"GeneralEnvelope[(-270.0, -135.0), (270.0, 135.0)]" (id=7289)
layout.toString()
"ImageLayout[TILE_GRID_X_OFFSET=0, TILE_GRID_Y_OFFSET=0, TILE_HEIGHT=512]" (id=7339)
suggestedTileSize[0]
512
suggestedTileSize[1]
512
```

## Typical request URL
http://localhost:8080/geoserver/pairs/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetFeatureInfo&FORMAT=image%2Fjpeg&TRANSPARENT=true&QUERY_LAYERS=pairs%3AHYP_LR&LAYERS=pairs%3AHYP_LR&exceptions=application%2Fvnd.ogc.se_inimage&INFO_FORMAT=text%2Fhtml&FEATURE_COUNT=50&X=50&Y=50&SRS=EPSG%3A4326&STYLES=&WIDTH=101&HEIGHT=101&BBOX=9.77783203125%2C-32.54150390625%2C80.79345703125%2C38.47412109375
