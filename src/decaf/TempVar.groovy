package decaf

class TempVar {
  int id //unique per temp var within a method
  TempVarType type //LOCAL or PARAM
  VariableDescriptor desc //if it's tied to a variable, this is the descriptor for that variable

  //these are filled in during ssa-ification
  LowIrNode defSite
  //if a site uses a variable n times, it will appear n times in useSites
  List<LowIrNode> useSites = []

  String toString() {
    "TempVar($id, $type)"
  }
}

enum TempVarType {
  LOCAL,
  PARAM
}

//Within a methoddesc, tracks temp var usage and contains the code to allocate tempvars on the hiir and symboltables
class TempVarFactory {
  def MethodDescriptor methodDesc // this is the methodDesciptor for the program fragment we're making temps for

  def tmpVarId = 0  //how many tempvars have been allocated locally?

  int nextId() {
    return tmpVarId++
  }

  TempVarFactory(MethodDescriptor desc) {
    methodDesc = desc
  }

  //put tmpVar on every hiir node and params
  void decorateMethodDesc() {
    methodDesc.block.inOrderWalk { cur ->
      if(cur instanceof Expr ||
        cur instanceof StringLiteral){
        // Allocates TempVar()s for temporary nodes
        try {
          declVar('tmpVar', createLocalTemp())
        } catch (MissingPropertyException e) {}
      } else if (cur instanceof Block || cur instanceof ForLoop) {
        // Allocates TempVar()s for all declared variables
        cur.symTable.@map.each { k, v ->
          v.tmpVar = createLocalTemp()
          v.tmpVar.desc = v
        }
      }
      walk()
    }
    methodDesc.params.eachWithIndex {param, index ->
      param.tmpVar = new TempVar(id: index, type: TempVarType.PARAM)
    }
  }

  TempVar createLocalTemp() {
    return new TempVar(id: nextId(), type: TempVarType.LOCAL)
  }
}
