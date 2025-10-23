package net.rptools.extra.addon.addon.macro;

public record MacroDetails(
      String name,
      String command,
      String description,
      MacroManager.Scope scope,
      String addOnNamespace,
      String addOnName) {}
