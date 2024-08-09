package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.cli.ConsoleIO;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class MockConsoleIO extends ConsoleIO {
  private final List<Item> expectedHistory = new ArrayList<>();
  private final List<String> inputs = new ArrayList<>();
  private int currentInputIndex;
  private final List<Item> actualHistory = new ArrayList<>();

  public MockConsoleIO() {
    super(null);
  }

  public void printf(String pattern, Object... args) {
    var string = String.format(pattern, args);
    if (!actualHistory.isEmpty() && !actualHistory.get(actualHistory.size() - 1).input) {
      actualHistory.set(actualHistory.size() - 1,
                        new Item(actualHistory.get(actualHistory.size() - 1).value + string, false));
    } else {
      actualHistory.add(new Item(string, false));
    }
  }

  public String readInteractive(String message) {
    if (currentInputIndex >= inputs.size()) {
      throw new NoSuchElementException(
          "Attempted to read with message '" + message + "' but there was no element ot return.");
    }
    printf(message);
    actualHistory.add(new Item(inputs.get(currentInputIndex), true));
    return inputs.get(currentInputIndex++);
  }

  @Override
  public String readPassword(String message) {
    return readInteractive(message);
  }

  public MockConsoleIO in(String input) {
    expectedHistory.add(new Item(input, true));
    inputs.add(input);
    return this;
  }

  public MockConsoleIO out(String output) {
    if (!expectedHistory.isEmpty() && !expectedHistory.get(expectedHistory.size() - 1).input) {
      expectedHistory.set(expectedHistory.size() - 1,
                          new Item(expectedHistory.get(expectedHistory.size() - 1).value + output, false));
    } else {
      expectedHistory.add(new Item(output, false));
    }
    return this;
  }

  public void assertMatchesHistory() {
    assertThat(actualHistory).isEqualTo(expectedHistory);
  }

  record Item(String value, boolean input) { }
}
