package steadytyperAnnotated;

import core.IStateMapper;
import java.lang.Integer;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;

class Cpu implements IStateMapper<String, Integer> {
  private CpuSpec base = new CpuSpec();

  @Override
  public Integer mapActionToState(String action) {
    if (action.equals("A")) {
      return base.handleKeyA();
    }
    if (action.equals("B")) {
      return base.handleKeyB();
    }
    throw new RuntimeException("Unhandled action type");
  }
}
