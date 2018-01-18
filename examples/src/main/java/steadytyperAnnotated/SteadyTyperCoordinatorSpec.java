package steadytyperAnnotated;

import processor.coordinator.annotations.CoordinatorSpec;
import processor.coordinator.annotations.CoordinatorSpecGroup;

@CoordinatorSpec(
        groups = {
                @CoordinatorSpecGroup(
                        actionGeneratorSpec = KeyboardSpec.class,
                        stateMapperSpec = CpuSpec.class,
                        modelRendererSpec = MonitorSpec.class
                )
        },
        generatedClassName = "SteadyTyperCoordinator"
)
public class SteadyTyperCoordinatorSpec {}
