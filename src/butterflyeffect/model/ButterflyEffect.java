package butterflyeffect.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ButterflyEffect extends Model {
	protected List<ButterflyEffect> effects;
	
	private static IModelVisitor adder = new Adder();
	private static IModelVisitor remover = new Remover();
	
	public static JSONObject toJSON(ButterflyEffect bEffect) throws IOException{
		try {
			JSONObject json = new JSONObject();
			json.put("fileName", bEffect.getName());
			json.put("lineNumber", bEffect.getLine());
			json.put("description", bEffect.getDescription());
			List<JSONObject> children = new ArrayList<JSONObject>();
			for(ButterflyEffect mb: bEffect.getEffects()) {
				children.add(toJSON(mb));
			}
			json.put("effects", children);
			return json;
		} catch(Exception e) {
			throw new IOException(e);
		}
	}
	
	public static ButterflyEffect fromJSON(JSONObject json) {
		ButterflyEffect bEffect;
		String fileName = (String)json.get("fileName");
		bEffect = fileName != null ? new ButterflyEffect(fileName) : new ButterflyEffect();
		int lineNumber = ((Long)json.get("lineNumber")).intValue();
		bEffect.setLine(lineNumber);
		bEffect.setDescription((String)json.get("description"));
		JSONArray effects = (JSONArray)json.get("effects");
		for(Object effect: effects) {
			bEffect.add(fromJSON((JSONObject)effect));
		}
		return bEffect;
	}
	
	public ButterflyEffect() {
		effects = new ArrayList<ButterflyEffect>();
	}
	
	private static class Adder implements IModelVisitor {
		public void visitEffect(ButterflyEffect bEffect, Object argument) {
			((ButterflyEffect) argument).addEffect(bEffect);
		}
	}

	private static class Remover implements IModelVisitor {

		public void visitEffect(ButterflyEffect bEffect, Object argument) {
			((ButterflyEffect) argument).removeEffect(bEffect);
		}

	}
	
	public ButterflyEffect(String name) {
		this();
		this.name = name;
	}
	
	public List<ButterflyEffect> getEffects() {
		return effects;
	}
	
	protected void addEffect(ButterflyEffect bEffect) {
		effects.add(bEffect);
		bEffect.parent = this;
		fireAdd(bEffect);
	}
	
	public void remove(Model toRemove) {
		toRemove.accept(remover, this);
	}

	protected void removeEffect(ButterflyEffect bEffect) {
		effects.remove(bEffect);
		bEffect.addListener(NullDeltaListener.getSoleInstance());
		fireRemove(bEffect);	
	}

	public void add(Model toAdd) {
		toAdd.accept(adder, this);
	}
	

	public int size() {
		return getEffects().size();
	}

	public void accept(IModelVisitor visitor, Object passAlongArgument) {
		visitor.visitEffect(this, passAlongArgument);
	}

}
