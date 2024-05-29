package pk.ajneb97.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.net.URL;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.item.*;

public class ItemUtils {


	@SuppressWarnings("deprecation")
	public static ItemStack createItemFromID(String id) {
		String[] idsplit = new String[2];
		int DataValue = 0;
		ItemStack stack = null;
		if(id.contains(":")){
			  idsplit = id.split(":");
			  String stringDataValue = idsplit[1];
			  DataValue = Integer.valueOf(stringDataValue);
			  Material mat = Material.getMaterial(idsplit[0].toUpperCase()); 
			  stack = new ItemStack(mat,1,(short)DataValue);	             	  
		}else{
			  Material mat = Material.getMaterial(id.toUpperCase());
			  stack = new ItemStack(mat,1);	  			  
		}
		return stack;
	}
	
	public static ItemStack setTagStringItem(PlayerKits2 plugin, ItemStack item, String key, String value) {
		return plugin.getNmsManager().setTagStringItem(item,key,value);
	}
	
	public static String getTagStringItem(PlayerKits2 plugin, ItemStack item, String key) {
		if(item == null || item.getType().equals(Material.AIR)){
			return null;
		}
		return plugin.getNmsManager().getTagStringItem(item,key);
	}

	public static ItemStack removeTagItem(PlayerKits2 plugin, ItemStack item, String key) {
		return plugin.getNmsManager().removeTagItem(item,key);
	}
	
	public static List<String> getNBT(PlayerKits2 plugin, ItemStack item){
		return plugin.getNmsManager().getNBT(item);
	}
	
	public static ItemStack setNBT(PlayerKits2 plugin, ItemStack item, List<String> nbtList){
		return plugin.getNmsManager().setNBT(item,nbtList);
	}
	
	public static KitItemSkullData getSkullData(ItemStack item) {
		KitItemSkullData kitItemSkullData = null;
		String owner = null;
		String texture = null;
		String id = null;
		
		String typeName = item.getType().name();
		if(!typeName.equals("PLAYER_HEAD") && !typeName.equals("SKULL_ITEM")) {
			return null;
		}
		
		SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
		Field profileField;
		try {
			profileField = skullMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);

			GameProfile gameProfile = (GameProfile) profileField.get(skullMeta);
			if(gameProfile != null && gameProfile.getProperties() != null) {
				PropertyMap propertyMap = gameProfile.getProperties();
				owner = gameProfile.getName();
				if(gameProfile.getId() != null) {
					id = gameProfile.getId().toString();
				}

				ServerVersion serverVersion = PlayerKits2.serverVersion;
				for(Property p : propertyMap.values()) {
					if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)){
						String pName = (String)p.getClass().getMethod("name").invoke(p);
						if(pName.equals("textures")){
							texture = (String)p.getClass().getMethod("value").invoke(p);
						}
					}else{
						if(p.getName().equals("textures")) {
							texture = p.getValue();
						}
					}
				}
			}
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
				| InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}

        if(texture != null || id != null || owner != null) {
			kitItemSkullData = new KitItemSkullData(owner,texture,id);
		}
		
		return kitItemSkullData;
	}

	public static void setSkullData(ItemStack item, KitItemSkullData skullData, Player player){
		String typeName = item.getType().name();
		if(!typeName.equals("PLAYER_HEAD") && !typeName.equals("SKULL_ITEM")) {
			return;
		}
		
		if(skullData == null) {
			return;
		}
		
		String texture = skullData.getTexture();
		String owner = skullData.getOwner();
		if(owner != null && player != null) {
			owner = owner.replace("%player%", player.getName());
		}
		String id = skullData.getId();
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        if(owner != null) {
        	skullMeta.setOwner(owner);
        }

		if(texture != null){
			ServerVersion serverVersion = PlayerKits2.serverVersion;
			if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)){
				UUID uuid = id != null ? UUID.fromString(id) : UUID.randomUUID();
				PlayerProfile profile = Bukkit.createPlayerProfile(uuid);
				PlayerTextures textures = profile.getTextures();
				URL url;
				try {
					String decoded = new String(Base64.getDecoder().decode(texture));
					String decodedFormatted = decoded.replaceAll("\\s", "");
					JsonObject jsonObject = new Gson().fromJson(decodedFormatted, JsonObject.class);
					String urlText = jsonObject.get("textures").getAsJsonObject().get("SKIN")
							.getAsJsonObject().get("url").getAsString();

					url = new URL(urlText);
				} catch (Exception error) {
					error.printStackTrace();
					return;
				}
				textures.setSkin(url);
				profile.setTextures(textures);
				skullMeta.setOwnerProfile(profile);
			}else{
				GameProfile profile = null;
				if(id == null) {
					profile = new GameProfile(UUID.randomUUID(), owner != null ? owner : "");
				}else {
					profile = new GameProfile(UUID.fromString(id), owner != null ? owner : "");
				}
				profile.getProperties().put("textures", new Property("textures", texture));

				try {
					Field profileField = skullMeta.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					profileField.set(skullMeta, profile);
				} catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
					error.printStackTrace();
				}
			}
		}

        item.setItemMeta(skullMeta);
	}
	
	public static KitItemPotionData getPotionData(ItemStack item) {
		KitItemPotionData potionData = null;
		List<String> potionEffectsList = new ArrayList<String>();
		boolean upgraded = false;
		boolean extended = false;
		String potionType = null;
		int potionColor = 0;
		
		String typeName = item.getType().name();
		if(!typeName.contains("POTION") && !typeName.equals("TIPPED_ARROW")) {
			return null;
		}
		
		if(!(item.getItemMeta() instanceof PotionMeta)) {
			return null;
		}
		
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		if(meta.hasCustomEffects()) {
			List<PotionEffect> potionEffects = meta.getCustomEffects();
			for(int i=0;i<potionEffects.size();i++) {
				String type = potionEffects.get(i).getType().getName();
				int amplifier = potionEffects.get(i).getAmplifier();
				int duration = potionEffects.get(i).getDuration();
				potionEffectsList.add(type+";"+amplifier+";"+duration);
			}
		}
		ServerVersion serverVersion = PlayerKits2.serverVersion;
		if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_9_R1)) {
			if(meta.hasColor()) {
				potionColor = meta.getColor().asRGB();
			}

			if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)) {
				if(meta.getBasePotionType() != null){
					potionType = meta.getBasePotionType().name();
				}
			}else{
				PotionData basePotionData = meta.getBasePotionData();
				extended = basePotionData.isExtended();
				upgraded = basePotionData.isUpgraded();
				potionType = basePotionData.getType().name();
			}
		}
		
		potionData = new KitItemPotionData(upgraded,extended,potionType,potionColor,potionEffectsList);
		
		return potionData;
	}
	
	public static void setPotionData(ItemStack item,KitItemPotionData potionData){
		String typeName = item.getType().name();
		if(!typeName.contains("POTION") && !typeName.equals("TIPPED_ARROW")) {
			return;
		}
		
		if(potionData == null) {
			return;
		}
		
		if(!(item.getItemMeta() instanceof PotionMeta)) {
			return;
		}
		
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		
		List<String> potionEffects = potionData.getPotionEffects();
		if(potionEffects != null) {
			for(int i=0;i<potionEffects.size();i++) {
				String[] sep = potionEffects.get(i).split(";");
				String type = sep[0];
				int amplifier = Integer.valueOf(sep[1]);
				int duration = Integer.valueOf(sep[2]);
				meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(type),duration,amplifier), false);
			}
		}

		ServerVersion serverVersion = PlayerKits2.serverVersion;
		if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_9_R1)) {
			int color = potionData.getPotionColor();
			if(color != 0) {
				meta.setColor(Color.fromRGB(color));
			}
			if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)) {
				if(potionData.getPotionType() != null && !potionData.getPotionType().isEmpty()){
					meta.setBasePotionType(PotionType.valueOf(potionData.getPotionType()));
				}
			}else{
				PotionData basePotionData = new PotionData(
						PotionType.valueOf(potionData.getPotionType()),
						potionData.isExtended(),
						potionData.isUpgraded());

				meta.setBasePotionData(basePotionData);
			}
		}
		
		item.setItemMeta(meta);
	}
	
	public static KitItemBannerData getBannerData(ItemStack item) {
		KitItemBannerData bannerData = null;
		List<String> bannerPatterns = new ArrayList<String>();
		String baseColor = null;
		
		List<Pattern> patterns = new ArrayList<Pattern>();
		
		String typeName = item.getType().name();
		if(typeName.contains("BANNER") || typeName.contains("PATTERN")) {
			if(item.getItemMeta() instanceof BannerMeta) {
				BannerMeta meta = (BannerMeta) item.getItemMeta();
				patterns = meta.getPatterns();
			}
		}else if(typeName.equals("SHIELD")) {
			BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
			if(meta.hasBlockState()) {
				Banner banner = (Banner) meta.getBlockState();
				patterns = banner.getPatterns();
				if(OtherUtils.isLegacy()) {
					baseColor = banner.getBaseColor().name();
				}else {
					baseColor = banner.getType().name();
				}
			}
		}else {
			return null;
		}
		
		for(Pattern p : patterns) {
			bannerPatterns.add(p.getColor().name()+";"+p.getPattern().name());
		}
		
		if(bannerPatterns.isEmpty() && baseColor == null) {
			return null;
		}
		
		bannerData = new KitItemBannerData(bannerPatterns,baseColor);
		return bannerData;
	}
	
	public static void setBannerData(ItemStack item,KitItemBannerData bannerData){
		String typeName = item.getType().name();

		if(bannerData == null) {
			return;
		}
		
		List<String> bannerPatterns = bannerData.getPatterns();
		String baseColor = bannerData.getBaseColor();
		
		if(typeName.contains("BANNER") || typeName.contains("PATTERN")) {
			BannerMeta meta = (BannerMeta) item.getItemMeta();
			if(bannerPatterns != null) {
				for(String pattern : bannerPatterns) {
					String[] patternSplit = pattern.split(";");
					String patternColor = patternSplit[0];
					String patternName = patternSplit[1];
					meta.addPattern(new Pattern(DyeColor.valueOf(patternColor),PatternType.valueOf(patternName)));
				}
				item.setItemMeta(meta);
			}
		}else if(typeName.equals("SHIELD")) {
			BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
			Banner banner = (Banner) meta.getBlockState();
			if(OtherUtils.isLegacy()) {
				banner.setBaseColor(DyeColor.valueOf(baseColor));
			}else {
				String fixedColor = baseColor.replace("_BANNER", "");
				banner.setBaseColor(DyeColor.valueOf(fixedColor));
			}
			if(bannerPatterns != null) {
				for(String pattern : bannerPatterns) {
					String[] patternSplit = pattern.split(";");
					String patternColor = patternSplit[0];
					String patternName = patternSplit[1];
					banner.addPattern(new Pattern(DyeColor.valueOf(patternColor),PatternType.valueOf(patternName)));
				}
			}
			banner.update();
			meta.setBlockState(banner);
			item.setItemMeta(meta);
		}
	}
	
	public static KitItemFireworkData getFireworkData(ItemStack item) {
		KitItemFireworkData fireworkData = null;
		
		List<String> fireworkRocketEffects = new ArrayList<String>();
		String fireworkStarEffect = null;
		int fireworkPower = 0;
		
		String typeName = item.getType().name();
		boolean isFireworkCharge = false;
		List<FireworkEffect> effects = new ArrayList<FireworkEffect>();
		
		if(typeName.equals("FIREWORK") || typeName.equals("FIREWORK_ROCKET")) {
			FireworkMeta meta = (FireworkMeta) item.getItemMeta();
			fireworkPower = meta.getPower();
			effects = meta.getEffects();
		}else if(typeName.equals("FIREWORK_STAR") || typeName.equals("FIREWORK_CHARGE")) {
			FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
			FireworkEffect effect = meta.getEffect();
			effects.add(effect);
			isFireworkCharge = true;
		}else {
			return null;
		}
		
		for(FireworkEffect e : effects) {
			if(e == null) {
				continue;
			}
			String line = e.getType().name()+";";
			List<Color> colors = e.getColors();
			String colorsLine = "";
			for(int i=0;i<colors.size();i++) {
				if(colors.size() <= (i+1)) {
					colorsLine = colorsLine+colors.get(i).asRGB()+";";
				}else {
					colorsLine = colorsLine+colors.get(i).asRGB()+",";
				}
			}
			List<Color> fadeColors = e.getFadeColors();
			String fadeColorsLine = "";
			for(int i=0;i<fadeColors.size();i++) {
				if(fadeColors.size() <= (i+1)) {
					fadeColorsLine = fadeColorsLine+fadeColors.get(i).asRGB();
				}else {
					fadeColorsLine = fadeColorsLine+fadeColors.get(i).asRGB()+",";
				}
			}
			line = line+colorsLine+fadeColorsLine+";"+e.hasFlicker()+";"+e.hasTrail();
			
			if(isFireworkCharge) {
				fireworkStarEffect = line;
				break;
			}
			
			fireworkRocketEffects.add(line);
		}
		
		fireworkData = new KitItemFireworkData(fireworkRocketEffects,fireworkStarEffect,fireworkPower);
		
		return fireworkData;
	}
	
	public static void setFireworkData(ItemStack item,KitItemFireworkData fireworkData){
		String typeName = item.getType().name();

		if(fireworkData == null) {
			return;
		}
		
		int power = fireworkData.getFireworkPower();
		List<String> rocketEffects = fireworkData.getFireworkRocketEffects();
		String starEffect = fireworkData.getFireworkStarEffect();
		
		if(typeName.equals("FIREWORK") || typeName.equals("FIREWORK_ROCKET")) {
			FireworkMeta meta = (FireworkMeta) item.getItemMeta();
			if(rocketEffects != null) {
				for(int i=0;i<rocketEffects.size();i++) {
					FireworkEffect effect = getFireworkEffect(rocketEffects.get(i));
					meta.addEffect(effect);
				}
			}
			meta.setPower(power);
			item.setItemMeta(meta);
		}else if(typeName.equals("FIREWORK_STAR") || typeName.equals("FIREWORK_CHARGE")) {
			FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
			if(starEffect != null) {
				meta.setEffect(getFireworkEffect(starEffect));
			}
			item.setItemMeta(meta);
		}
	}
	
	private static FireworkEffect getFireworkEffect(String line) {
		String[] sep = line.split(";");
		String type = sep[0];
		String[] colors = sep[1].split(",");
		List<Color> colorsList = new ArrayList<Color>();
		for(int c=0;c<colors.length;c++) {
			colorsList.add(Color.fromRGB(Integer.valueOf(colors[c])));
		}
		List<Color> fadeColorsList = new ArrayList<Color>();
		if(!sep[2].equals("")) {
			String[] fadeColors = sep[2].split(",");
			for(int c=0;c<fadeColors.length;c++) {
				fadeColorsList.add(Color.fromRGB(Integer.valueOf(fadeColors[c])));
			} 
		}

		boolean flicker = Boolean.valueOf(sep[3]);
		boolean trail = Boolean.valueOf(sep[4]);
		return FireworkEffect.builder().flicker(flicker).trail(trail).with(Type.valueOf(type))
				.withColor(colorsList).withFade(fadeColorsList).build();
	}
	
	public static List<String> getAttributes(PlayerKits2 plugin, ItemStack item){
		if(OtherUtils.isLegacy()) {
			return plugin.getNmsManager().getAttributes(item);
		}else {
			//1.13+
			ItemMeta meta = item.getItemMeta();
			if(meta.hasAttributeModifiers()) {
				Multimap<Attribute,AttributeModifier> attributes = meta.getAttributeModifiers();
				Set<Attribute> set = attributes.keySet();

				List<String> attributeList = new ArrayList<String>();
				for(Attribute a : set) {
					Collection<AttributeModifier> listModifiers = attributes.get(a);
					for(AttributeModifier m : listModifiers) {
						String line = a.name()+";"+m.getOperation().name()+";"+m.getAmount()+";"+m.getUniqueId();
						if(m.getSlot() != null) {
							line=line+";"+m.getSlot().name();
						}
						line=line+";custom_name:"+m.getName();
						attributeList.add(line);
					}
				}
				
				return attributeList;
			}
		}
		return null;
	}
	
	public static ItemStack setAttributes(PlayerKits2 plugin, ItemStack item, List<String> attributes) {
		if(attributes == null) {
			return item;
		}
		
		if(OtherUtils.isLegacy()) {
			return plugin.getNmsManager().setAttributes(item,attributes);
		}else {
			//1.13+
			ItemMeta meta = item.getItemMeta();
			for(String a : attributes) {
				String[] sep = a.split(";");
				String attribute = sep[0];
				AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(sep[1]);
				double amount = Double.valueOf(sep[2]);
				UUID uuid = UUID.fromString(sep[3]);
				String customName = attribute;
				for(int i=0;i<sep.length;i++){
					if(sep[i].startsWith("custom_name:")){
						customName = sep[i].replace("custom_name:","");
					}
				}
				AttributeModifier modifier = null;
				if(sep.length >= 5) {
					if(!sep[4].startsWith("custom_name:")){
						EquipmentSlot slot = EquipmentSlot.valueOf(sep[4]);
						modifier = new AttributeModifier(uuid,customName,amount,op,slot);
					}else{
						modifier = new AttributeModifier(uuid,customName,amount,op);
					}
				}else {
					modifier = new AttributeModifier(uuid,customName,amount,op);
				}

				meta.addAttributeModifier(Attribute.valueOf(attribute), modifier);
			}
			
			item.setItemMeta(meta);
		}
		return item;
	}
	
	public static KitItemBookData getBookData(ItemStack item){
		KitItemBookData bookData = null;
		String typeName = item.getType().name();
		
		List<String> pages = new ArrayList<String>();
		String author = null;
		String generation = null;
		String title = null;

		if(typeName.equals("WRITTEN_BOOK")) {
			BookMeta meta = (BookMeta) item.getItemMeta();
			
			author = meta.getAuthor();
			title = meta.getTitle();
			
			if(!Bukkit.getVersion().contains("1.12") && OtherUtils.isLegacy()) {
				pages = new ArrayList<String>(meta.getPages());
			}else {
				if(meta.getGeneration() != null) {
					generation = meta.getGeneration().name();
				}
				for(BaseComponent[] page : meta.spigot().getPages()) {
					pages.add(ComponentSerializer.toString(page));
				}
			}
			
			bookData = new KitItemBookData(pages,author,generation,title);
			return bookData;
		}else {
			return null;
		}
	}
	
	public static void setBookData(ItemStack item,KitItemBookData bookData){
		String typeName = item.getType().name();

		if(bookData == null) {
			return;
		}
		
		String author = bookData.getAuthor();
		String generation = bookData.getGeneration();
		String title = bookData.getTitle();
		List<String> pages = bookData.getPages();
		
		if(typeName.equals("WRITTEN_BOOK")) {
			BookMeta meta = (BookMeta) item.getItemMeta();
			if(!Bukkit.getVersion().contains("1.12") && OtherUtils.isLegacy()) {
				meta.setPages(new ArrayList<String>(pages));
			}else {
				ArrayList<BaseComponent[]> pagesBaseComponent = new ArrayList<BaseComponent[]>();
				for(String page : pages) {
					pagesBaseComponent.add(ComponentSerializer.parse(page));
				}
				meta.spigot().setPages(pagesBaseComponent);
				if(generation != null) {
					meta.setGeneration(Generation.valueOf(generation));
				}
			}
			meta.setAuthor(author);
			meta.setTitle(title);
			
			item.setItemMeta(meta);
		}
	}

	public static KitItemTrimData getArmorTrimData(ItemStack item) {
		if(!OtherUtils.isTrimNew()) {
			return null;
		}

		String armorTrimPattern = null;
		String armorTrimMaterial = null;
		if(item.getItemMeta() instanceof ArmorMeta) {
			ArmorMeta meta = (ArmorMeta) item.getItemMeta();
			if(meta.hasTrim()){
				ArmorTrim armorTrim = meta.getTrim();
				armorTrimPattern = armorTrim.getPattern().getKey().getKey();
				armorTrimMaterial = armorTrim.getMaterial().getKey().getKey();
			}else{
				return null;
			}
		}else{
			return null;
		}

		return new KitItemTrimData(armorTrimPattern,armorTrimMaterial);
	}

	public static void setArmorTrimData(ItemStack item,KitItemTrimData trimData){
		if(trimData == null || !OtherUtils.isTrimNew()) {
			return;
		}

		String pattern = trimData.getPattern();
		String material = trimData.getMaterial();

		if(item.getItemMeta() instanceof ArmorMeta) {
			ArmorMeta meta = (ArmorMeta) item.getItemMeta();
			if(pattern != null && material != null){
				ArmorTrim armorTrim = new ArmorTrim(
						Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(material.toLowerCase())),
						Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(pattern.toLowerCase()))
				);
				meta.setTrim(armorTrim);
				item.setItemMeta(meta);
			}
		}
	}
}
