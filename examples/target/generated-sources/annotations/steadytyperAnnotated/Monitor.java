package steadytyperAnnotated;

import core.IModelRenderer;
import java.lang.Integer;
import java.lang.Override;

class Monitor implements IModelRenderer<Integer> {
  private MonitorSpec base = new MonitorSpec();

  @Override
  public void render(Integer state) {
    base.render(state);
  }
}
