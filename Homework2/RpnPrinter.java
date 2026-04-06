class RpnPrinter implements Expr.Visitor<String> {

  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return print(expr.left) + " " + print(expr.right) + " " + expr.operator.lexeme;
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return print(expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) return "nil";

    if (expr.value instanceof Double) {
      double value = (Double) expr.value;
      if (value == (long) value) {
        return Long.toString((long) value);
      }
    }

    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return print(expr.right) + " " + expr.operator.lexeme;
  }
}