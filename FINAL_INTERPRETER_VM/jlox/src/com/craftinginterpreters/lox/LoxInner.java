package com.craftinginterpreters.lox;

import java.util.Collections;
import java.util.List;

class LoxInner implements LoxCallable {
  private final LoxInstance instance;
  private final LoxFunction currentMethod;

  LoxInner(LoxInstance instance, LoxFunction currentMethod) {
    this.instance = instance;
    this.currentMethod = currentMethod;
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LoxFunction next = instance.getKlass().findNextMethodDown(
        currentMethod.owner,
        currentMethod.getName());

    if (next == null) return null;

    return next.bind(instance).call(interpreter, Collections.emptyList());
  }

  @Override
  public String toString() {
    return "<inner>";
  }
}