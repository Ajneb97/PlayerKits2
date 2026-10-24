package pk.ajneb97.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

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
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
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
		if(id.contains(":")){
			String[] idSplit = id.split(":");
			int dataValue = Integer.parseInt(idSplit[1]);
			return new ItemStack(getMaterial(idSplit[0]),1,(short)dataValue);
		}
		return new ItemStack(getMaterial(id),1);
	}

	private static Material getMaterial(String name) {
		Material material = Material.getMaterial(name.toUpperCase());
		if(material == null) {
			throw new IllegalArgumentException("Unknown material: "+name);
		}
		return material;
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

	@SuppressWarnings("deprecation")
	public static KitItemSkullData getSkullData(ItemStack item) {
		String owner = null;
		String texture = null;
		String id = null;

		String typeName = item.getType().name();
		if(!typeName.equals("PLAYER_HEAD") && !typeName.equals("SKULL_ITEM")) {
			return null;
		}

		if(!(item.getItemMeta() instanceof SkullMeta skullMeta)) {
			return null;
		}
		ServerVersion serverVersion = PlayerKits2.serverVersion;
		if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R1)){
			PlayerProfile profile = skullMeta.getOwnerProfile();
			if(profile != null){
				owner = profile.getName();
				if(profile.getUniqueId() != null){
					id = profile.getUniqueId().toString();
				}
				URL skin = profile.getTextures().getSkin();
				if(skin != null){
					JsonObject skinJsonObject = new JsonObject();
					skinJsonObject.addProperty("url", skin.toString());
					JsonObject texturesJsonObject = new JsonObject();
					texturesJsonObject.add("SKIN", skinJsonObject);
					JsonObject minecraftTexturesJsonObject = new JsonObject();
					minecraftTexturesJsonObject.add("textures", texturesJsonObject);
					texture = new String(Base64.getEncoder().encode(minecraftTexturesJsonObject.toString().getBytes()));
				}
			}
		}else{
			try {
				Field profileField = skullMeta.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);

				GameProfile gameProfile = (GameProfile) profileField.get(skullMeta);
				if(gameProfile != null && gameProfile.getProperties() != null) {
					PropertyMap propertyMap = gameProfile.getProperties();
					owner = gameProfile.getName();
					if(gameProfile.getId() != null) {
						id = gameProfile.getId().toString();
					}

					for(Property property : propertyMap.values()) {
						String propertyName = (String) invokeFirstMethod(property,"name","getName");
						if("textures".equals(propertyName)){
							texture = (String) invokeFirstMethod(property,"value","getValue");
						}
					}
				}
			} catch (ReflectiveOperationException | SecurityException | IllegalArgumentException e) {
				JavaPlugin.getPlugin(PlayerKits2.class).getLogger().log(Level.WARNING,"Could not read skull data",e);
			}
		}

		if(texture != null || id != null || owner != null) {
			return new KitItemSkullData(owner,texture,id);
		}
		return null;
	}

	private static Object invokeFirstMethod(Object target, String... methodNames) throws ReflectiveOperationException {
		NoSuchMethodException lastError = null;
		for(String methodName : methodNames) {
			try {
				return target.getClass().getMethod(methodName).invoke(target);
			} catch (NoSuchMethodException error) {
				lastError = error;
			}
		}
		throw lastError != null ? lastError : new NoSuchMethodException();
	}

	@SuppressWarnings("deprecation")
	public static void setSkullData(ItemStack item, KitItemSkullData skullData, Player player){
		String typeName = item.getType().name();
		if(!typeName.equals("PLAYER_HEAD") && !typeName.equals("SKULL_ITEM")) {
			return;
		}

		if(skullData == null) {
			return;
		}

		if(!(item.getItemMeta() instanceof SkullMeta skullMeta)) {
			return;
		}

		String texture = skullData.texture();
		String owner = skullData.owner();
		if(owner != null && player != null) {
			owner = owner.replace("%player%", player.getName());
		}
		String id = skullData.id();
		if(owner != null) {
			skullMeta.setOwner(owner);
		}

		if(texture != null){
			ServerVersion serverVersion = PlayerKits2.serverVersion;
			if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)){
				UUID uuid = id != null ? UUID.fromString(id) : UUID.randomUUID();
				PlayerProfile profile = Bukkit.createPlayerProfile(uuid,"playerkits");
				PlayerTextures textures = profile.getTextures();
				URL url;
				try {
					String decoded = new String(Base64.getDecoder().decode(texture));
					String decodedFormatted = decoded.replaceAll("\\s", "");
					JsonObject jsonObject = new Gson().fromJson(decodedFormatted, JsonObject.class);
					String urlText = jsonObject.get("textures").getAsJsonObject().get("SKIN")
							.getAsJsonObject().get("url").getAsString();

					url = URI.create(urlText).toURL();
				} catch (Exception error) {
					JavaPlugin.getPlugin(PlayerKits2.class).getLogger().log(Level.WARNING,"Could not read skull texture",error);
					return;
				}
				textures.setSkin(url);
				profile.setTextures(textures);
				skullMeta.setOwnerProfile(profile);
			}else{
				UUID uuid = id == null ? UUID.randomUUID() : UUID.fromString(id);
				GameProfile profile = new GameProfile(uuid, owner != null ? owner : "");
				profile.getProperties().put("textures", new Property("textures", texture));

				try {
					Field profileField = skullMeta.getClass().getDeclaredField("profile");
					profileField.setAccessible(true);
					profileField.set(skullMeta, profile);
				} catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
					JavaPlugin.getPlugin(PlayerKits2.class).getLogger().log(Level.WARNING,"Could not set skull texture",error);
				}
			}
		}

		item.setItemMeta(skullMeta);
	}

	@SuppressWarnings("deprecation")
	public static KitItemPotionData getPotionData(ItemStack item) {
		List<String> potionEffectsList = new ArrayList<>();
		boolean upgraded = false;
		boolean extended = false;
		String potionType = null;
		int potionColor = 0;

		String typeName = item.getType().name();
		if(!typeName.contains("POTION") && !typeName.equals("TIPPED_ARROW")) {
			return null;
		}

		if(!(item.getItemMeta() instanceof PotionMeta meta)) {
			return null;
		}

		if(meta.hasCustomEffects()) {
			for(PotionEffect potionEffect : meta.getCustomEffects()) {
				potionEffectsList.add(potionEffect.getType().getName()+";"+potionEffect.getAmplifier()+";"+potionEffect.getDuration());
			}
		}
		ServerVersion serverVersion = PlayerKits2.serverVersion;
		if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_9_R1)) {
			Color color = meta.getColor();
			if(color != null) {
				potionColor = color.asRGB();
			}

			if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)) {
				PotionType basePotionType = meta.getBasePotionType();
				if(basePotionType != null){
					potionType = basePotionType.name();
				}
			}else{
				try {
					Object basePotionData = PotionMeta.class.getMethod("getBasePotionData").invoke(meta);
					if(basePotionData != null) {
						Class<?> potionDataClass = basePotionData.getClass();
						extended = (boolean) potionDataClass.getMethod("isExtended").invoke(basePotionData);
						upgraded = (boolean) potionDataClass.getMethod("isUpgraded").invoke(basePotionData);
						Object basePotionType = potionDataClass.getMethod("getType").invoke(basePotionData);
						if(basePotionType instanceof PotionType type) {
							potionType = type.name();
						}
					}
				} catch (ReflectiveOperationException error) {
					JavaPlugin.getPlugin(PlayerKits2.class).getLogger().log(Level.WARNING,"Could not read legacy potion data",error);
				}
			}
		}

		return new KitItemPotionData(upgraded,extended,potionType,potionColor,potionEffectsList);
	}

	@SuppressWarnings("deprecation")
	public static void setPotionData(ItemStack item,KitItemPotionData potionData){
		String typeName = item.getType().name();
		if(!typeName.contains("POTION") && !typeName.equals("TIPPED_ARROW")) {
			return;
		}

		if(potionData == null) {
			return;
		}

		if(!(item.getItemMeta() instanceof PotionMeta meta)) {
			return;
		}

		List<String> potionEffects = potionData.potionEffects();
		if(potionEffects != null) {
			for(String potionEffect : potionEffects) {
				String[] sep = potionEffect.split(";");
				PotionEffectType type = PotionEffectType.getByName(sep[0]);
				if(type == null) {
					continue;
				}
				int amplifier = Integer.parseInt(sep[1]);
				int duration = Integer.parseInt(sep[2]);
				meta.addCustomEffect(new PotionEffect(type,duration,amplifier), false);
			}
		}

		ServerVersion serverVersion = PlayerKits2.serverVersion;
		if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_9_R1)) {
			int color = potionData.potionColor();
			if(color != 0) {
				meta.setColor(Color.fromRGB(color));
			}
			String potionType = potionData.potionType();
			if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)) {
				if(potionType != null && !potionType.isEmpty()){
					meta.setBasePotionType(PotionType.valueOf(potionType));
				}
			}else if(potionType != null && !potionType.isEmpty()){
				try {
					Class<?> potionDataClass = Class.forName("org.bukkit.potion.PotionData");
					Object basePotionData = potionDataClass.getConstructor(PotionType.class,boolean.class,boolean.class)
							.newInstance(PotionType.valueOf(potionType),potionData.extended(),potionData.upgraded());
					for(Method method : PotionMeta.class.getMethods()) {
						if(method.getName().equals("setBasePotionData") && method.getParameterCount() == 1) {
							method.invoke(meta,basePotionData);
							break;
						}
					}
				} catch (ReflectiveOperationException error) {
					JavaPlugin.getPlugin(PlayerKits2.class).getLogger().log(Level.WARNING,"Could not set legacy potion data",error);
				}
			}
		}

		item.setItemMeta(meta);
	}

	public static KitItemBannerData getBannerData(ItemStack item) {
		List<String> bannerPatterns = new ArrayList<>();
		String baseColor = null;

		List<Pattern> patterns = new ArrayList<>();

		String typeName = item.getType().name();
		if(typeName.contains("BANNER") || typeName.contains("PATTERN")) {
			if(item.getItemMeta() instanceof BannerMeta meta) {
				patterns = meta.getPatterns();
			}
		}else if(typeName.equals("SHIELD")) {
			if(item.getItemMeta() instanceof BlockStateMeta meta && meta.hasBlockState()
					&& meta.getBlockState() instanceof Banner banner) {
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
			bannerPatterns.add(p.getColor().name()+";"+getBannerPatternName(p));
		}

		if(bannerPatterns.isEmpty() && baseColor == null) {
			return null;
		}

		return new KitItemBannerData(bannerPatterns,baseColor);
	}

	public static void setBannerData(ItemStack item,KitItemBannerData bannerData){
		String typeName = item.getType().name();

		if(bannerData == null) {
			return;
		}

		List<String> bannerPatterns = bannerData.patterns();
		String baseColor = bannerData.baseColor();

		if(typeName.contains("BANNER") || typeName.contains("PATTERN")) {
			if(bannerPatterns != null && item.getItemMeta() instanceof BannerMeta meta) {
				for(String pattern : bannerPatterns) {
					String[] patternSplit = pattern.split(";");
					meta.addPattern(new Pattern(DyeColor.valueOf(patternSplit[0]),getBannerPatternByName(patternSplit[1])));
				}
				item.setItemMeta(meta);
			}
		}else if(typeName.equals("SHIELD")) {
			if(!(item.getItemMeta() instanceof BlockStateMeta meta) || !(meta.getBlockState() instanceof Banner banner)) {
				return;
			}
			if(baseColor != null) {
				String fixedColor = OtherUtils.isLegacy() ? baseColor : baseColor.replace("_BANNER", "");
				banner.setBaseColor(DyeColor.valueOf(fixedColor));
			}
			if(bannerPatterns != null) {
				for(String pattern : bannerPatterns) {
					String[] patternSplit = pattern.split(";");
					banner.addPattern(new Pattern(DyeColor.valueOf(patternSplit[0]),getBannerPatternByName(patternSplit[1])));
				}
			}
			banner.update();
			meta.setBlockState(banner);
			item.setItemMeta(meta);
		}
	}

	public static KitItemFireworkData getFireworkData(ItemStack item) {
		List<String> fireworkRocketEffects = new ArrayList<>();
		String fireworkStarEffect = null;
		int fireworkPower = 0;

		String typeName = item.getType().name();
		boolean isFireworkCharge = false;
		List<FireworkEffect> effects = new ArrayList<>();

		if(typeName.equals("FIREWORK") || typeName.equals("FIREWORK_ROCKET")) {
			if(item.getItemMeta() instanceof FireworkMeta meta) {
				fireworkPower = meta.getPower();
				effects = meta.getEffects();
			}
		}else if(typeName.equals("FIREWORK_STAR") || typeName.equals("FIREWORK_CHARGE")) {
			if(item.getItemMeta() instanceof FireworkEffectMeta meta) {
				effects.add(meta.getEffect());
			}
			isFireworkCharge = true;
		}else {
			return null;
		}

		for(FireworkEffect effect : effects) {
			if(effect == null) {
				continue;
			}
			String line = serializeFireworkEffect(effect);

			if(isFireworkCharge) {
				fireworkStarEffect = line;
				break;
			}

			fireworkRocketEffects.add(line);
		}

		return new KitItemFireworkData(fireworkRocketEffects,fireworkStarEffect,fireworkPower);
	}

	private static String serializeFireworkEffect(FireworkEffect effect) {
		StringJoiner colors = new StringJoiner(",");
		for(Color color : effect.getColors()) {
			colors.add(String.valueOf(color.asRGB()));
		}
		StringJoiner fadeColors = new StringJoiner(",");
		for(Color color : effect.getFadeColors()) {
			fadeColors.add(String.valueOf(color.asRGB()));
		}
		String colorsLine = effect.getColors().isEmpty() ? "" : colors+";";
		return effect.getType().name()+";"+colorsLine+fadeColors+";"+effect.hasFlicker()+";"+effect.hasTrail();
	}

	public static void setFireworkData(ItemStack item,KitItemFireworkData fireworkData){
		String typeName = item.getType().name();

		if(fireworkData == null) {
			return;
		}

		int power = fireworkData.fireworkPower();
		List<String> rocketEffects = fireworkData.fireworkRocketEffects();
		String starEffect = fireworkData.fireworkStarEffect();

		if(typeName.equals("FIREWORK") || typeName.equals("FIREWORK_ROCKET")) {
			if(!(item.getItemMeta() instanceof FireworkMeta meta)) {
				return;
			}
			if(rocketEffects != null) {
				for(String rocketEffect : rocketEffects) {
					meta.addEffect(getFireworkEffect(rocketEffect));
				}
			}
			meta.setPower(power);
			item.setItemMeta(meta);
		}else if(typeName.equals("FIREWORK_STAR") || typeName.equals("FIREWORK_CHARGE")) {
			if(!(item.getItemMeta() instanceof FireworkEffectMeta meta)) {
				return;
			}
			if(starEffect != null) {
				meta.setEffect(getFireworkEffect(starEffect));
			}
			item.setItemMeta(meta);
		}
	}

	private static FireworkEffect getFireworkEffect(String line) {
		String[] sep = line.split(";");
		String type = sep[0];
		List<Color> colorsList = new ArrayList<>();
		for(String color : sep[1].split(",")) {
			colorsList.add(Color.fromRGB(Integer.parseInt(color)));
		}
		List<Color> fadeColorsList = new ArrayList<>();
		if(!sep[2].isEmpty()) {
			for(String fadeColor : sep[2].split(",")) {
				fadeColorsList.add(Color.fromRGB(Integer.parseInt(fadeColor)));
			}
		}

		boolean flicker = Boolean.parseBoolean(sep[3]);
		boolean trail = Boolean.parseBoolean(sep[4]);
		return FireworkEffect.builder().flicker(flicker).trail(trail).with(Type.valueOf(type))
				.withColor(colorsList).withFade(fadeColorsList).build();
	}

	@SuppressWarnings({"deprecation", "removal"})
	public static List<String> getAttributes(PlayerKits2 plugin, ItemStack item){
		if(OtherUtils.isLegacy()) {
			return plugin.getNmsManager().getAttributes(item);
		}

		ServerVersion serverVersion = PlayerKits2.serverVersion;
		boolean newSystem = serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R1);

		ItemMeta meta = item.getItemMeta();
		if(meta == null || !meta.hasAttributeModifiers()) {
			return null;
		}
		Multimap<Attribute,AttributeModifier> attributes = meta.getAttributeModifiers();
		if(attributes == null) {
			return null;
		}

		List<String> attributeList = new ArrayList<>();
		for(Attribute attribute : attributes.keySet()) {
			for(AttributeModifier modifier : attributes.get(attribute)) {
				String line;
				if(newSystem){
					line = getAttributeName(attribute)+";"+modifier.getOperation().name()+";"+modifier.getAmount()+";"
							+modifier.getKey().getNamespace()+":"+modifier.getKey().getKey()+";"+modifier.getSlotGroup();
				}else{
					line = getAttributeName(attribute)+";"+modifier.getOperation().name()+";"+modifier.getAmount()+";"+modifier.getUniqueId();
					if(modifier.getSlot() != null) {
						line=line+";"+modifier.getSlot().name();
					}
					line=line+";custom_name:"+modifier.getName();
				}

				attributeList.add(line);
			}
		}
		return attributeList;
	}

	@SuppressWarnings("removal")
	public static ItemStack setAttributes(PlayerKits2 plugin, ItemStack item, List<String> attributes) {
		if(attributes == null) {
			return item;
		}

		if(OtherUtils.isLegacy()) {
			return plugin.getNmsManager().setAttributes(item,attributes);
		}

		ServerVersion serverVersion = PlayerKits2.serverVersion;
		boolean newSystem = serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R1);

		try{
			ItemMeta meta = item.getItemMeta();
			if(meta == null) {
				return item;
			}
			for(String a : attributes) {
				String[] sep = a.split(";");
				String attribute = sep[0];
				AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(sep[1]);
				double amount = Double.parseDouble(sep[2]);

				AttributeModifier modifier;

				if(newSystem){
					String[] id = sep[3].split(":");
					NamespacedKey namespacedKey = new NamespacedKey(id[0],id[1]);
					modifier = new AttributeModifier(namespacedKey,amount,op,getEquipmentSlotGroup(sep[4]));
				}else{
					String customName = attribute;
					for(String part : sep){
						if(part.startsWith("custom_name:")){
							customName = part.replace("custom_name:","");
						}
					}
					UUID uuid = UUID.fromString(sep[3]);
					if(sep.length >= 5 && !sep[4].startsWith("custom_name:")) {
						EquipmentSlot slot = EquipmentSlot.valueOf(sep[4]);
						modifier = new AttributeModifier(uuid,customName,amount,op,slot);
					}else {
						modifier = new AttributeModifier(uuid,customName,amount,op);
					}
				}

				meta.addAttributeModifier(getAttributeByName(attribute), modifier);
			}

			item.setItemMeta(meta);
		}catch(Exception error){
			plugin.getLogger().log(Level.WARNING,"Could not set item attributes",error);
		}
		return item;
	}

	private static EquipmentSlotGroup getEquipmentSlotGroup(String name) {
		for(Field field : EquipmentSlotGroup.class.getFields()) {
			if(field.getType() != EquipmentSlotGroup.class) {
				continue;
			}
			try {
				Object group = field.get(null);
				if(group != null && group.toString().equalsIgnoreCase(name)) {
					return (EquipmentSlotGroup) group;
				}
			} catch (IllegalAccessException ignored) {
				return EquipmentSlotGroup.ANY;
			}
		}
		return EquipmentSlotGroup.ANY;
	}

	public static void addDummyAttribute(ItemMeta meta,PlayerKits2 plugin){
		try{
			AttributeModifier modifier = new AttributeModifier(new NamespacedKey(plugin,"dummy_attribute"),0,AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.FEET);
			ServerVersion serverVersion = PlayerKits2.serverVersion;
			if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R2)){
				meta.addAttributeModifier(Attribute.GRAVITY, modifier);
			}else{
				meta.addAttributeModifier(getAttributeByName("GENERIC_GRAVITY"), modifier);
			}
		}catch(Exception ignored){
			plugin.getLogger().fine("Dummy attribute is not supported on this server version.");
		}
	}

	@SuppressWarnings("deprecation")
	public static KitItemBookData getBookData(ItemStack item){
		String typeName = item.getType().name();
		if(!typeName.equals("WRITTEN_BOOK") || !(item.getItemMeta() instanceof BookMeta meta)) {
			return null;
		}

		List<String> pages = new ArrayList<>();
		String generation = null;
		String author = meta.getAuthor();
		String title = meta.getTitle();

		if(!Bukkit.getVersion().contains("1.12") && OtherUtils.isLegacy()) {
			pages = new ArrayList<>(meta.getPages());
		}else {
			if(meta.getGeneration() != null) {
				generation = meta.getGeneration().name();
			}
			for(BaseComponent[] page : meta.spigot().getPages()) {
				pages.add(ComponentSerializer.toString(page));
			}
		}

		return new KitItemBookData(pages,author,generation,title);
	}

	@SuppressWarnings("deprecation")
	public static void setBookData(ItemStack item,KitItemBookData bookData){
		String typeName = item.getType().name();

		if(bookData == null) {
			return;
		}

		String author = bookData.author();
		String generation = bookData.generation();
		String title = bookData.title();
		List<String> pages = bookData.pages();

		if(typeName.equals("WRITTEN_BOOK") && item.getItemMeta() instanceof BookMeta meta) {
			if(!Bukkit.getVersion().contains("1.12") && OtherUtils.isLegacy()) {
				meta.setPages(new ArrayList<>(pages));
			}else {
				ArrayList<BaseComponent[]> pagesBaseComponent = new ArrayList<>();
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

	@SuppressWarnings("removal")
	public static KitItemTrimData getArmorTrimData(ItemStack item) {
		if(OtherUtils.isTrimUnsupported()) {
			return null;
		}

		if(!(item.getItemMeta() instanceof ArmorMeta meta) || !meta.hasTrim()) {
			return null;
		}
		ArmorTrim armorTrim = meta.getTrim();
		if(armorTrim == null) {
			return null;
		}

		return new KitItemTrimData(armorTrim.getPattern().getKey().getKey(),armorTrim.getMaterial().getKey().getKey());
	}

	@SuppressWarnings("deprecation")
	public static void setArmorTrimData(ItemStack item,KitItemTrimData trimData){
		if(trimData == null || OtherUtils.isTrimUnsupported()) {
			return;
		}

		String pattern = trimData.pattern();
		String material = trimData.material();
		if(pattern == null || material == null || !(item.getItemMeta() instanceof ArmorMeta meta)) {
			return;
		}

		TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(material.toLowerCase()));
		TrimPattern trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(pattern.toLowerCase()));
		if(trimMaterial == null || trimPattern == null) {
			return;
		}

		meta.setTrim(new ArmorTrim(trimMaterial,trimPattern));
		item.setItemMeta(meta);
	}

	private static String getBannerPatternName(Pattern p) {
		try {
			Object patternType = p.getPattern();
			Method getPatternName = patternType.getClass().getMethod("name");
			getPatternName.setAccessible(true);
			return (String) getPatternName.invoke(patternType);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static PatternType getBannerPatternByName(String name){
		try {
			Class<?> patternTypeClass = Class.forName("org.bukkit.block.banner.PatternType");
			Method valueOf = patternTypeClass.getMethod("valueOf", String.class);
			return (PatternType) valueOf.invoke(null,name);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static Attribute getAttributeByName(String name){
		try {
			Class<?> attributeTypeClass = Class.forName("org.bukkit.attribute.Attribute");
			Field field = attributeTypeClass.getField(name);
			return (Attribute) field.get(null);
		} catch (IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getAttributeName(Object attribute){
		try {
			Class<?> attributeTypeClass = Class.forName("org.bukkit.attribute.Attribute");
			return (String)attributeTypeClass.getMethod("name").invoke(attribute);
		} catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
