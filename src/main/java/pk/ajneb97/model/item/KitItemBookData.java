package pk.ajneb97.model.item;

import java.util.ArrayList;
import java.util.List;

public record KitItemBookData(List<String> pages, String author, String generation, String title) {
    public KitItemBookData copy(){
        return new KitItemBookData(new ArrayList<>(pages),author,generation,title);
    }
}
