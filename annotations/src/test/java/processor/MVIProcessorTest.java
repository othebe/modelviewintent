//package processor;
//
//import processor.actiongenerator.annotations.ActionGeneratorSpec;
//import processor.coordinator.annotations.CoordinatorSpec;
//import processor.coordinator.annotations.CoordinatorSpecGroup;
//import processor.modelrenderer.annotations.ModelRendererSpec;
//import processor.statemapper.annotations.ActionHandler;
//import processor.statemapper.annotations.StateMapperSpec;
//
//@ActionGeneratorSpec(
//        actions = { MVIProcessorTest.HELLO, MVIProcessorTest.BUENO },
//        generatedClassName = "MVIActionGenerator"
//)
//@StateMapperSpec(
//        modelClass = String.class,
//        generatedClassName = "MVIStateMapper"
//)
//@ModelRendererSpec(
//        modelClass = Integer.class,
//        generatedClassName = "MVIModelRenderer"
//)
//@CoordinatorSpec(
//        groups = {
//                @CoordinatorSpecGroup(
//                        actionGeneratorSpec = MVIProcessorTest.class,
//                        stateMapperSpec = MVIProcessorTest.class,
//                        modelRendererSpec = MVIProcessorTest.class
//                )
//        },
//        generatedClassName = "MVICoordinator"
//)
//public class MVIProcessorTest {
//    public final static String HELLO = "hello";
//    public final static String BUENO = "BUENO";
//
//    @ActionHandler(HELLO)
//    String handleHello() {
//        return "HELLO!!!";
//    }
//
//    @ActionHandler(BUENO)
//    String handleBueno() {
//        return "BUENO!!!";
//    }
//}
