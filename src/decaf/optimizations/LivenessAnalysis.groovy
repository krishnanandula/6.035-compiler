package decaf.optimizations
import decaf.*
import decaf.graph.GraphNode
import decaf.graph.*
import static decaf.graph.Traverser.eachNodeOf

// This analysis is essentially a copy of the one from DCE.
class LivenessAnalysis extends Analizer {
  LivenessAnalysis() {
    dir = AnalysisDirection.BACKWARD
  }

  final void lazyInit(node) {
    if (node.anno['regalloc-liveness'] == null)
      node.anno['regalloc-liveness'] = new HashSet()
  }

  void store(GraphNode node, Set data) {
    lazyInit(node)
    node.anno['regalloc-liveness'] = data
  }

  Set load(GraphNode node) {
    lazyInit(node)
    return node.anno['regalloc-liveness']
  }

  def gen(node) {
    //if(node instanceof LowIrIntLiteral)
    //  println "gen: $node, ${node.getUses()}"
    return new LinkedHashSet(node.getUses())
  }

  def kill(node) {
    //if(node instanceof LowIrIntLiteral)
    //  println "kill: $node, ${node.getDef()}"
    return Collections.singleton(node.getDef());    
  }

  Set transfer(GraphNode node, Set input) {
    def out = gen(node)
    out.addAll(input - kill(node))
    return out
  }

  Set join(GraphNode node) {
    def out = new HashSet()
    for (succ in node.successors) {
      out += load(succ)
    }
    return out
  }

  boolean sideEffectFree(LowIrNode it) {
    //can have side effects
    if (it instanceof LowIrCallOut || it instanceof LowIrMethodCall) {
      return false
    }
    //could be div by 0, resulting in a runtime exception
    if (it instanceof LowIrBinOp && it.op == BinOpType.DIV) {
      return false
    }

    return true
  }

  def run(startNode) {
    println "Running Liveness Analysis!"
    analize(startNode)
  }
}
