package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {
  final String name;
  final LoxClass superclass;
  private final Map<String, LoxFunction> methods;
  private final Map<String, LoxFunction> staticMethods;
  private final Map<String, LoxFunction> setters;

  LoxClass(String name, LoxClass superclass, 
      Map<String, LoxFunction> methods, 
      Map <String, LoxFunction> staticMethods,
      Map <String, LoxFunction> setters) {
    this.superclass = superclass;
    this.name = name;
    this.methods = methods;
    this.staticMethods = staticMethods;
    this.setters = setters;
  }

  LoxFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    if (superclass != null) {
      return superclass.findMethod(name);
    }

    return null;
  }

  LoxFunction findStaticMethod(String name) {
    if (staticMethods.containsKey(name)) {
      return staticMethods.get(name);
    }

    return null;
  }

  LoxFunction findTopMethod(String name) {
    if (superclass != null) {
      LoxFunction method = superclass.findTopMethod(name);
      if (method != null) return method;
    }

    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    return null;
  }

  LoxFunction findNextMethodDown(LoxClass currentOwner, String name) {
    if (this == currentOwner) {
      return null;
    }

    if (superclass != null) {
      LoxFunction method = superclass.findNextMethodDown(currentOwner, name);
      if (method != null) return method;
    }

    if (superclass == currentOwner && methods.containsKey(name)) {
      return methods.get(name);
    }

    return null;
  }

  LoxFunction findSetter(String name) {
    if (setters.containsKey(name)) {
      return setters.get(name);
    }

    if (superclass != null) {
      return superclass.findSetter(name);
    }

    return null;
  }

  Object get(Token name) {
    LoxFunction method = findStaticMethod(name.lexeme);
    if (method != null) return method.bindToClass(this);

    throw new RuntimeError(name,
        "Undefined property '" + name.lexeme + "'.");
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Object call(Interpreter interpreter,
                     List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this);
    LoxFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }

    return instance;
  }

  @Override
  public int arity() {
    LoxFunction initializer = findMethod("init");
    if (initializer == null) return 0;
    return initializer.arity();
  }
}