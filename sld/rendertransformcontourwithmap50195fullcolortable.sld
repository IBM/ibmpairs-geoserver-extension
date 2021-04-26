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
                            <ColorMapEntry color="#00009F" label="240.0" opacity="1.0" quantity="240.0" />
                            <ColorMapEntry color="#0000BF" label="242.09677" opacity="1.0" quantity="242.09677" />
                            <ColorMapEntry color="#0000DF" label="244.19354" opacity="1.0" quantity="244.19354" />
                            <ColorMapEntry color="#0000FF" label="246.29033" opacity="1.0" quantity="246.29033" />
                            <ColorMapEntry color="#0020FF" label="248.3871" opacity="1.0" quantity="248.3871" />
                            <ColorMapEntry color="#0040FF" label="250.48387" opacity="1.0" quantity="250.48387" />
                            <ColorMapEntry color="#0060FF" label="252.58064" opacity="1.0" quantity="252.58064" />
                            <ColorMapEntry color="#0080FF" label="254.67741" opacity="1.0" quantity="254.67741" />
                            <ColorMapEntry color="#009FFF" label="256.7742" opacity="1.0" quantity="256.7742" />
                            <ColorMapEntry color="#00BFFF" label="258.87097" opacity="1.0" quantity="258.87097" />
                            <ColorMapEntry color="#00DFFF" label="260.96774" opacity="1.0" quantity="260.96774" />
                            <ColorMapEntry color="#00FFFF" label="263.0645" opacity="1.0" quantity="263.0645" />
                            <ColorMapEntry color="#20FFDF" label="265.1613" opacity="1.0" quantity="265.1613" />
                            <ColorMapEntry color="#40FFBF" label="267.25806" opacity="1.0" quantity="267.25806" />
                            <ColorMapEntry color="#60FF9F" label="269.35483" opacity="1.0" quantity="269.35483" />
                            <ColorMapEntry color="#80FF80" label="271.4516" opacity="1.0" quantity="271.4516" />
                            <ColorMapEntry color="#9FFF60" label="273.5484" opacity="1.0" quantity="273.5484" />
                            <ColorMapEntry color="#BFFF40" label="275.64517" opacity="1.0" quantity="275.64517" />
                            <ColorMapEntry color="#DFFF20" label="277.74194" opacity="1.0" quantity="277.74194" />
                            <ColorMapEntry color="#FFFF00" label="279.8387" opacity="1.0" quantity="279.8387" />
                            <ColorMapEntry color="#FFDF00" label="281.9355" opacity="1.0" quantity="281.9355" />
                            <ColorMapEntry color="#FFBF00" label="284.03226" opacity="1.0" quantity="284.03226" />
                            <ColorMapEntry color="#FF9F00" label="286.12903" opacity="1.0" quantity="286.12903" />
                            <ColorMapEntry color="#FF8000" label="288.2258" opacity="1.0" quantity="288.2258" />
                            <ColorMapEntry color="#FF6000" label="290.32257" opacity="1.0" quantity="290.32257" />
                            <ColorMapEntry color="#FF4000" label="292.41934" opacity="1.0" quantity="292.41934" />
                            <ColorMapEntry color="#FF2000" label="294.5161" opacity="1.0" quantity="294.5161" />
                            <ColorMapEntry color="#FF0000" label="296.6129" opacity="1.0" quantity="296.6129" />
                            <ColorMapEntry color="#DF0000" label="298.7097" opacity="1.0" quantity="298.7097" />
                            <ColorMapEntry color="#BF0000" label="300.80646" opacity="1.0" quantity="300.80646" />
                            <ColorMapEntry color="#9F0000" label="302.90323" opacity="1.0" quantity="302.90323" />
                            <ColorMapEntry color="#800000" label="305.0" opacity="1.0" quantity="305.0" />
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