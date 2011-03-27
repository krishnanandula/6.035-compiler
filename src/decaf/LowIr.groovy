package decaf
import decaf.graph.*

class LowIr {}

class LowIrBridge {
  LowIrNode begin, end

  LowIrBridge(LowIrNode node) {
    begin = node
    end = node
  }

  LowIrBridge(LowIrNode begin, LowIrNode end) {
    this.begin = begin
    this.end = end
  }

  LowIrBridge seq(LowIrBridge next) {
    LowIrNode.link(this.end, next.begin)
    if (next instanceof LowIrValueBridge) {
      return new LowIrValueBridge(this.begin, next.end)
    } else {
      return new LowIrBridge(this.begin, next.end)
    }
  }

  void insertBetween(LowIrNode before, LowIrNode after) {
    LowIrNode.unlink(before, after)
    LowIrNode.link(before, begin)
    LowIrNode.link(end, after)
    //fix the explicit links in Cond Jumps
    if (before instanceof LowIrCondJump) {
      if (before.trueDest == after) {
        before.trueDest = begin
      } else if (before.falseDest == after) {
        before.falseDest = begin
      } else {
        assert false
      }
    }
  }

  void insertBefore(LowIrNode node) {
    def noop = new LowIrNode(metaText: 'insertBefore cruft')
    node.predecessors.clone().each {
      LowIrNode.unlink(it, node)
      LowIrNode.link(it, noop)
    }
    LowIrNode.link(noop, node)
    insertBetween(noop, node)
  }

  //removes this bridge from the lowir
/*
  void excise() {
    assert end.successors.size() <= 1
    def successors = end.successors.clone()
    def predecessors = begin.predecessors.clone()
    predecessors.each {
      LowIrNode.unlink(it, begin)
      LowIrNode.link(it, )//here
    }
    LowIrNode.
  }
*/
}

class LowIrValueBridge extends LowIrBridge {
  TempVar tmpVar

  LowIrValueBridge(LowIrValueNode node) {
    super(node)
    tmpVar = node.tmpVar
  }

  LowIrValueBridge(LowIrNode begin, LowIrValueNode end) {
    super(begin, end)
    tmpVar = end.tmpVar
  }
}

class LowIrNode implements GraphNode{
  def anno = [:]

  def predecessors = []
  def successors = []

  def metaText = ''
  def frak = false

  static int labelNum = 0
  def label = 'label'+(labelNum++)

  List getPredecessors() { predecessors }
  List getSuccessors() { successors }

  static void link(LowIrNode fst, LowIrNode snd) {
    fst.successors << snd
    snd.predecessors << fst
  }

  static void unlink(LowIrNode fst, LowIrNode snd) {
    assert fst.successors.contains(snd)
    assert snd.predecessors.contains(fst)
    fst.successors.remove(snd)
    snd.predecessors.remove(fst)
  }

  String toString() {
    "LowIrNode($metaText)"
  }
}

class LowIrCondJump extends LowIrNode {
  TempVar condition
  LowIrNode trueDest, falseDest

  String toString() {
    "LowIrCondJump(condition: $condition)"
  }
}

class LowIrCallOut extends LowIrValueNode {
  String name
  TempVar[] paramTmpVars

  String toString() {
    "LowIrCallOut(method: $name, tmpVar: $tmpVar, params: $paramTmpVars)"
  }
}


class LowIrMethodCall extends LowIrValueNode {
  MethodDescriptor descriptor
  TempVar[] paramTmpVars

  String toString() {
    "LowIrMethodCall(method: $descriptor.name, tmpVar: $tmpVar, params: $paramTmpVars)"
  }
}

class LowIrReturn extends LowIrValueNode {
  TempVar tmpVar

  String toString() {
    "LowIrReturn(tmpVar: $tmpVar)"
  }
}

class LowIrValueNode extends LowIrNode{
  TempVar tmpVar

  String toString() {
    "LowIrValueNode($metaText, tmpVar: $tmpVar)"
  }
}

class LowIrStringLiteral extends LowIrValueNode {
  String value

  String toString() {
    "LowIrStringLiteral(value: $value, tmpVar: $tmpVar)"
  }
}

class LowIrIntLiteral extends LowIrValueNode {
  int value

  String toString() {
    "LowIrIntLiteral(value: $value, tmpVar: $tmpVar)"
  }
}

class LowIrBinOp extends LowIrValueNode {
  TempVar leftTmpVar, rightTmpVar
  BinOpType op

  String toString() {
    "LowIrBinOp(op: $op, leftTmp: $leftTmpVar, rightTmp: $rightTmpVar, tmpVar: $tmpVar)"
  }
}

class LowIrMov extends LowIrNode {
  TempVar src, dst

  String toString() {
    "LowIrMov(src: $src, dst: $dst)"
  }
}

class LowIrStore extends LowIrNode {
  VariableDescriptor desc
  TempVar index
  TempVar value //this is what gets stored

  String toString() {
    "LowIrStore(dest: $desc, index: $index)"
  }
}

class LowIrLoad extends LowIrValueNode {
  VariableDescriptor desc
  TempVar index

  String toString() {
    "LowIrLoad(dest: $desc, index: $index)"
  }
}

class LowIrPhi extends LowIrValueNode {
  TempVar[] args

  String toString() {
    "LowIrPhi(tmpVar: $tmpVar, args: $args)"
  }
}
