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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsoleCLITest {
  private static final Object o1 = new Object();
  private static final Object o2 = new Object();

  @Mock
  Console console;

  @Mock
  Parser<Object> parser;

  @Mock
  Validator<Object> validator;

  @InjectMocks
  ConsoleCLI cli;

  @Test
  void printlnCallsPrintln() {
    cli.println("testString");

    var captor = ArgumentCaptor.forClass(String.class);
    verify(console, atLeastOnce()).printf(captor.capture());
    assertThat(String.join("", captor.getAllValues())).isEqualTo("testString\n");
  }

  @Test
  void printfCallsPrintf() {
    var args = new Object[] { "Hello" };
    cli.printf("%s world", args);
    verify(console).printf("%s world", "Hello");
  }

  @Test
  void readInteractiveCallsPrintlnAndReadsLine() {
    when(console.readLine("Message")).thenReturn("testString");
    assertThat(cli.readInteractive("Message")).isEqualTo("testString");
    verify(console).readLine("Message");
  }

  @Test
  void parseRetriesAfterEmptyString() {
    when(console.readLine("Message")).thenReturn("", "", "testString");
    when(parser.parse("testString")).thenReturn(o1);
    assertThat(cli.parse("Message", parser)).isEqualTo(o1);
    verify(console, times(3)).readLine("Message");
    verify(parser).parse("testString");
  }

  @Test
  void parseRetriesAfterException() {
    when(console.readLine("Message")).thenReturn("1", "2", "3");

    when(parser.parse("1")).thenThrow(new ParsingException());
    when(parser.parse("2")).thenReturn(o1);
    when(parser.parse("3")).thenReturn(o2);

    doThrow(new ParsingException()).when(validator).validate(o1);
    doNothing().when(validator).validate(o2);

    assertThat(cli.parse("Message", parser, validator)).isEqualTo(o2);
  }

  @Test
  void parseOptionalStopsOnEmptyString() {
    when(console.readLine("Message")).thenReturn("");
    assertThat(cli.parseOptional("Message", parser)).isEqualTo(Optional.empty());
    verify(console, times(1)).readLine("Message");
  }

  @Test
  void parseOptionalRetriesAfterException() {
    when(console.readLine("Message")).thenReturn("1", "2", "3");

    when(parser.parse("1")).thenThrow(new ParsingException());
    when(parser.parse("2")).thenReturn(o1);
    when(parser.parse("3")).thenReturn(o2);

    doThrow(new ParsingException()).when(validator).validate(o1);
    doNothing().when(validator).validate(o2);

    assertThat(cli.parseOptional("Message", parser, validator)).isEqualTo(Optional.of(o2));
  }
}
