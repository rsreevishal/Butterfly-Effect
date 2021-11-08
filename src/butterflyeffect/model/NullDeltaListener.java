package butterflyeffect.model;
public class NullDeltaListener implements IDeltaListener {
	protected static NullDeltaListener soleInstance = new NullDeltaListener();
	public static NullDeltaListener getSoleInstance() {
		return soleInstance;
	}
	public void add(DeltaEvent event) {}
	public void remove(DeltaEvent event) {}
}
