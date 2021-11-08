package butterflyeffect.model;



public interface IModelVisitor {
	public void visitEffect(ButterflyEffect bEffect, Object passAlongArgument);
}
