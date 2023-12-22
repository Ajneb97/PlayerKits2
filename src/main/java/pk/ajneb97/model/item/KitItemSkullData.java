package pk.ajneb97.model.item;

public class KitItemSkullData {

    private String owner;
    private String texture;
    private String id;
    public KitItemSkullData(String owner, String texture, String id) {
        super();
        this.owner = owner;
        this.texture = texture;
        this.id = id;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getTexture() {
        return texture;
    }
    public void setTexture(String texture) {
        this.texture = texture;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public KitItemSkullData clone(){
        return new KitItemSkullData(owner,texture,id);
    }
}
