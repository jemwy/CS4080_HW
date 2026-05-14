#include <stdio.h>

#include "common.h"
#include "debug.h"
#include "vm.h"

#include "value.h"
#include "memory.h"

VM vm; 

void initVM() {
  vm.stackCapacity = 8;
  vm.stack = ALLOCATE(Value, vm.stackCapacity);
  resetStack();
}
static void resetStack() {
  vm.stackTop = vm.stack;
}

void freeVM() {
  FREE_ARRAY(Value, vm.stack, vm.stackCapacity);
}

void push(Value value) {
  int stackCount = (int)(vm.stackTop - vm.stack);

  if (stackCount >= vm.stackCapacity) {
    int oldCapacity = vm.stackCapacity;
    vm.stackCapacity = GROW_CAPACITY(oldCapacity);

    vm.stack = GROW_ARRAY(Value, vm.stack,
                          oldCapacity, vm.stackCapacity);

    vm.stackTop = vm.stack + stackCount;
  }

  *vm.stackTop = value;
  vm.stackTop++;
}

Value pop() {
  vm.stackTop--;
  return *vm.stackTop;
}

static InterpretResult run() {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
#define BINARY_OP(op) \
    do { \
      double b = pop(); \
      double a = pop(); \
      push(a op b); \
    } while (false)

  for (;;) {
#ifdef DEBUG_TRACE_EXECUTION
    printf("          ");
    for (Value* slot = vm.stack; slot < vm.stackTop; slot++) {
      printf("[ ");
      printValue(*slot);
      printf(" ]");
    }
    printf("\n");
    disassembleInstruction(vm.chunk,
                           (int)(vm.ip - vm.chunk->code));
#endif

    uint8_t instruction;
    switch (instruction = READ_BYTE()) {
      case OP_CONSTANT: {
        Value constant = READ_CONSTANT();
        push(constant);
        break;
      }
      case OP_ADD:
        vm.stackTop[-2] = vm.stackTop[-2] + vm.stackTop[-1];
        vm.stackTop--;
        break;
      case OP_SUBTRACT:
        vm.stackTop[-2] = vm.stackTop[-2] - vm.stackTop[-1];
        vm.stackTop--;
        break;
      case OP_MULTIPLY:
        vm.stackTop[-2] = vm.stackTop[-2] * vm.stackTop[-1];
        vm.stackTop--;
        break;
      case OP_DIVIDE:
        vm.stackTop[-2] = vm.stackTop[-2] / vm.stackTop[-1];
        vm.stackTop--;
        break;
      case OP_NEGATE:
        vm.stackTop[-1] = -vm.stackTop[-1];
        break;
      case OP_RETURN: {
        printValue(pop());
        printf("\n");
        return INTERPRET_OK;
      }
    }
  }

#undef READ_BYTE
#undef READ_CONSTANT
#undef BINARY_OP
}

InterpretResult interpret(Chunk* chunk) {
  vm.chunk = chunk;
  vm.ip = vm.chunk->code;
  return run();
}