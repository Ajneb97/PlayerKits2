package pk.ajneb97.model.item;

public record KitItemTrimData(String pattern, String material) {
	public KitItemTrimData copy(){
		return new KitItemTrimData(pattern,material);
	}
}
