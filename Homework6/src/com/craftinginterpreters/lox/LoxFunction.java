package com.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure;
  
  private final boolean isInitializer;
  final boolean isGetter;
  final boolean isSetter;

  final LoxClass owner;

  LoxFunction(Stmt.Function declaration, Environment closure,
            boolean isInitializer, LoxClass owner) {
    this.isInitializer = isInitializer;
    this.closure = closure;
    this.declaration = declaration;
    this.isGetter = declaration.isGetter;
    this.owner = owner;
    this.isSetter = declaration.isSetter;
  }

  LoxFunction(Expr.Function declaration, Environment closure) {
    this.closure = closure;
    this.declaration = new Stmt.Function(
        null,
        declaration.params,
        declaration.body,
        false,
        false,
        false
    );
    this.isInitializer = false;
    this.isGetter = false;
    this.owner = null;
    this.isSetter = false;
  }

  LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);

    environment.define("this", instance);
    environment.define("inner", new LoxInner(instance, this));
    return new LoxFunction(declaration, environment,isInitializer, owner);
  }

  LoxFunction bindToClass(LoxClass klass) {
    Environment environment = new Environment(closure);
    environment.define("this", klass);
    return new LoxFunction(declaration, environment, isInitializer, owner);
  }

  @Override
  public String toString() {
    if (declaration.name == null) return "<fn>";
    return "<fn " + declaration.name.lexeme + ">";
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public Object call(Interpreter interpreter,
                     List<Object> arguments) {
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme,
          arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      if (isInitializer) return closure.getAt(0, "this");

      return returnValue.value;
    }

    if (isInitializer) return closure.getAt(0, "this");
    return null;
  }

  String getName() {
    return declaration.name.lexeme;
  }
}