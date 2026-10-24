package pk.ajneb97.model.item;

public record KitItemSkullData(String owner, String texture, String id) {
    public KitItemSkullData copy(){
        return new KitItemSkullData(owner,texture,id);
    }
}
