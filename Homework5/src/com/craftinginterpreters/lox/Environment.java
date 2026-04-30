package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();
  private final List<Object> slots = new ArrayList<>();
  private static final Object UNINITIALIZED = new Object();

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      Object value = values.get(name.lexeme);

      if (value == UNINITIALIZED) {
        throw new RuntimeError(name,
            "Variable '" + name.lexeme + "' is not initialized.");
      }

      return value;
    }

    if (enclosing != null) return enclosing.get(name);

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }

  static Object uninitialized() {
    return UNINITIALIZED;
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value) {
    values.put(name, value);
    slots.add(value);
  }

  Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing; 
    }

    return environment;
  }

  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }

  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }

  Object getAtIndex(int distance, int index) {
    return ancestor(distance).slots.get(index);
  }

  void assignAtIndex(int distance, int index, Object value) {
    ancestor(distance).slots.set(index, value);
  }
}