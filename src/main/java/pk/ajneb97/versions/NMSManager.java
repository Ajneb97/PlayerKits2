package pk.ajneb97.versions;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.utils.ServerVersion;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public class NMSManager {

    private final Version version;
    private final ServerVersion serverVersion;
    private final PlayerKits2 plugin;

    public NMSManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.version = new Version();
        this.serverVersion = PlayerKits2.serverVersion;

        if(serverVersionGreaterEqualThan(ServerVersion.v1_20_R4)){
            return;
        }

        try {

            version.addClass("CraftItemStack",Class.forName("org.bukkit.craftbukkit."+serverVersion+".inventory.CraftItemStack"));
            if(serverVersionGreaterEqualThan(ServerVersion.v1_17_R1)){

                version.addClass("ItemStackNMS",Class.forName("net.minecraft.world.item.ItemStack"));
                version.addClass("NBTTagCompound",Class.forName("net.minecraft.nbt.NBTTagCompound"));
                version.addClass("MojangsonParser",Class.forName("net.minecraft.nbt.MojangsonParser"));
                version.addClass("NBTBase",Class.forName("net.minecraft.nbt.NBTBase"));
                version.addClass("NBTTagList",Class.forName("net.minecraft.nbt.NBTTagList"));
            }else{

                version.addClass("ItemStackNMS",Class.forName("net.minecraft.server."+serverVersion+".ItemStack"));
                version.addClass("NBTTagCompound",Class.forName("net.minecraft.server."+serverVersion+".NBTTagCompound"));
                version.addClass("MojangsonParser",Class.forName("net.minecraft.server."+serverVersion+".MojangsonParser"));
                version.addClass("NBTBase",Class.forName("net.minecraft.server."+serverVersion+".NBTBase"));
                version.addClass("NBTTagList",Class.forName("net.minecraft.server."+serverVersion+".NBTTagList"));
            }

            version.addMethod("asNMSCopy",version.getClassRef("CraftItemStack").getMethod("asNMSCopy",ItemStack.class));
            version.addMethod("asBukkitCopy",version.getClassRef("CraftItemStack").getMethod("asBukkitCopy",version.getClassRef("ItemStackNMS")));
            if(!serverVersionGreaterEqualThan(ServerVersion.v1_13_R1)){

                version.addMethod("listSize",version.getClassRef("NBTTagList").getMethod("size"));
                version.addMethod("listGet",version.getClassRef("NBTTagList").getMethod("get",int.class));
                version.addMethod("listAdd",version.getClassRef("NBTTagList").getMethod("add",version.getClassRef("NBTBase")));
            }
            if(!serverVersionGreaterEqualThan(ServerVersion.v1_18_R1)){

                version.addMethod("hasTag",version.getClassRef("ItemStackNMS").getMethod("hasTag"));
                version.addMethod("getTag",version.getClassRef("ItemStackNMS").getMethod("getTag"));
                version.addMethod("setTag",version.getClassRef("ItemStackNMS").getMethod("setTag",version.getClassRef("NBTTagCompound")));
                version.addMethod("setString",version.getClassRef("NBTTagCompound").getMethod("setString",String.class,String.class));
                version.addMethod("setBoolean",version.getClassRef("NBTTagCompound").getMethod("setBoolean",String.class,boolean.class));
                version.addMethod("setDouble",version.getClassRef("NBTTagCompound").getMethod("setDouble",String.class,double.class));
                version.addMethod("setInt",version.getClassRef("NBTTagCompound").getMethod("setInt",String.class,int.class));
                version.addMethod("set",version.getClassRef("NBTTagCompound").getMethod("set",String.class,version.getClassRef("NBTBase")));
                version.addMethod("hasKey",version.getClassRef("NBTTagCompound").getMethod("hasKey",String.class));
                version.addMethod("getString",version.getClassRef("NBTTagCompound").getMethod("getString",String.class));
                version.addMethod("getBoolean",version.getClassRef("NBTTagCompound").getMethod("getBoolean",String.class));
                version.addMethod("getInt",version.getClassRef("NBTTagCompound").getMethod("getInt",String.class));
                version.addMethod("getDouble",version.getClassRef("NBTTagCompound").getMethod("getDouble",String.class));
                version.addMethod("getCompound",version.getClassRef("NBTTagCompound").getMethod("getCompound",String.class));
                version.addMethod("getList",version.getClassRef("NBTTagCompound").getMethod("getList",String.class,int.class));
                version.addMethod("get",version.getClassRef("NBTTagCompound").getMethod("get",String.class));
                version.addMethod("remove",version.getClassRef("NBTTagCompound").getMethod("remove",String.class));
                if(serverVersionGreaterEqualThan(ServerVersion.v1_13_R1)){
                    version.addMethod("getKeys",version.getClassRef("NBTTagCompound").getMethod("getKeys"));
                }else{
                    version.addMethod("getKeys",version.getClassRef("NBTTagCompound").getMethod("c"));
                }
                version.addMethod("hasKeyOfType",version.getClassRef("NBTTagCompound").getMethod("hasKeyOfType",String.class,int.class));
                version.addMethod("parse",version.getClassRef("MojangsonParser").getMethod("parse",String.class));
            }else{

                String hasTagMethod = switch(serverVersion){
                    case v1_18_R1 -> "r";
                    case v1_18_R2 -> "s";
                    case v1_19_R1, v1_19_R2, v1_19_R3 -> "t";
                    case v1_20_R1, v1_20_R2, v1_20_R3 -> "u";
                    default -> throw new NoSuchMethodException("Unsupported server version "+serverVersion);
                };
                version.addMethod("hasTag",version.getClassRef("ItemStackNMS").getMethod(hasTagMethod));

                String getTagMethod = switch(serverVersion){
                    case v1_18_R1 -> "s";
                    case v1_18_R2 -> "t";
                    case v1_19_R1, v1_19_R2, v1_19_R3 -> "u";
                    case v1_20_R1, v1_20_R2, v1_20_R3 -> "v";
                    default -> throw new NoSuchMethodException("Unsupported server version "+serverVersion);
                };
                version.addMethod("getTag",version.getClassRef("ItemStackNMS").getMethod(getTagMethod));

                version.addMethod("setTag",version.getClassRef("ItemStackNMS").getMethod("c",version.getClassRef("NBTTagCompound")));
                version.addMethod("setString",version.getClassRef("NBTTagCompound").getMethod("a",String.class,String.class));
                version.addMethod("setBoolean",version.getClassRef("NBTTagCompound").getMethod("a",String.class,boolean.class));
                version.addMethod("setDouble",version.getClassRef("NBTTagCompound").getMethod("a",String.class,double.class));
                version.addMethod("setInt",version.getClassRef("NBTTagCompound").getMethod("a",String.class,int.class));
                version.addMethod("set",version.getClassRef("NBTTagCompound").getMethod("a",String.class,version.getClassRef("NBTBase")));
                version.addMethod("hasKey",version.getClassRef("NBTTagCompound").getMethod("e",String.class));
                version.addMethod("getString",version.getClassRef("NBTTagCompound").getMethod("l",String.class));
                version.addMethod("getBoolean",version.getClassRef("NBTTagCompound").getMethod("q",String.class));
                version.addMethod("getInt",version.getClassRef("NBTTagCompound").getMethod("h",String.class));
                version.addMethod("getDouble",version.getClassRef("NBTTagCompound").getMethod("k",String.class));
                version.addMethod("getCompound",version.getClassRef("NBTTagCompound").getMethod("p",String.class));
                version.addMethod("getList",version.getClassRef("NBTTagCompound").getMethod("c",String.class,int.class));
                version.addMethod("get",version.getClassRef("NBTTagCompound").getMethod("c",String.class));
                version.addMethod("remove",version.getClassRef("NBTTagCompound").getMethod("r",String.class));

                String getKeysMethod = switch(serverVersion){
                    case v1_18_R1, v1_18_R2, v1_19_R1 -> "d";
                    case v1_19_R2, v1_19_R3, v1_20_R1, v1_20_R2, v1_20_R3 -> "e";
                    default -> throw new NoSuchMethodException("Unsupported server version "+serverVersion);
                };
                version.addMethod("getKeys",version.getClassRef("NBTTagCompound").getMethod(getKeysMethod));
                version.addMethod("hasKeyOfType",version.getClassRef("NBTTagCompound").getMethod("b",String.class,int.class));
                version.addMethod("parse",version.getClassRef("MojangsonParser").getMethod("a",String.class));
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            plugin.getLogger().log(Level.WARNING,"NMS operation failed",e);
        }
    }

    public ItemStack setTagStringItem(ItemStack item, String key, String value) {
        if(serverVersionGreaterEqualThan(ServerVersion.v1_20_R4)){
            ItemMeta meta = item.getItemMeta();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
            PersistentDataContainer p = meta.getPersistentDataContainer();
            p.set(namespacedKey, PersistentDataType.STRING,value);
            item.setItemMeta(meta);
            return item;
        }

        try {
            Object newItem = version.getMethodRef("asNMSCopy").invoke(null,item);
            Object compound = getNBTCompound(newItem);
            version.getMethodRef("setString").invoke(compound,key,value);
            version.getMethodRef("setTag").invoke(newItem,compound);
            return (ItemStack)version.getMethodRef("asBukkitCopy").invoke(null,newItem);
        } catch (IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING,"NMS operation failed",e);
        }
        return item;
    }

    public String getTagStringItem(ItemStack item,String key) {
        if(serverVersionGreaterEqualThan(ServerVersion.v1_20_R4)){
            ItemMeta meta = item.getItemMeta();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
            PersistentDataContainer p = meta.getPersistentDataContainer();
            if(p.has(namespacedKey)){
                return p.get(namespacedKey,PersistentDataType.STRING);
            }
            return null;
        }

        try {
            Object newItem = version.getMethodRef("asNMSCopy").invoke(null,item);
            Object compound = getNBTCompound(newItem);
            if((boolean)version.getMethodRef("hasKey").invoke(compound,key)){
                return (String)version.getMethodRef("getString").invoke(compound,key);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING,"NMS operation failed",e);
        }
        return null;
    }

    public ItemStack removeTagItem(ItemStack item, String key) {
        if(serverVersionGreaterEqualThan(ServerVersion.v1_20_R4)){
            ItemMeta meta = item.getItemMeta();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, key);

            PersistentDataContainer p = meta.getPersistentDataContainer();
            if(p.has(namespacedKey)){
                p.remove(namespacedKey);
            }
            item.setItemMeta(meta);
            return item;
        }

        try {
            Object newItem = version.getMethodRef("asNMSCopy").invoke(null,item);
            Object compound = getNBTCompound(newItem);
            version.getMethodRef("remove").invoke(compound,key);
            version.getMethodRef("setTag").invoke(newItem,compound);
            return (ItemStack)version.getMethodRef("asBukkitCopy").invoke(null,newItem);
        } catch (IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING,"NMS operation failed",e);
        }
        return item;
    }

    @SuppressWarnings("unchecked")
    public List<String> getNBT(ItemStack item){
        List<String> nbtList = new ArrayList<>();
        try {
            Object newItem = version.getMethodRef("asNMSCopy").invoke(null,item);
            if((boolean)version.getMethodRef("hasTag").invoke(newItem)){
                Object compound = version.getMethodRef("getTag").invoke(newItem);
                Set<String> tags = (Set<String>) version.getMethodRef("getKeys").invoke(compound);
                Set<String> notTags = new HashSet<>(Arrays.asList(
                        "ench", "HideFlags", "display", "SkullOwner", "AttributeModifiers", "Enchantments",
                        "Damage", "CustomModelData", "Potion", "StoredEnchantments", "CustomPotionColor",
                        "CustomPotionEffects", "Fireworks", "Explosion", "pages", "title", "author", "resolved",
                        "generation", "Trim", "custom_potion_effects", "CanPlaceOn", "CanDestroy"
                ));
                for(String t : tags) {
                    if(!notTags.contains(t)) {
                        if(t.equals("BlockEntityTag") && !item.getType().name().contains("SHULKER") && !item.getType().name().contains("CHEST")){
                            continue;
                        }
                        if((boolean)version.getMethodRef("hasKeyOfType").invoke(compound,t,1)) {

                            nbtList.add(t+"|"+version.getMethodRef("getBoolean").invoke(compound,t)+"|boolean");
                        }else if((boolean)version.getMethodRef("hasKeyOfType").invoke(compound,t,3)) {

                            nbtList.add(t+"|"+version.getMethodRef("getInt").invoke(compound,t)+"|int");
                        }else if((boolean)version.getMethodRef("hasKeyOfType").invoke(compound,t,6)) {

                            nbtList.add(t+"|"+version.getMethodRef("getDouble").invoke(compound,t)+"|double");
                        }else if((boolean)version.getMethodRef("hasKeyOfType").invoke(compound,t,10)){

                            nbtList.add(t+"|"+version.getMethodRef("getCompound").invoke(compound,t)+"|compound");
                        }else if((boolean)version.getMethodRef("hasKeyOfType").invoke(compound,t,8)) {

                            nbtList.add(t+"|"+version.getMethodRef("getString").invoke(compound,t));
                        }

                        else {

                            nbtList.add(t+"|"+version.getMethodRef("get").invoke(compound,t));
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING,"NMS operation failed",e);
        }
        return nbtList;
    }

    public ItemStack setNBT(ItemStack item, List<String> nbtList) {
        if(nbtList == null || nbtList.isEmpty()) {
            return item;
        }

        try {
            Object newItem = version.getMethodRef("asNMSCopy").invoke(null,item);
            Object compound = getNBTCompound(newItem);
            for(String nbt : nbtList) {
                String[] sep = nbt.split("\\|");
                String id = sep[0];
                String type = sep[sep.length-1];
                if(type.equals("list")) {
                    continue;
                }

                switch(type) {
                    case "boolean" -> version.getMethodRef("setBoolean").invoke(compound,id,Boolean.parseBoolean(sep[1]));
                    case "double" -> version.getMethodRef("setDouble").invoke(compound,id,Double.parseDouble(sep[1]));
                    case "int" -> version.getMethodRef("setInt").invoke(compound,id,Integer.parseInt(sep[1]));
                    case "compound" -> {
                        String finalNBT = nbt.replace(id+"|", "").replace("|compound", "");
                        Object compoundNew = version.getMethodRef("parse").invoke(null,finalNBT);
                        version.getMethodRef("set").invoke(compound,id,compoundNew);
                    }
                    default -> version.getMethodRef("setString").invoke(compound,id,nbt.replace(id+"|", ""));
                }
            }
            version.getMethodRef("setTag").invoke(newItem,compound);
            return (ItemStack)version.getMethodRef("asBukkitCopy").invoke(null,newItem);
        }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            plugin.getLogger().log(Level.WARNING,"NMS operation failed",e);
        }
        return null;
    }

    public List<String> getAttributes(ItemStack item) {
        try {
            Object newItem = version.getMethodRef("asNMSCopy").invoke(null,item);
            if((boolean)version.getMethodRef("hasTag").invoke(newItem)) {
                Object compound = version.getMethodRef("getTag").invoke(newItem);
                if((boolean)version.getMethodRef("hasKey").invoke(compound,"AttributeModifiers")){
                    List<String> attributeList = new ArrayList<>();
                    Object attributes = version.getMethodRef("getList").invoke(compound,"AttributeModifiers",10);
                    for(int i=0;i<(int)version.getMethodRef("listSize").invoke(attributes);i++){
                        Object compoundI = version.getMethodRef("listGet").invoke(attributes,i);
                        String attributeName = (String)version.getMethodRef("getString").invoke(compoundI,"AttributeName");
                        String name = (String)version.getMethodRef("getString").invoke(compoundI,"Name");
                        double amount = (double)version.getMethodRef("getDouble").invoke(compoundI,"Amount");
                        int operation = (int)version.getMethodRef("getInt").invoke(compoundI,"Operation");
                        int uuidLeast = (int)version.getMethodRef("getInt").invoke(compoundI,"UUIDLeast");
                        int uuidMost = (int)version.getMethodRef("getInt").invoke(compoundI,"UUIDMost");
                        String all = attributeName+";"+name+";"+amount+";"+operation+";"+uuidLeast+";"+uuidMost;
                        if((boolean)version.getMethodRef("hasKey").invoke(compoundI,"Slot")) {
                            all = all+";"+version.getMethodRef("getString").invoke(compoundI,"Slot");
                        }

                        attributeList.add(all);
                    }

                    return attributeList;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING,"NMS operation failed",e);
        }
        return null;
    }

    public ItemStack setAttributes(ItemStack item, List<String> attributeList) {
        try{
            Object newItem = version.getMethodRef("asNMSCopy").invoke(null,item);
            Object compound = getNBTCompound(newItem);
            Object attributesList = version.getClassRef("NBTTagList").getDeclaredConstructor().newInstance();
            for(String attribute : attributeList){
                Object attributeCompound = version.getClassRef("NBTTagCompound").getDeclaredConstructor().newInstance();

                String[] data = attribute.split(";");
                String attributeName = data[0];
                String name = data[1];
                double amount = Double.parseDouble(data[2]);
                int operation = Integer.parseInt(data[3]);
                int uuidLeast = Integer.parseInt(data[4]);
                int uuidMost = Integer.parseInt(data[5]);

                version.getMethodRef("setString").invoke(attributeCompound,"AttributeName",attributeName);
                version.getMethodRef("setString").invoke(attributeCompound,"Name",name);
                version.getMethodRef("setDouble").invoke(attributeCompound,"Amount",amount);
                version.getMethodRef("setInt").invoke(attributeCompound,"Operation",operation);
                version.getMethodRef("setInt").invoke(attributeCompound,"UUIDLeast",uuidLeast);
                version.getMethodRef("setInt").invoke(attributeCompound,"UUIDMost",uuidMost);
                if(data.length >= 7) {
                    String slot = data[6];
                    version.getMethodRef("setString").invoke(attributeCompound,"Slot",slot);
                }
                version.getMethodRef("listAdd").invoke(attributesList,attributeCompound);
            }

            version.getMethodRef("set").invoke(compound,"AttributeModifiers",attributesList);
            version.getMethodRef("setTag").invoke(newItem,compound);

            return (ItemStack)version.getMethodRef("asBukkitCopy").invoke(null,newItem);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            plugin.getLogger().log(Level.WARNING,"NMS operation failed",e);
        }

        return null;
    }

    private Object getNBTCompound(Object newItem){
        try {
            boolean hasTag = (boolean)version.getMethodRef("hasTag").invoke(newItem);
            return hasTag ? version.getMethodRef("getTag").invoke(newItem) :
                    version.getClassRef("NBTTagCompound").getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            plugin.getLogger().log(Level.WARNING,"NMS operation failed",e);
        }
        return null;
    }

    private boolean serverVersionGreaterEqualThan(ServerVersion version){
        return serverVersion.ordinal() >= version.ordinal();
    }
}
