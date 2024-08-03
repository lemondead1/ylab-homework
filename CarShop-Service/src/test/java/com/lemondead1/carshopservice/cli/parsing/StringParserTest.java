package com.lemondead1.carshopservice.cli.parsing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class StringParserTest {
  @ParameterizedTest
  @ValueSource(strings = {
      "ag-84t89g  gfdg hugs892gdf34",
      "  #$@&H^T*@THUJOJ*(@#g fdg  d",
      "adu78hunja a78g74 37th^&Y3Y&T#p9 H#&*o "
  })
  void stringParserPassesThroughString(String test) {
    assertThat(StringParser.INSTANCE.parse(test)).isEqualTo(test);
  }
}
