package dev.navids.soottutorial.basicapi;

import dev.navids.soottutorial.visual.Visualizer;
import jas.jasError;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.scalar.Pair;
import java.io.File;
import java.util.*;

public class BasicAPI {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "BasicAPI";
    public static String circleClassName = "Circle";

    public static void setupSoot() {
        G.reset();
// Uncomment line below to import essential Java classes
//        Options.v().set_prepend_classpath(true);
// Comment the line below to not have phantom refs (you need to uncomment the line above)
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(sourceDirectory);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        Options.v().set_whole_program(true);
        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();
    }

    public static void main(String[] args) {
        setupSoot();
        // Access to Classes
        SootClass circleClass = reportSootClassInfo();
        // Access to Fields
        SootField radiusField = reportSootFieldInfo(circleClass);
        // Access to Methods
        SootMethod areaMethod = reportSootMethodInfo(circleClass);
        // Access to Body (units, locals)
        System.out.println("-----Body-----");
        JimpleBody body = (JimpleBody) areaMethod.getActiveBody();
        reportLocalInfo(body);
        Stmt firstNonIdentitiyStmt = body.getFirstNonIdentityStmt();
        int c = 0;
        for (Unit u : body.getUnits()) {
            c++;
            Stmt stmt = (Stmt) u;
            System.out.println(String.format("(%d): %s", c, stmt ));
            if(stmt.equals(firstNonIdentitiyStmt))
                System.out.println("    This statement is the first non-identity statement!");
            if(stmt.containsFieldRef())
                reportFieldRefInfo(radiusField, stmt);
            if(doesInvokeMethod(stmt, "int area()", circleClassName)){
                System.out.println("    This statement invokes 'int area()' method");
            }
            modifyBody(body, stmt);
        }
        for(Trap trap : body.getTraps()){
            System.out.println(trap);
        }
        try {
            body.validate();
            System.out.println("Body is validated! No inconsistency found.");
        }
        catch (Exception exception){
            System.out.println("Body is not validated!");
        }
        // Call graph
        System.out.println("-----CallGraph-----");
        CallGraph callGraph = Scene.v().getCallGraph();
        for(Iterator<Edge> it = callGraph.edgesOutOf(areaMethod); it.hasNext(); ){
            Edge edge = it.next();
            System.out.println(String.format("Method '%s' invokes method '%s' through stmt '%s", edge.src(), edge.tgt(), edge.srcUnit()));
        }
        boolean drawGraph = false;
        explore_call_paths();
        if (args.length > 0 && args[0].equals("draw"))
            drawGraph = true;
        if (drawGraph) {
//            Visualizer.v().addCallGraph(callGraph);
            Visualizer.v().addCallGraph(callGraph, edge -> edge.src().getDeclaringClass().isApplicationClass(),
                sootMethod -> new Pair<>(sootMethod.getDeclaringClass().isApplicationClass() ? "cg_node, default_color" : "cg_node, cg_lib_class"
                , sootMethod.getDeclaringClass().isApplicationClass() ? sootMethod.getSubSignature() : sootMethod.getSignature()));
            Visualizer.v().draw();
        }
    }

    private static void reportLocalInfo(JimpleBody body) {
        System.out.println(String.format("Local variables count: %d", body.getLocalCount()));
        Local thisLocal = body.getThisLocal();
        Type thisType = thisLocal.getType();
        Local paramLocal = body.getParameterLocal(0);
    }

    public static void display_call_paths_gnrtd_by_expltn(){
        List<Edge> path;
        for(int i = 0; i < call_paths.size(); i++){
            path = call_paths.get(i);
            System.out.println("Path " + i);
            // System.out.println(String.format("method '%s'", path.get(i)));
            /*for(int j = 0; j < path.size(); j++){
                System.out.println(String.format("method '%s'", path.get(j).src()));
            }*/
        }
    }

    private static SootMethod reportSootMethodInfo(SootClass circleClass) {
        System.out.println("-----Method-----");
        System.out.println(String.format("List of %s's methods:", circleClass.getName()));
        for(SootMethod sootMethod : circleClass.getMethods())
            System.out.println(String.format("- %s",sootMethod.getName()));
        SootMethod getCircleCountMethod = circleClass.getMethod("int getCircleCount()");
        System.out.println(String.format("Method Signature: %s", getCircleCountMethod.getSignature()));
        System.out.println(String.format("Method Subsignature: %s", getCircleCountMethod.getSubSignature()));
        System.out.println(String.format("Method Name: %s", getCircleCountMethod.getName()));
        System.out.println(String.format("Declaring class: %s", getCircleCountMethod.getDeclaringClass()));
        int methodModifers = getCircleCountMethod.getModifiers();
        System.out.println(String.format("Method %s is public: %b, is static: %b, is final: %b", getCircleCountMethod.getName(), Modifier.isPublic(methodModifers), Modifier.isStatic(methodModifers), Modifier.isFinal(methodModifers)));
        SootMethod constructorMethod = circleClass.getMethodByName("<init>");
        try{
            SootMethod areaMethod = circleClass.getMethodByName("area");
        }
        catch (Exception exception){
            System.out.println("Th method 'area' is overloaded and Soot cannot retrieve it by name");
        }
        return circleClass.getMethod("int area(boolean)");
    }

    public static void display_call_paths_gnrtd_by_explrtn(){
        List<Edge> path;
        for(int i = 0; i < call_paths.size(); i++){
            path = call_paths.get(i);
            System.out.print("Path " + i + " ");
            // System.out.println(String.format("method '%s'", path.get(i)));
            /*for(int j = 0; j < path.size(); j++){
                System.out.println(String.format("method '%s'", path.get(j).src()));
            }*/
        }
    }

    private static SootField reportSootFieldInfo(SootClass circleClass) {
        SootField radiusField = circleClass.getField("radius", IntType.v());
        SootField piField = circleClass.getField("double PI");
        System.out.println(String.format("Field %s is final: %b", piField, piField.isFinal()));
        return radiusField;
    }

    private static SootClass reportSootClassInfo() {
        System.out.println("-----Class-----");
        SootClass circleClass = Scene.v().getSootClass(circleClassName);
        System.out.println(String.format("The class %s is an %s class, loaded with %d methods! ",
                circleClass.getName(), circleClass.isApplicationClass() ? "Application" : "Library", circleClass.getMethodCount()));
        String wrongClassName = "Circrle";
        SootClass notExistedClass = Scene.v().getSootClassUnsafe(wrongClassName, false);
        System.out.println(String.format("getClassUnsafe: Is the class %s null? %b", wrongClassName, notExistedClass==null));
        try{
            notExistedClass = Scene.v().getSootClass(wrongClassName);
            System.out.println(String.format("getClass creates a phantom class for %s: %b", wrongClassName, notExistedClass.isPhantom()));
        }catch (Exception exception){
            System.out.println(String.format("getClass throws an exception for class %s.", wrongClassName));
        }
        Type circleType = circleClass.getType();
        System.out.println(String.format("Class '%s' is same as class of type '%s': %b", circleClassName, circleType.toString(), circleClass.equals(Scene.v().getSootClass(circleType.toString()))));
        return circleClass;
    }

    private static void modifyBody(JimpleBody body, Stmt stmt) {
        stmt.apply(new AbstractStmtSwitch() {
            @Override
            public void caseIfStmt(IfStmt stmt) {
                System.out.println(String.format("    (Before change) if condition '%s' is true goes to stmt '%s'", stmt.getCondition(), stmt.getTarget()));
                stmt.setTarget(body.getUnits().getSuccOf(stmt));
                System.out.println(String.format("    (After change) if condition '%s' is true goes to stmt '%s'", stmt.getCondition(), stmt.getTarget()));
            }
        });
    }

    private static boolean doesInvokeMethod(Stmt stmt, String subsignature, String declaringClass) {
        if(!stmt.containsInvokeExpr())
            return false;
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
        invokeExpr.apply(new AbstractJimpleValueSwitch() {
            @Override
            public void caseStaticInvokeExpr(StaticInvokeExpr v) {
                System.out.println(String.format("    StaticInvokeExpr '%s' from class '%s'", v, v.getType()));
            }

            @Override
            public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
                System.out.println(String.format("    VirtualInvokeExpr '%s' from local '%s' with type %s", v, v.getBase(), v.getBase().getType()));
            }

            @Override
            public void defaultCase(Object v) {
                super.defaultCase(v);
            }
        });
        return invokeExpr.getMethod().getSubSignature().equals(subsignature) && invokeExpr.getMethod().getDeclaringClass().getName().equals(declaringClass);
    }

    private static void reportFieldRefInfo(SootField radiusField, Stmt stmt) {
        FieldRef fieldRef = stmt.getFieldRef();
        fieldRef.apply(new AbstractRefSwitch() {
            @Override
            public void caseStaticFieldRef(StaticFieldRef v) {
                // A static field reference
            }

            @Override
            public void caseInstanceFieldRef(InstanceFieldRef v) {
                if(v.getField().equals(radiusField)){
                    System.out.println(String.format("    Field %s is used through FieldRef '%s'. The base local of FieldRef has type '%s'", radiusField, v, v.getBase().getType()));
                }
            }
        });
    }
    
    //START
    static ArrayList<List<Edge>> call_paths;
    static HashMap<Integer, Integer> method_table;
    static ArrayList<Edge> call_sequence;

    static int LOOP_LIMIT = 1;

    public static ArrayList<Edge> generate_path (HashMap<SootMethod, Edge> out_edge, SootMethod start_method, int length){
        ArrayList<Edge> path = new ArrayList<>();
        SootMethod current = start_method;
        while(length > 0){
            Edge call_edge = out_edge.get(current);
            path.add(call_edge);
            current = call_edge.tgt();
            length--;
        }
        return path;
    }

    public ArrayList<Edge> shortest_path_to_default_path(SootMethod new_method, ArrayList<Edge> call_sequence){
        if(call_sequence.isEmpty()){
            return null;
        }
        CallGraph call_graph = Scene.v().getCallGraph();
        HashSet<SootMethod> originally_called_methods = new HashSet<>();
        originally_called_methods.add(call_sequence.get(0).src());
        for(Edge _edge : call_sequence){
            originally_called_methods.add(_edge.tgt());
        }

        SootMethod start_method = new SootMethod(circleClassName, null, null);
        for(Iterator<MethodOrMethodContext> src_method_iterator = call_graph.sourceMethods(); src_method_iterator.hasNext();){
            start_method = src_method_iterator.next().method();
            if(start_method.isEntryMethod()){
                break;
            }
        }

        int current_distance = 0;
        HashMap<SootMethod, Edge> out_edge = new HashMap<>(); //stores the edge that calls each method
        HashMap<SootMethod, Integer> distance = new HashMap<>();
        Queue<SootMethod> frontier = new LinkedList<>();

        frontier.add(start_method);
        distance.put(start_method, 0);
        while(!frontier.isEmpty()){
            SootMethod current_method = frontier.poll();
            current_distance = distance.get(current_method);
            for(Iterator<Edge> call_iterator = call_graph.edgesInto(current_method);call_iterator.hasNext();){
                Edge call_edge = call_iterator.next();
                SootMethod caller_method = call_edge.src();
                if(!out_edge.containsKey(caller_method)){
                    distance.put(caller_method, current_distance+1);
                    out_edge.put(caller_method, call_edge);
                    if(originally_called_methods.contains(caller_method)){ //arrived on the original call path
                        return generate_path(out_edge, caller_method, current_distance+1);
                    }
                    frontier.add(caller_method);
                }
            }
        }
        return null;
    }

    public static void print_list(ArrayList<Edge> complete_sequence){
        if(complete_sequence.isEmpty()) 
            return;
        System.out.print(complete_sequence.get(0).src() + " " + complete_sequence.get(0).src().equivHashCode() + ", ");
        for(int j = 0; j < complete_sequence.size(); j++)
            System.out.print(complete_sequence.get(j).tgt() + " " + complete_sequence.get(j).tgt().equivHashCode() + ", ");
    }

    public static void call_next_method(CallGraph callgraph, SootMethod current_method){        
        if(current_method.getName().compareTo("registerNatives") == 0)
            return;
        Iterator<Edge> call_iterator = callgraph.edgesOutOf(current_method);
        Iterator<Edge> call_it = callgraph.edgesOutOf(current_method);

        if(!call_iterator.hasNext()){ //method calls nothing else, so its the end of the call path
            //System.out.println("ending function: " + current_method);
            ArrayList<Edge> complete_sequence = new ArrayList<>();
            complete_sequence.addAll(call_sequence);
            call_paths.add(complete_sequence);
            //System.out.println("ending function: " + current_method + " found new complete path\n");
            print_list(complete_sequence);
            System.out.println("Ending Function: " + current_method);
            System.out.println("\n\n\n");
            return;
        }
        int hash_code;

        ArrayList<Edge> new_calls = new ArrayList<>();
        ArrayList<Edge> recalls = new ArrayList<>();
        int baga = 0;

        System.out.println("----------------------------------");
        while(call_it.hasNext()){
            Edge edge = call_it.next();
            SootMethod curr_method = edge.src();
            SootMethod next_method = edge.tgt();
            // hash_code = next_method.equivHashCode();
            System.out.println(baga + " ====  CURR:  " + curr_method.getName() + "  NEXT:  " + next_method.getName() + "  " + edge + " ====");
            baga++;
        }
        System.out.println("----------------------------------");

        baga = 0;

        while(call_iterator.hasNext()){
            Edge edge = call_iterator.next();
            SootMethod next_method = edge.tgt();
            hash_code = next_method.equivHashCode();
            if(!method_table.containsKey(hash_code)){
                System.out.print(baga + " [call_sequence] ------");
                print_list(call_sequence);
                System.out.println(" >>>>" + next_method.getName() + " does not exists in list");
                new_calls.add(edge);
            }
            else{
                System.out.print(baga + " [call_sequence] +++++++");
                print_list(call_sequence);
                System.out.println(" >>>>" + next_method.getName() + " already exists in list");
                recalls.add(edge);
            }
            baga++;
        }

        
        int occurrence_location;
        //first process the cyclic calls
        for(Edge edge_to_called_method : recalls){
            occurrence_location = method_table.get(edge_to_called_method.tgt().equivHashCode());
            ArrayList<Edge> repeat_sequence = new ArrayList<>();
            for(int i = occurrence_location; i < call_sequence.size(); i++){
                repeat_sequence.add(call_sequence.get(i));
            }
            //save the list
            ArrayList<Edge> original_sequence = new ArrayList<>();
            original_sequence.addAll(call_sequence);
            //generate paths that involve goind around the funcion call cycle 1 - repeat_instance times
            for(int i = 0; i < LOOP_LIMIT; i++){
                //go through the loop one more time
                call_sequence.addAll(repeat_sequence);
                if(new_calls.isEmpty()){
                    ArrayList<Edge> complete_sequence = new ArrayList<>();
                    complete_sequence.addAll(call_sequence);
                    call_paths.add(complete_sequence);
                    //System.out.println("ending function: " + current_method + " found new complete path\n");
                    
                    System.out.println("ending function >>>>>>>>: " + current_method);
                    System.out.println("\n\n\n");
                }
                else{
                    for(Edge edge_to_new_method : new_calls){
                        SootMethod new_method = edge_to_new_method.tgt();
                        hash_code = new_method.equivHashCode();
                        method_table.put(hash_code, call_sequence.size());
                        call_sequence.add(edge_to_new_method);
                        call_next_method(callgraph, new_method);
                        method_table.remove(hash_code);
                        call_sequence.remove(edge_to_new_method);
                    }
                }
            }
            //reset to the list before the looping around
            call_sequence = new ArrayList<>();
            call_sequence.addAll(original_sequence);
            call_sequence = original_sequence;
        }
        //then process new calls
        for(Edge edge_to_new_method : new_calls){
            SootMethod new_method = edge_to_new_method.tgt();
            hash_code = new_method.equivHashCode();
            method_table.put(hash_code, call_sequence.size()); //value is the index in the sequence at which this method is first encountered (current end of list)
            call_sequence.add(edge_to_new_method);
            call_next_method(callgraph, new_method);
            method_table.remove(hash_code);
            call_sequence.remove(edge_to_new_method);
        }
    }

    public static void explore_call_paths(){
        call_paths = new ArrayList<>();
        CallGraph call_graph = Scene.v().getCallGraph();
        method_table = new HashMap<>();
        call_sequence = new ArrayList<>();
        SootMethod start_method;
        for(Iterator<MethodOrMethodContext> method_iterator = call_graph.sourceMethods(); method_iterator.hasNext();){
            start_method = method_iterator.next().method();
            if(start_method.isEntryMethod()){
                method_table.put(start_method.equivHashCode(), 0);
                call_next_method(call_graph, start_method);
                break;
            }
        }
        System.out.println(call_paths.size());
        //print_call_paths();
    }
}