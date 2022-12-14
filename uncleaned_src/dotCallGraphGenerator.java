import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.util.*;
import soot.util.Chain.*;
import soot.jimple.NopStmt;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.*;
import soot.javaToJimple.LocalGenerator;
import soot.AbstractASMBackend;
import soot.baf.BafASMBackend;

import java.util.concurrent.ThreadLocalRandom;
import java.io.*;
import java.util.*;

public class dotCallGraphGenerator {
  public static String sourceDirectory = System.getProperty("user.dir");
  public static SootClass entrySC;
  public static SootMethod entrySM;

  public static void configureSoot(
    String entryClassName, String entryMethodName) {
    G.reset();

    Options.v().set_prepend_classpath(true);
    //Options.v().set_src_prec(Options.src_prec_java);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_soot_classpath(sourceDirectory);

    Options.v().set_whole_program(true);
    Options.v().set_output_format(Options.output_format_none); Options.v().setPhaseOption("cg.spark", "verbose:false");

    //Scene.v().loadNecessaryClasses();
    //for (SootClass sc : Scene.v().getClasses()) {
    //  Scene.v().loadClassAndSupport(sc.getName());
    //  System.out.println(sc.getName());
    //}

    //SootClass entrySC = Scene.v().getSootClassUnsafe(entryClassName);

    entrySC = Scene.v().loadClassAndSupport(entryClassName);
    //SootClass entrySC = Scene.v().loadClassAndSupport("TestInvoke");
    entrySC.setApplicationClass();
    Scene.v().loadNecessaryClasses();
    //SootClass entrySC = Scene.v().loadClassAndSupport(entryClassName);
    //entrySC.setApplicationClass();
    //Scene.v().loadNecessaryClasses();
    //SootClass sc2 = Scene.v().getSootClassUnsafe("MyCounter");
    //for (SootMethod sm : entrySC.getMethods()) {
    //  System.out.println("Hello1");
    //  System.out.println(sm);
    //}
    //for (SootMethod sm : sc2.getMethods()) {
    //  System.out.println("Hello2");
    //  System.out.println(sm);
    //}
    //System.out.println("Hellofinal");
    entrySM = entrySC.getMethodByNameUnsafe(entryMethodName);

    //System.out.println(entrySC);
    //System.out.println(sc2);
    //System.out.println(entrySM);

    Options.v().set_main_class(entrySM.getSignature());
    Scene.v().setEntryPoints(Collections.singletonList(entrySM));
    PackManager.v().runPacks();

  }

  private static boolean omitSootMethod(SootMethod sm) {
    if (sm.getName().equals("<clinit>") || sm.getName().equals("<init>")) return true;
    if (sm.getName().equals("A0")) return false;
    if (sm.getName().equals("A1")) return false;
    if (sm.getName().equals("A2")) return false;
    if (sm.getName().equals("A3")) return false;
    if (sm.getName().equals("B0")) return false;
    if (sm.getName().equals("B1")) return false;
    if (sm.getName().equals("B2")) return false;
    if (sm.getName().equals("B3")) return false;
    if (sm.getName().equals("C0")) return false;
    if (sm.getName().equals("C1")) return false;
    if (sm.getName().equals("C2")) return false;
    if (sm.getName().equals("C3")) return false;
    if (sm.getName().equals("main")) return false;
    if (sm.getName().equals("endProgram")) return true;
    return true;

    //Iterator<SootClass> itClass = Scene.v().getClasses().iterator();
    //if (!sm.getDeclaringClass().toString().equals("no_ifs")) return true;
    //return false;

    //boolean found = false;
    //while (itClass.hasNext()) {
    //  SootClass scTemp = (SootClass) itClass.next();
    //  System.out.println(scTemp.getName().toString());
    //  //if (sm.getDeclaringClass().toString().equals(scTemp.getName().toString())) {
    //  if (sm.getDeclaringClass().toString().equals(scTemp.getName().toString())) {
    //    found = true;
    //    break;
    //  }
    //}
    //return !found;
  }

  private static String genRetAndParaKey(SootMethod sm) {
    return sm.getReturnType().toString() + sm.getParameterTypes();
  }

  public static void printMethod(SootMethod method) {
    System.out.println("" + method);
    System.out.println("\t\t Src " + method.getDeclaringClass());
    //System.out.println("\t\t Sig " + method.getSignature());
    System.out.println("\t\t Sub " + method.getSubSignature());
    //System.out.println("\t\t Fnk " + method.getName());
    //System.out.println("\t\t Par " + method.getParameterTypes());
    //System.out.println("\t\t Ret " + method.getReturnType());
  }

  //protected static class SootMethodData {
  //  protected Type returnType;
  //  protected List<Type> parameterTypes;
  //  protected String functionName;

  //  protected SootMethodData(Type r, List<Type> p, String s) {
  //    returnType = r;
  //    parameterTypes = p;
  //    functionName = s;
  //  }
  //}

  private static void addToBranchHTable(
    Hashtable<String, List<SootMethod>> htable,
    SootMethod sm)
  {
    String hashKey = genRetAndParaKey(sm);
    //SootMethodData newData = new SootMethodData(sm.getReturnType(), sm.getParameterTypes(), sm.getName());
    //System.out.println(newData.functionName);

    if (htable.containsKey(hashKey)) {
      List<SootMethod> l = htable.get(hashKey);
      l.add(sm);
    } else {
      List<SootMethod> newList = new ArrayList<SootMethod>(Arrays.asList(sm));
      htable.put(hashKey, newList);
    }

    //System.out.println(hash_key);
    //System.out.println(sm);
  }

  private static Local generateNewLocal(Body body, Type type) {
    LocalGenerator lg = new LocalGenerator(body);
		return lg.generateLocal(type);
  }
  
  protected static class BranchStaticInstrumenter extends BodyTransformer {
    @Override
    protected void internalTransform(Body body, String phase, Map map) {

      Chain units = body.getUnits();
      var stmtIt = units.snapshotIterator();

      int c = 1;
      
      List<Stmt> invokes = new ArrayList<Stmt>();

      while (stmtIt.hasNext()) {
        Stmt stmt = (Stmt) stmtIt.next();
        System.out.println("(" + c + ") " + stmt);
        //if (stmt instanceof JIfStmt) 
        if (stmt.containsInvokeExpr()) {
          InvokeExpr expr = (InvokeExpr) stmt.getInvokeExpr();
          if (expr instanceof StaticInvokeExpr) {
            invokes.add(stmt);
            //System.out.println("YAYAYAY");
            //List<Unit> generated = new ArrayList<>();
          
            ////Local arg;
            ////arg = Jimple.v().newLocal

            //Local localBoolean = generateNewLocal(body, BooleanType.v());
            //AssignStmt astmt = Jimple.v().newAssignStmt(localBoolean, IntConstant.v(0));
            //generated.add(astmt);
            //
            //EqExpr equalExpr = Jimple.v().newEqExpr(localBoolean, IntConstant.v(0));
            ////NopStmt nop = Jimple.v().newNopStmt();
            //IfStmt ifStmt = Jimple.v().newIfStmt(equalExpr, stmt);
            //generated.add(ifStmt);

            //System.out.println(astmt);
            //System.out.println(ifStmt);

            //units.insertBefore(generated, stmt);
            //units.insertAfter(nop, stmt);
          }
        }
        c++;
      }

      for (Stmt s : invokes) {
        System.out.println(s);

        Stmt n = (Stmt) units.getSuccOf(s);

        //List<Unit> generated = new ArrayList<>();
      
        //Local localBoolean = generateNewLocal(body, BooleanType.v());
        //AssignStmt astmt = Jimple.v().newAssignStmt(localBoolean, IntConstant.v(0));
        //
        //EqExpr equalExpr = Jimple.v().newEqExpr(localBoolean, IntConstant.v(0));
        ////NopStmt nop = Jimple.v().newNopStmt();
        //IfStmt ifStmt = Jimple.v().newIfStmt(equalExpr, (Stmt)s);
        GotoStmt gtStmt = Jimple.v().newGotoStmt((Stmt)n);

        //InvokeStmt iStmt = Jimple.v().newInvokeStmt(
        //  Jimple.v().newStaticInvokeExpr(

        
        SootMethod addSM = entrySC.getMethodByNameUnsafe("A1");
        System.out.println("add SM = " + addSM);
        InvokeExpr incExpr = Jimple.v().newStaticInvokeExpr(addSM.makeRef());
        Stmt incStmt = Jimple.v().newInvokeStmt(incExpr);

        Local localBoolean = generateNewLocal(body, BooleanType.v());
        AssignStmt astmt = Jimple.v().newAssignStmt(localBoolean, IntConstant.v(0));
        
        EqExpr equalExpr = Jimple.v().newEqExpr(localBoolean, IntConstant.v(0));
        IfStmt ifStmt = Jimple.v().newIfStmt(equalExpr, incStmt);
        
        //units.insertAfter(incStmt, s);
        //units.insertAfter(gtStmt, s);

        //units.insertBefore(astmt, s);
        //units.insertBefore(ifStmt, s);

        //generated.add(gtStmt);
        //generated.add(ifStmt);
        //generated.add(astmt);

        //System.out.println(astmt);
        //System.out.println(ifStmt);
        //System.out.println(gtStmt);

        //units.insertBefore(generated, s);
        //units.insertBefore(astmt, s);
        //units.insertBefore(ifStmt, s);
        //units.insertBefore(gtStmt, s);
        //units.insertBefore(ifStmt, gtStmt);
        //units.insertBefore(astmt, ifStmt);

      }

      c= 1;
      stmtIt = units.snapshotIterator();

      //JimpleBody newBody = Jimple.v().newBody(entrySM);
      //Chain newUnits = newBody.getUnits();

      while (stmtIt.hasNext()) {
        Stmt stmt = (Stmt) stmtIt.next();
        //newUnits.add(stmt);
        System.out.println("(" + c + ") " + stmt);
        c++;

        //if (stmt instanceof JIfStmt) {
        //  System.out.println("if target = " + stmt.getTarget());

        //}
      }
    }
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {
    String entryClassName = args[0];
    String entryMethodName = args[1];
    configureSoot(entryClassName, entryMethodName);
    CallGraph callGraph = Scene.v().getCallGraph();


    //Hashtable<String, List<SootMethodData>> htable = new Hashtable<>();
    Hashtable<String, List<SootMethod>> branchHTable = new Hashtable<>();

    // Not allocated -> Not encountered
    // Allocated false -> Enqueued
    // Allocated true -> Dequeued
    Hashtable<SootMethod, Boolean> bfsVisited = new Hashtable<>(); 

    Queue<SootMethod> bfsQueue = new LinkedList<>();

    // Enqueue all the static soot methods (owned by the class)
    // Cannot simply start from the entry method since some
    // methods are never called but can be used to instrument code
    for (SootMethod sm : entrySC.getMethods()) {

      if (omitSootMethod(sm)) continue;

      System.out.println(sm);

      bfsQueue.add(sm);
      assert !bfsVisited.containsKey(sm);
      bfsVisited.put(sm, false);
    }

    while (!bfsQueue.isEmpty()) {
      SootMethod sm = bfsQueue.remove();
      if (omitSootMethod(sm)) continue;

      assert bfsVisited.get(sm) == false;
      bfsVisited.put(sm, true);

      addToBranchHTable(branchHTable, sm);

      Iterator<Edge> edgeIt = callGraph.edgesOutOf(sm);
      while (edgeIt.hasNext()) {
        Edge e = (Edge) edgeIt.next();
        SootMethod new_sm = e.tgt();
        if (omitSootMethod(new_sm)) continue;
        if (bfsVisited.containsKey(new_sm)) {
          assert bfsVisited.get(new_sm) == false;
          continue;
        }

        bfsVisited.put(new_sm, false);
        bfsQueue.add(new_sm);
        addToBranchHTable(branchHTable, new_sm);
      }
    }

    //System.out.println(branchHTable);

    //configureSoot(entryClassName, entryMethodName);
    //G.reset();
    //Options.v().set_prepend_classpath(true);
    //Options.v().set_allow_phantom_refs(true);
    //Options.v().set_soot_classpath(sourceDirectory);
    //Options.v().set_output_format(Options.output_format_class);
    //Options.v().set_whole_program(false);
    
    //SootMethod testSM = entrySC.getMethodByNameUnsafe("B2");
    //for (SootMethod sm : branchHTable.get(genRetAndParaKey(testSM))) {
    //  System.out.println(sm.getName());
    //}

    //Options.v().set_whole_program(false);

  int maxChanges = 0;

  Queue<SootMethod> changeQ = new LinkedList<>();
  changeQ.add(entrySC.getMethodByNameUnsafe("main"));
  changeQ.add(entrySC.getMethodByNameUnsafe("A0"));
  changeQ.add(entrySC.getMethodByNameUnsafe("A1"));
  changeQ.add(entrySC.getMethodByNameUnsafe("A2"));
  changeQ.add(entrySC.getMethodByNameUnsafe("A3"));
  changeQ.add(entrySC.getMethodByNameUnsafe("B0"));
  changeQ.add(entrySC.getMethodByNameUnsafe("B1"));
  changeQ.add(entrySC.getMethodByNameUnsafe("B2"));
  changeQ.add(entrySC.getMethodByNameUnsafe("B3"));
  changeQ.add(entrySC.getMethodByNameUnsafe("C0"));
  changeQ.add(entrySC.getMethodByNameUnsafe("C1"));
  changeQ.add(entrySC.getMethodByNameUnsafe("C2"));
  changeQ.add(entrySC.getMethodByNameUnsafe("C3"));
  for (SootMethod sm : changeQ) {
    if (omitSootMethod(sm)) continue;

    maxChanges++;
    //if (maxChanges > 10) break;

      JimpleBody body = (JimpleBody) sm.retrieveActiveBody();
      //body.validate();
      //body.validateLocals();
      //body.validateTraps();
      //body.validateUnitBoxes();
      //body.validateUses();
      //new BranchStaticInstrumenter().transform(body);

      Chain units = body.getUnits();
      //JimpleBody newBody = Jimple.v().newBody(entrySM);
      //Chain newUnits = newBody.getUnits();

      var stmtIt = units.snapshotIterator();


      int c = 1;
      List<Stmt> invokes = new ArrayList<Stmt>();
      while (stmtIt.hasNext()) {
        Stmt stmt = (Stmt) stmtIt.next();
        //System.out.println("(" + c + ") " + stmt);
        if (stmt.containsInvokeExpr()) {
          InvokeExpr expr = (InvokeExpr) stmt.getInvokeExpr();
          if (expr instanceof StaticInvokeExpr) {
            invokes.add(stmt);
          }
        }
        c++;
      }

      for (Stmt s : invokes) {
        System.out.println(s);

        //Setup the new function by looking at the function signature
        //and args count of the original function
        InvokeExpr expr = (InvokeExpr) s.getInvokeExpr();
        if (omitSootMethod(expr.getMethod())) continue;
        List<Value> oldArgs = expr.getArgs();
        String hashKey = genRetAndParaKey(expr.getMethod());
        //System.out.println("hashkey = " + hashKey);
        if (!branchHTable.containsKey(hashKey)) continue;
        List<SootMethod> swapList = branchHTable.get(hashKey);
        //System.out.println("swapLIst = " + swapList);
        int totalCount = swapList.size();
        //int index = ThreadLocalRandom.current().nextInt(0, totalCount);
        Random r = new Random();
        int index = r.nextInt(totalCount);
        //System.out.println("totalCount = " + totalCount);
        //System.out.println("index = " + index);

        SootMethod chosenSM = swapList.get(index);
        String chosenSM_name = chosenSM.getName().toString();

        Stmt n = (Stmt) units.getSuccOf(s);
        GotoStmt gtStmt = Jimple.v().newGotoStmt((Stmt)n);

        
        //SootMethod addSM = entrySC.getMethodByNameUnsafe("A1");
        SootMethod addSM = entrySC.getMethodByNameUnsafe(chosenSM_name);
        //System.out.println("add SM = " + addSM);
        //InvokeExpr incExpr;
        InvokeExpr incExpr = Jimple.v().newStaticInvokeExpr(addSM.makeRef(), oldArgs);
        //if (oldArgs.size() == 1) {
        //  incExpr = Jimple.v().newStaticInvokeExpr(addSM.makeRef());
        //} else {
        //  incExpr = Jimple.v().newStaticInvokeExpr(addSM.makeRef(), IntConstant.v(1));
        //}
        
        Stmt incStmt = Jimple.v().newInvokeStmt(incExpr);
        System.out.println("incStmt = " + incStmt);

        Local localBoolean = generateNewLocal(body, BooleanType.v());
        AssignStmt astmt = Jimple.v().newAssignStmt(localBoolean, IntConstant.v(0));
          
        EqExpr equalExpr = Jimple.v().newEqExpr(localBoolean, IntConstant.v(0));
        IfStmt ifStmt = Jimple.v().newIfStmt(equalExpr, incStmt);
        
        units.insertAfter(incStmt, s);
        units.insertAfter(gtStmt, s);

        units.insertBefore(astmt, s);
        units.insertBefore(ifStmt, s);
      }

      //c= 1;
      //stmtIt = units.snapshotIterator();
      //System.out.println("-----------------------------");

      //while (stmtIt.hasNext()) {
      //  Stmt stmt = (Stmt) stmtIt.next();
      //  System.out.println("(" + c + ") " + stmt);
      //  c++;
      //}
      //System.out.println("-----------------------------");
    }
  


    int java_version = Options.v().java_version();
    String fileName = SourceLocator.v().getFileNameFor(entrySC, Options.output_format_class);
    OutputStream streamOut = new FileOutputStream(fileName);
    BafASMBackend backend = new BafASMBackend(entrySC, java_version);
    backend.generateClassFile(streamOut);
    streamOut.close();

    System.out.println(branchHTable);



  //{


  //  Chain units = body.getUnits();
  //  var stmtIt = units.snapshotIterator();
  //  int c=1;
  //  while (stmtIt.hasNext()) {
  //    Stmt stmt = (Stmt) stmtIt.next();
  //    System.out.println("(" + c + ") " + stmt);
  //    //if (stmt instanceof JIfStmt) 
  //    if (stmt.containsInvokeExpr()) {
  //      InvokeExpr expr = (InvokeExpr) stmt.getInvokeExpr();
  //      if (expr instanceof StaticInvokeExpr) {
  //        System.out.println("YAYAYAY");
  //        List<Unit> generated = new ArrayList<>();
  //      
  //        //Local arg;
  //        //arg = Jimple.v().newLocal

  //        Local localBoolean = generateNewLocal(body, BooleanType.v());
  //        AssignStmt astmt = Jimple.v().newAssignStmt(localBoolean, IntConstant.v(0));
  //        EqExpr equalExpr = Jimple.v().newEqExpr(localBoolean, IntConstant.v(0));

  //        //generated.add(astmt);

  //        units.insertBefore(astmt, stmt);
  //      }
  //    }
  //    c++;
  //  }
  //}

  ////// Get all methods in the entry class
  ////// Hash them by return type + parameters
  ////for (SootMethod sm : entrySC.getMethods()) {
  ////  if (omitSootMethod(sm)) continue;

  ////  addToBranchHTable(htable, sm); 
  ////}

  ////System.out.println(htable);
  //
  //for (Iterator<Edge> it = callGraph.edgesOutOf(entrySM); it.hasNext(); ) {
  //  Edge edge = (Edge) it.next();
  //  SootMethod target_sm = edge.tgt();
  //  
  //  if (omitSootMethod(target_sm)) continue;

  //  printMethod(target_sm);
  //  String hash_key = genRetAndParaKey(target_sm);
  //  if (branchHTable.containsKey(hash_key)) {
  //    for (SootMethod sm : branchHTable.get(hash_key)) {
  //      System.out.println(sm.getName());
  //    }
  //  } else {
  //    System.out.println("Unencountered Key: " + hash_key);
  //  }

  //  //System.out.println(target_sm);
  //}

}


}
