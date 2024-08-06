package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.exceptions.ParsingException;

import java.nio.file.Files;
import java.nio.file.Path;

public enum FileParser implements Parser<Path> {
  INSTANCE;

  @Override
  public Path parse(String string) {
    Path path;
    try {
      path = Path.of(string).toAbsolutePath();
    } catch (Exception e) {
      throw new ParsingException("'" + string + "' is not a valid path.");
    }
    if (path.getParent() == null) {
      throw new ParsingException("Cannot save file at root.");
    }
    if (!Files.isDirectory(path.getParent())) {
      throw new ParsingException("Directory '" + path.getParent() + "' not found.");
    }
    return path;
  }
}
