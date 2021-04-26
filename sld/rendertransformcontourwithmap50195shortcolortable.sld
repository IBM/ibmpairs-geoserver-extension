<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xsi:schemaLocation="http://schemas.opengis.net/sld StyledLayerDescriptor.xsd" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <NamedLayer>
        <Name>pairs:pairspluginlayer</Name>
        <UserStyle>
            <Title>Contour Demo</Title>
            <Abstract>Extracts contours layer 50195 dim horizon, value 30</Abstract>
            <FeatureTypeStyle>
                <Name />
                <Rule>
                    <RasterSymbolizer>
                        <Geometry>
                            <PropertyName>grid</PropertyName>
                        </Geometry>
                        <Opacity>1</Opacity>
                        <ColorMap>
                            <ColorMapEntry color="#FFFFFF" label="NO_DATA" opacity="0.0" quantity="-9999" />
                            <ColorMapEntry color="#0033FF" label="240.0" opacity="1.0" quantity="240.0" />
                            <ColorMapEntry color="#0066FF" label="247.22223" opacity="1.0" quantity="247.22223" />
                            <ColorMapEntry color="#0099FF" label="254.44444" opacity="1.0" quantity="254.44444" />
                            <ColorMapEntry color="#00CCFF" label="261.66666" opacity="1.0" quantity="261.66666" />
                            <ColorMapEntry color="#00FFFF" label="268.8889" opacity="1.0" quantity="268.8889" />
                            <ColorMapEntry color="#33FFCC" label="276.1111" opacity="1.0" quantity="276.1111" />
                            <ColorMapEntry color="#66FF99" label="283.33334" opacity="1.0" quantity="283.33334" />
                            <ColorMapEntry color="#99FF66" label="290.55554" opacity="1.0" quantity="290.55554" />
                            <ColorMapEntry color="#CCFF33" label="297.77777" opacity="1.0" quantity="297.77777" />
                            <ColorMapEntry color="#FFFF00" label="305.0" opacity="1.0" quantity="305.0" />
                        </ColorMap>
                    </RasterSymbolizer>
                </Rule>
            </FeatureTypeStyle>
            <FeatureTypeStyle>
                <Transformation>
                    <ogc:Function name="ras:Contour">
                        <ogc:Function name="parameter">
                            <ogc:Literal>data</ogc:Literal>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>levels</ogc:Literal>
                            <ogc:Literal>260</ogc:Literal>
                            <ogc:Literal>270</ogc:Literal>
                            <ogc:Literal>285</ogc:Literal>
                            <ogc:Literal>290</ogc:Literal>
                            <ogc:Literal>295</ogc:Literal>
                            <ogc:Literal>300</ogc:Literal>
                            <ogc:Literal>305</ogc:Literal>
                        </ogc:Function>
                    </ogc:Function>
                </Transformation>
                <Rule>
                    <Name>rule1</Name>
                    <Title>Contour Line</Title>
                    <LineSymbolizer>
                        <Stroke>
                            <CssParameter name="stroke">#000000</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </LineSymbolizer>
                    <TextSymbolizer>
                        <Label>
                            <ogc:PropertyName>value</ogc:PropertyName>
                        </Label>
                        <Font>
                            <CssParameter name="font-family">Arial</CssParameter>
                            <CssParameter name="font-style">Normal</CssParameter>
                            <CssParameter name="font-size">10</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <LinePlacement />
                        </LabelPlacement>
                        <Halo>
                            <Radius>
                                <ogc:Literal>2</ogc:Literal>
                            </Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                                <CssParameter name="fill-opacity">0.6</CssParameter>
                            </Fill>
                        </Halo>
                        <Fill>
                            <CssParameter name="fill">#000000</CssParameter>
                        </Fill>
                        <Priority>2000</Priority>
                        <VendorOption name="followLine">true</VendorOption>
                        <VendorOption name="repeat">100</VendorOption>
                        <VendorOption name="maxDisplacement">50</VendorOption>
                        <VendorOption name="maxAngleDelta">30</VendorOption>
                    </TextSymbolizer>
                </Rule>
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>