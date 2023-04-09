package dev.navids.soottutorial.basicapi;

import dev.navids.soottutorial.visual.Visualizer;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.options.Options;
import soot.toolkits.scalar.Pair;

import java.io.File;
import java.util.*;

public class BasicAPI {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "BasicAPI";
    public static String circleClassName = "Circle";
    public static String methodName = "apply_transform_1";

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

    private static void reportLocalInfo(JimpleBody body) {
        System.out.println(String.format("Local variables count: %d", body.getLocalCount()));
        Local thisLocal = body.getThisLocal();
        Type thisType = thisLocal.getType();
        Local paramLocal = body.getParameterLocal(0);
    }

    private static SootMethod reportSootMethodInfo(SootClass target_class) {
        System.out.println("-----Method-----");
        System.out.println(String.format("List of %s's methods:", target_class.getName()));
        for(SootMethod sootMethod : target_class.getMethods())
            System.out.println(String.format("- %s",sootMethod.getName()));
        SootMethod getMethod = target_class.getMethodByName(methodName);
        System.out.println(String.format("Method Signature: %s", getMethod.getSignature()));
        System.out.println(String.format("Method Subsignature: %s", getMethod.getSubSignature()));
        System.out.println(String.format("Method Name: %s", getMethod.getName()));
        System.out.println(String.format("Declaring class: %s", getMethod.getDeclaringClass()));
        int methodModifers = getMethod.getModifiers();
        System.out.println(String.format("Method %s is public: %b, is static: %b, is final: %b", getMethod.getName(), Modifier.isPublic(methodModifers), Modifier.isStatic(methodModifers), Modifier.isFinal(methodModifers)));
        SootMethod constructorMethod = target_class.getMethodByName("<init>");
        /*try{
            SootMethod areaMethod = circleClass.getMethodByName("area");
        }
        catch (Exception exception){
            System.out.println("Th method 'area' is overloaded and Soot cannot retrieve it by name");
        }*/
        return target_class.getMethodByName(methodName);
    }

    private static SootField reportSootFieldInfo(SootClass circleClass) {
        SootField radiusField = circleClass.getField("radius", IntType.v());
        SootField piField = circleClass.getField("double PI");
        System.out.println(String.format("Field %s is final: %b", piField, piField.isFinal()));
        return radiusField;
    }

    private static SootClass reportSootClassInfo() {
        //System.out.println("-----Class-----");
        SootClass circleClass = Scene.v().getSootClass(circleClassName);
        /*System.out.println(String.format("The class %s is an %s class, loaded with %d methods! ",
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
        System.out.println(String.format("Class '%s' is same as class of type '%s': %b", circleClassName, circleType.toString(), circleClass.equals(Scene.v().getSootClass(circleType.toString()))));*/
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

    static int LOOP_LIMIT = 3;

    public static ArrayList<Edge> generate_call_path(HashMap<Integer, Edge> out_edges, SootMethod start_method, int length){
        ArrayList<Edge> path = new ArrayList<>();
        int current = start_method.equivHashCode();
        while(length > 0){
            Edge call_edge = out_edges.get(current);
            path.add(call_edge);
            current = call_edge.tgt().equivHashCode();
            length--;
        }
        return path;
    }

    public static ArrayList<Unit> generate_intra_path(HashMap<Unit, Unit> successor_table, Unit start_unit, Unit call_site){
        ArrayList<Unit> path = new ArrayList<>();
        Unit current = start_unit;
        do{
            path.add(current);
            current = successor_table.get(current);
        }while(!current.equals(call_site));
        path.add(call_site);
        return path;
    }

    //find all methods that are reachable from current basic block without having to force another edge
    public static ArrayList<Edge> compute_closure_of_edge_forcing(){
        return null;
    }

    public static ArrayList<SootMethod> least_method_calls_to_target_block(SootMethod message_generating_method, ArrayList<SootMethod> default_call_sequence){
        if(default_call_sequence.isEmpty()){
            return null;
        }
        CallGraph call_graph = Scene.v().getCallGraph();
        HashSet<Integer> originally_called_methods = new HashSet<>();
        for(int i = 0; i < default_call_sequence.size(); i++){
            originally_called_methods.add(default_call_sequence.get(i).equivHashCode());
        }

        int dist;
        int caller_hashcode;
        HashMap<Integer, Edge> out_edges = new HashMap<>(); //stores the edge that calls each method
        HashMap<Integer, Integer> distances = new HashMap<>();
        Queue<SootMethod> frontier = new LinkedList<>();

        frontier.add(message_generating_method);
        distances.put(message_generating_method.equivHashCode(), 0);
        while(!frontier.isEmpty()){
            SootMethod current_method = frontier.poll();
            dist = distances.get(current_method.equivHashCode());
            for(Iterator<Edge> call_iterator = call_graph.edgesInto(current_method); call_iterator.hasNext();){
                Edge call_edge = call_iterator.next();
                SootMethod caller_method = call_edge.src();
                caller_hashcode = caller_method.equivHashCode();
                if(!out_edges.containsKey(caller_hashcode)){
                    distances.put(caller_hashcode, dist+1);
                    out_edges.put(caller_hashcode, call_edge);
                    if(originally_called_methods.contains(caller_hashcode)){ //arrived on the original call path
                        System.out.println("Found shortest diverging path of length " + (dist+1) +  " to connect to the original path\n");
                        ArrayList<SootMethod> new_path = new ArrayList<>();
                        for(SootMethod existing_method : default_call_sequence){
                            new_path.add(existing_method);
                            if(existing_method.equivHashCode() == caller_hashcode)
                                break;
                        }
                        ArrayList<Edge> new_edges = generate_call_path(out_edges, caller_method, dist+1);
                        for(Edge new_edge : new_edges){
                            new_path.add(new_edge.tgt());
                        }
                        return new_path;
                    }
                    frontier.add(caller_method);
                }
            }
        }
        return null;
    }

    public static ArrayList<Unit> method_entry_point_to_callsite(SootMethod method, Unit call_site){
        JimpleBody method_body = (JimpleBody)method.getActiveBody();
        UnitGraph unit_graph = new ClassicCompleteUnitGraph(method_body);
        Unit first_unit = method_body.getUnits().getFirst();
        HashMap<Unit, Unit> successor_table = new HashMap<>();
        Queue<Unit> queue = new LinkedList<>();
        queue.add(call_site);
        Unit current;
        List<Unit> predecessors;
        while(!queue.isEmpty()){
            current = queue.poll();
            predecessors = unit_graph.getPredsOf(current);
            for(Unit predecessor : predecessors){
                if(!successor_table.containsKey(predecessor)){
                    successor_table.put(predecessor, current);
                    if(predecessor.equals(first_unit)){
                        ArrayList<Unit> path = generate_intra_path(successor_table, first_unit, call_site);
                        return path;
                    }
                    queue.add(predecessor);
                }
            }
        }
        return null;
    }

    /*public static ArrayList<Unit> shortest_path_to_target_block(SootMethod message_generating_method, ArrayList<SootMethod> default_call_sequence){
        ArrayList<Edge> least_methods_path = least_method_calls_to_target_block(message_generating_method, default_call_sequence);
        if(least_methods_path == null){
            return null;
        }
        ArrayList<Unit> shortest_path = new ArrayList<>();
        shortest_path.add(least_methods_path.get(0).srcUnit());
        Edge edge;
        for(int i = 1; i < least_methods_path.size(); i++){
            edge = least_methods_path.get(i);
            ArrayList<Unit> path_inside_method = method_entry_point_to_callsite(edge.src(), edge.srcUnit());
            shortest_path.addAll(path_inside_method);
        }
        return shortest_path;
    }*/

    public static ArrayList<ArrayList<Edge>> all_paths_to_target_block_via_bfs(SootMethod message_generating_method, ArrayList<SootMethod> default_call_sequence){
        if(call_sequence.isEmpty()){
            return null;
        }

        ArrayList<ArrayList<Edge>> paths = new ArrayList<>();

        CallGraph call_graph = Scene.v().getCallGraph();
        HashSet<Integer> originally_called_methods = new HashSet<>();
        for(int i = 0; i < call_sequence.size(); i++){
            originally_called_methods.add(default_call_sequence.get(i).equivHashCode());
        }

        int dist;
        int caller_hashcode;
        HashMap<Integer, Edge> out_edges = new HashMap<>(); //stores the edge that calls each method
        HashMap<Integer, Integer> distances = new HashMap<>();
        Queue<SootMethod> frontier = new LinkedList<>();

        frontier.add(message_generating_method);
        distances.put(message_generating_method.equivHashCode(), 0);
        while(!frontier.isEmpty()){
            SootMethod current_method = frontier.poll();
            dist = distances.get(current_method.equivHashCode());
            for(Iterator<Edge> call_iterator = call_graph.edgesInto(current_method); call_iterator.hasNext();){
                Edge call_edge = call_iterator.next();
                SootMethod caller_method = call_edge.src();
                caller_hashcode = caller_method.equivHashCode();
                if(!out_edges.containsKey(caller_hashcode)){
                    distances.put(caller_hashcode, dist+1);
                    out_edges.put(caller_hashcode, call_edge);
                    if(originally_called_methods.contains(caller_hashcode)){ //arrived on the original call path
                        ArrayList<Edge> new_path = generate_call_path(out_edges, caller_method, dist+1);
                        paths.add(new_path);
                    }
                    frontier.add(caller_method);
                }
                else if(originally_called_methods.contains(caller_hashcode)){
                    out_edges.put(caller_hashcode, call_edge);
                    ArrayList<Edge> new_path = generate_call_path(out_edges, caller_method, dist+1);
                    paths.add(new_path);
                }
            }
        }
        return paths;
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

    public static void main(String[] args) {
        setupSoot();
        
        // Access to Classes
        SootClass circleClass = reportSootClassInfo();
        // Access to Fields
        // SootField radiusField = reportSootFieldInfo(circleClass);
        // Access to Methods
        SootMethod computeValueMethod = reportSootMethodInfo(circleClass);
        // Access to Body (units, locals)
        System.out.println("-----Body-----");
        JimpleBody body = (JimpleBody) computeValueMethod.getActiveBody();
        reportLocalInfo(body);
        Stmt firstNonIdentitiyStmt = body.getFirstNonIdentityStmt();
        int c = 0;

        // SootMethod main_method = circleClass.getMethodByName("main");
        // SootMethod area_bool_method = circleClass.getMethod("int area(boolean)");
        UnitGraph ug = new ClassicCompleteUnitGraph(computeValueMethod.getActiveBody());

        UnitPatchingChain units_in_method = body.getUnits();
        List<UnitBox> predecessors;
        for (Unit u : units_in_method) {
            c++;
            Stmt stmt = (Stmt) u;
            modifyBody(body, stmt);
            System.out.println(String.format("(%d): %s", c, u ));
            //if(stmt.branches())
            //    System.out.println("the above statement is a branch statement");
            //if(stmt.equals(firstNonIdentitiyStmt))
            //    System.out.println("    This statement is the first non-identity statement!");
            /*if(stmt.containsFieldRef()){
                System.out.print("reporting field reference:");
                reportFieldRefInfo(radiusField, stmt);
            }*/
            /*if(doesInvokeMethod(stmt, "int area()", circleClassName)){
                System.out.println("    This statement invokes 'int area()' method");
            }*/
            //predecessors = stmt.getBoxesPointingToThis();
            //System.out.println("fallthrough: " + stmt.fallsThrough() + " # preds: " + predecessors.size());
            //System.out.println(ug.getPredsOf(u));
            // List<Unit> pred_Us = ug.getPredsOf(u);

            // for(Unit pred_U : pred_Us){
            //     // System.out.println(String.format("(%d): %s", c, pred_U ));
            // }

            //if(predecessors.size() > 0){
            //    System.out.print("predecessors: ");
            //    for(UnitBox pred : predecessors){
                    // System.out.println(pred.getUnit() + "    " + units_in_method.getPredOf(pred.getUnit()));
            //        System.out.println(ug.getPredsOf(pred.getUnit()));
                    //Stmt pred_stmt = (Stmt)(pred.getUnit());
                    //System.out.println(pred_stmt);
            //    }
            //}
        //    System.out.println();
        }
        //for(Trap trap : body.getTraps()){
        //    System.out.println(trap);
        //}
        //try {
        //    body.validate();
        //    System.out.println("Body is validated! No inconsistency found.");
        //}
        //catch (Exception exception){
        //    System.out.println("Body is not validated!");
        //}
        
        // Call graph

        CallGraph callGraph = Scene.v().getCallGraph();
        //System.out.println("-----CallGraph-----");
        /*for(Iterator<Edge> it = callGraph.edgesOutOf(areaMethod); it.hasNext(); ){
            Edge edge = it.next();
            System.out.println(String.format("Method '%s' invokes method '%s' through stmt '%s", edge.src(), edge.tgt(), edge.srcUnit()));
        }*/
        if(args.length >= 2 && args[1].equals("display_path")){
            System.out.println("\n\n[ZombieJazz] display_path enabled\n");
            SootMethod main_method = circleClass.getMethodByName("main");
            //SootMethod area_bool_method = circleClass.getMethod("int area(boolean)");
            ArrayList<SootMethod> default_path = new ArrayList<>();
            default_path.add(main_method);
            default_path.add(circleClass.getMethodByName("setup"));
            default_path.add(circleClass.getMethodByName("compute_value"));
            default_path.add(circleClass.getMethodByName("hash"));
            default_path.add(circleClass.getMethodByName("send_untransformed"));
            //default_path.add(area_bool_method);
            SootMethod target_method = circleClass.getMethodByName("send_transform_result");
            System.out.println();
            ArrayList<SootMethod> shortest_path = least_method_calls_to_target_block(target_method, default_path);
            if(shortest_path != null){
                System.out.println("--------original path--------\n");
                for(SootMethod method : default_path)
                    System.out.println(method);
                System.out.println();
                System.out.println("--------new path--------\n");
                for(SootMethod method : shortest_path)
                    System.out.println(method);
                System.out.println();
            }
            System.out.println();
        }
        else{
            System.out.println("not printing paths..\n");
        }
        boolean drawGraph = false;
        if (args.length > 0 && args[0].equals("draw"))
            drawGraph = true;
        if (drawGraph){
            Visualizer.v().addCallGraph(callGraph, edge -> edge.src().getDeclaringClass().isApplicationClass(),
                sootMethod -> new Pair<>(sootMethod.getDeclaringClass().isApplicationClass() ? "cg_node, default_color" : "cg_node, default_color"
                , sootMethod.getDeclaringClass().isApplicationClass() ? sootMethod.getSubSignature() : sootMethod.getSignature()));
            Visualizer.v().draw();
            Visualizer.v().addUnitGraph(ug);
            Visualizer.v().draw();
        }
    }
}