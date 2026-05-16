package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
  private LoxClass klass;
  private final Map<String, Object> fields = new HashMap<>();

  LoxInstance(LoxClass klass) {
    this.klass = klass;
  }

  Object get(Token name, Interpreter interpreter) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    LoxFunction method = klass.findTopMethod(name.lexeme);

    if (method != null && method.isGetter) {
      return method.bind(this).call(interpreter, java.util.Collections.emptyList());
    }

    if (method != null) {
      return method.bind(this);
    }

    throw new RuntimeError(name, 
        "Undefined property '" + name.lexeme + "'.");
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return klass.name + " instance";
  }

  LoxClass getKlass() {
    return klass;
  }

  LoxFunction findSetter(String name) {
    return klass.findSetter(name);
  }
}