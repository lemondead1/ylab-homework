package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.parsing.Parser;
import com.lemondead1.carshopservice.cli.validation.Validator;
import com.lemondead1.carshopservice.exceptions.ParsingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Console;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsoleIOTest {
  private static final Object o1 = new Object();
  private static final Object o2 = new Object();

  @Mock
  Console console;

  @Mock
  Appendable out;

  @Mock
  Parser<Object> parser;

  @Mock
  Validator<Object> validator;

  @InjectMocks
  ConsoleIO cli;

  @Test
  void printlnCallsPrintln() throws IOException {
    cli.println("testString");

    var captor = ArgumentCaptor.forClass(String.class);
    verify(out, atLeastOnce()).append(captor.capture());
    assertThat(String.join("", captor.getAllValues())).isEqualTo("testString\n");
  }

  @Test
  void printfCallsPrintf() throws IOException {
    var args = new Object[] { "Hello" };
    cli.printf("%s world", args);
    verify(out).append("Hello world");
  }

  @Test
  void readInteractiveCallsPrintlnAndReadsLine() throws IOException {
    when(console.readLine()).thenReturn("testString");
    assertThat(cli.readInteractive("Message")).isEqualTo("testString");
    verify(out).append("Message");
  }

  @Test
  void parseRetriesAfterEmptyString() {
    when(console.readLine()).thenReturn("", "", "testString");
    when(parser.parse("testString")).thenReturn(o1);
    assertThat(cli.parse("Message", parser)).isEqualTo(o1);
    verify(console, times(3)).readLine();
    verify(parser).parse("testString");
  }

  @Test
  void parseRetriesAfterException() {
    when(console.readLine()).thenReturn("1", "2", "3");

    when(parser.parse("1")).thenThrow(new ParsingException());
    when(parser.parse("2")).thenReturn(o1);
    when(parser.parse("3")).thenReturn(o2);

    doThrow(new ParsingException()).when(validator).validate(o1);
    doNothing().when(validator).validate(o2);

    assertThat(cli.parse("Message", parser, validator)).isEqualTo(o2);
  }

  @Test
  void parseOptionalStopsOnEmptyString() {
    when(console.readLine()).thenReturn("");
    assertThat(cli.parseOptional("Message", parser)).isEqualTo(Optional.empty());
    verify(console, times(1)).readLine();
  }

  @Test
  void parseOptionalRetriesAfterException() {
    when(console.readLine()).thenReturn("1", "2", "3");

    when(parser.parse("1")).thenThrow(new ParsingException());
    when(parser.parse("2")).thenReturn(o1);
    when(parser.parse("3")).thenReturn(o2);

    doThrow(new ParsingException()).when(validator).validate(o1);
    doNothing().when(validator).validate(o2);

    assertThat(cli.parseOptional("Message", parser, validator)).isEqualTo(Optional.of(o2));
  }
}
