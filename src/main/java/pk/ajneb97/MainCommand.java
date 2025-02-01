package pk.ajneb97;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pk.ajneb97.configs.MainConfigManager;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.internal.GiveKitInstructions;
import pk.ajneb97.model.internal.PlayerKitsMessageResult;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.inventory.KitInventory;
import pk.ajneb97.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MainCommand implements CommandExecutor, TabCompleter {

    private final PlayerKits2 plugin;
    private final MessagesManager msgManager;
    private final FileConfiguration messagesConfig;

    public MainCommand(PlayerKits2 plugin) {
        this.plugin = plugin;
        this.msgManager = plugin.getMessagesManager();
        this.messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            if (!executeCommon(sender, args)) {
                help(sender);
            }
            return true;
        }

        Player player = (Player) sender;

        boolean claimKitShortCommand = plugin.getConfigsManager().getMainConfigManager().getConfig().getBoolean("claim_kit_short_command");

        if (executeCommon(sender, args)) {
            return true;
        }

        if (args.length < 1) {
            // /kit
            if(plugin.getVerifyManager().isCriticalErrors()){
                msgManager.sendMessage(player,messagesConfig.getString("pluginCriticalErrors"),true);
                return true;
            }
            plugin.getInventoryManager().openInventory(new InventoryPlayer(player,"main_inventory"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "preview":
                preview(player, args);
                break;
            case "create":
                create(player, args);
                break;
            case "delete":
                delete(sender, args);
                break;
            case "edit":
                edit(player, args);
                break;
            case "verify":
                verify(player);
                break;
            case "claim":
                claim(player, args);
                break;
            default:
                if(claimKitShortCommand){
                    claimKitShortCommand(player,args[0]);
                    break;
                }
                help(sender);
                break;
        }
        return true;
    }

    private void help(final CommandSender sender){
        if(!PlayerUtils.isPlayerKitsAdmin(sender)){
            msgManager.sendMessage(sender,messagesConfig.getString("commandDoesNotExists"),true);
            return;
        }
        sender.sendMessage(
            "\n §7[ [ §8[§bPlayerKits§a²§8] §7] ]" +
            "\n " +
            "\n §6/kit §8Opens the GUI." +
            "\n §6/kit claim <kit> §8Claims a kit outside the GUI." +
            "\n §6/kit create <kit> (optional)original §8Creates a new kit using the items in your inventory." +
            "\n §6/kit edit <kit> §8Edits a kit." +
            "\n §6/kit give <kit> <player> §8Gives a kit to a player." +
            "\n §6/kit delete <kit> §8Deletes a kit." +
            "\n §6/kit reset <kit> <player>/* §8Resets kit data for a player." +
            "\n §6/kit preview <kit> §8Previews a kit." +
            "\n §6/kit open <inventory> <player> §8Opens a specific inventory for a player." +
            "\n §6/kit reload §8Reloads the config." +
            "\n §6/kit verify §8Checks the plugin for errors." +
            "\n " +
            "\n §7[ [ §8[§bPlayerKits§a²§8] §7] ]"
        );
    }

    private boolean executeCommon(final CommandSender sender, final String[] args) {
        if (args.length < 1) {
            return false;
        }
        switch(args[0].toLowerCase()) {
            case "reload":
                reload(sender, args);
                break;
            case "give":
                give(sender, args);
                break;
            case "reset":
                reset(sender, args);
                break;
            case "migrate":
                migrate(sender, args);
                break;
            case "open":
                open(sender, args);
                break;
            default:
                return false;
        }
        return true;
    }

    public void migrate(CommandSender sender,String[] args){
        if(!PlayerUtils.isPlayerKitsAdmin(sender)){
            msgManager.sendMessage(sender,messagesConfig.getString("noPermissions"),true);
            return;
        }

        plugin.getMigrationManager().migrate(sender);
    }

    public void verify(Player player){
        if(!PlayerUtils.isPlayerKitsAdmin(player)){
            msgManager.sendMessage(player,messagesConfig.getString("noPermissions"),true);
            return;
        }
        plugin.getVerifyManager().sendVerification(player);
    }

    public void reload(CommandSender sender,String[] args){
        if(!PlayerUtils.isPlayerKitsAdmin(sender)){
            msgManager.sendMessage(sender,messagesConfig.getString("noPermissions"),true);
            return;
        }

        if(!plugin.getConfigsManager().reload()){
            sender.sendMessage(PlayerKits2.prefix+MessagesManager.getColoredMessage(" &cThere was an error reloading the config, check the console."));
            return;
        }
        msgManager.sendMessage(sender,messagesConfig.getString("commandReload"),true);
    }

    public void reset(CommandSender sender,String[] args) {
        // /kits reset <kit> <player>
        if(!PlayerUtils.isPlayerKitsAdmin(sender)) {
            msgManager.sendMessage(sender, messagesConfig.getString("noPermissions"), true);
            return;
        }

        if(args.length < 3) {
            msgManager.sendMessage(sender, messagesConfig.getString("commandResetError"), true);
            return;
        }

        String kitName = args[1];
        String playerName = args[2];

        PlayerKitsMessageResult result = plugin.getPlayerDataManager().resetKitForPlayer(playerName,kitName,playerName.equals("*"));
        if(result.isError()){
            msgManager.sendMessage(sender, result.getMessage(), true);
        }else{
            if(playerName.equals("*")){
                msgManager.sendMessage(sender, messagesConfig.getString("kitResetCorrectAll")
                        .replace("%kit%",kitName), true);
            }else{
                msgManager.sendMessage(sender, messagesConfig.getString("kitResetCorrect")
                        .replace("%kit%",kitName).replace("%player%",playerName), true);
            }
        }
    }

    public void open(CommandSender sender,String[] args) {
        // /kits open <inventory> <player>
        if(!PlayerUtils.isPlayerKitsAdmin(sender)) {
            msgManager.sendMessage(sender, messagesConfig.getString("noPermissions"), true);
            return;
        }

        if(args.length < 3) {
            msgManager.sendMessage(sender, messagesConfig.getString("commandOpenError"), true);
            return;
        }

        String inventoryName = args[1];
        String playerName = args[2];

        if(plugin.getInventoryManager().getInventory(inventoryName) == null){
            msgManager.sendMessage(sender, messagesConfig.getString("inventoryNotExists"), true);
            return;
        }

        Player player = Bukkit.getPlayer(playerName);
        if(player == null){
            msgManager.sendMessage(sender,messagesConfig.getString("playerNotOnline")
                    .replace("%player%",playerName),true);
            return;
        }

        InventoryPlayer inventoryPlayer = new InventoryPlayer(player,inventoryName);
        plugin.getInventoryManager().openInventory(inventoryPlayer);
    }

    public void give(CommandSender sender,String[] args){
        // /kits give <kit> <player>
        if(!PlayerUtils.isPlayerKitsAdmin(sender)){
            msgManager.sendMessage(sender,messagesConfig.getString("noPermissions"),true);
            return;
        }

        if(args.length < 3){
            msgManager.sendMessage(sender,messagesConfig.getString("commandGiveError"),true);
            return;
        }

        String kitName = args[1];
        Player player = Bukkit.getPlayer(args[2]);
        if(player == null){
            msgManager.sendMessage(sender,messagesConfig.getString("playerNotOnline")
                    .replace("%player%",args[2]),true);
            return;
        }

        PlayerKitsMessageResult result = plugin.getKitsManager().giveKit(player,kitName,new GiveKitInstructions(true,false,false,false));
        if(result.isError()){
            msgManager.sendMessage(sender,messagesConfig.getString("commandGiveError2")
                    .replace("%error%",result.getMessage()),true);
        }else{
            msgManager.sendMessage(sender,messagesConfig.getString("commandGiveCorrect")
                    .replace("%kit%",kitName).replace("%player%",args[2]),true);
        }
    }

    public void claim(Player player,String[] args){
        // /kit claim <kit>
        if(args.length < 2){
            msgManager.sendMessage(player,messagesConfig.getString("commandClaimError"),true);
            return;
        }

        String kitName = args[1];
        claimKitShortCommand(player,kitName);
    }

    public void preview(Player player,String[] args){
        // /kit preview <kit>
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
        if(!mainConfigManager.isKitPreview()){
            msgManager.sendMessage(player,messagesConfig.getString("kitPreviewDisabled"),true);
            return;
        }

        if(args.length < 2){
            msgManager.sendMessage(player,messagesConfig.getString("commandPreviewError"),true);
            return;
        }

        Kit kit = plugin.getKitsManager().getKitByName(args[1]);
        if(kit == null){
            msgManager.sendMessage(player,messagesConfig.getString("kitDoesNotExists")
                    .replace("%kit%",args[1]),true);
            return;
        }

        if(kit.isPermissionRequired()){
            if(mainConfigManager.isKitPreviewRequiresKitPermission() && !kit.playerHasPermission(player)){
                msgManager.sendMessage(player,messagesConfig.getString("cantPreviewError"),true);
                return;
            }
        }

        InventoryPlayer inventoryPlayer = new InventoryPlayer(player,"preview_inventory");
        inventoryPlayer.setKitName(args[1]);
        inventoryPlayer.setPreviousInventoryName("main_inventory");
        plugin.getInventoryManager().openInventory(inventoryPlayer);
    }

    public void claimKitShortCommand(Player player,String kitName){
        // /kit <kit>
        PlayerKitsMessageResult result = plugin.getKitsManager().giveKit(player,kitName,new GiveKitInstructions());
        if(result.isError()){
            msgManager.sendMessage(player,result.getMessage(),true);
        }else{
            if(result.isProceedToBuy()){
                //Open requirements inventory
                InventoryPlayer inventoryPlayer = new InventoryPlayer(player,"buy_requirements_inventory");
                inventoryPlayer.setKitName(kitName);
                inventoryPlayer.setPreviousInventoryName("main_inventory");
                plugin.getInventoryManager().openInventory(inventoryPlayer);
                return;
            }
            msgManager.sendMessage(player,messagesConfig.getString("kitReceived").replace("%kit%",kitName),true);
        }
    }

    public void create(Player player,String[] args){
        // /kit create <kit> (optional)<original/configurable>
        if(!PlayerUtils.isPlayerKitsAdmin(player)){
            msgManager.sendMessage(player,messagesConfig.getString("noPermissions"),true);
            return;
        }

        if(args.length < 2){
            msgManager.sendMessage(player,messagesConfig.getString("commandCreateError"),true);
            return;
        }

        boolean saveOriginalItems = plugin.getConfigsManager().getMainConfigManager().isNewKitDefaultSaveModeOriginal();
        if(args.length >= 3){
            if(args[2].equalsIgnoreCase("original")){
                saveOriginalItems = true;
            }else if(args[2].equalsIgnoreCase("configurable")){
                saveOriginalItems = false;
            }else{
                msgManager.sendMessage(player,messagesConfig.getString("commandCreateError"),true);
                return;
            }
        }

        plugin.getKitsManager().createKit(args[1],player,saveOriginalItems);
    }

    public void delete(CommandSender sender,String[] args){
        // /kit delete <kit>
        if(!PlayerUtils.isPlayerKitsAdmin(sender)){
            msgManager.sendMessage(sender,messagesConfig.getString("noPermissions"),true);
            return;
        }

        if(args.length < 2){
            msgManager.sendMessage(sender,messagesConfig.getString("commandDeleteError"),true);
            return;
        }

        plugin.getKitsManager().deleteKit(args[1],sender);
    }

    public void edit(Player player,String[] args){
        // /kit edit <kit>
        if(!PlayerUtils.isPlayerKitsAdmin(player)){
            msgManager.sendMessage(player,messagesConfig.getString("noPermissions"),true);
            return;
        }

        if(args.length < 2){
            msgManager.sendMessage(player,messagesConfig.getString("commandEditError"),true);
            return;
        }

        if(plugin.getKitsManager().getKitByName(args[1]) == null){
            msgManager.sendMessage(player,messagesConfig.getString("kitDoesNotExists")
                    .replace("%kit%",args[1]),true);
            return;
        }

        InventoryPlayer inventoryPlayer = new InventoryPlayer(player,null);
        inventoryPlayer.setKitName(args[1]);
        plugin.getInventoryEditManager().openInventory(inventoryPlayer);
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
        boolean claimKitShortCommand = mainConfigManager.isClaimKitShortCommand();
        boolean kitPreviewEnabled = mainConfigManager.isKitPreview();

        List<String> completions = new ArrayList<String>();
        List<String> commands = new ArrayList<String>();

        if(args.length == 1) {
            if(claimKitShortCommand){
                List<String> kitCompletions = getKitCompletions(sender,args,0);
                if(kitCompletions != null){
                    commands.addAll(kitCompletions);
                }
            }else{
                commands.add("claim");
            }
            if(kitPreviewEnabled){
                commands.add("preview");
            }
            if(PlayerUtils.isPlayerKitsAdmin(sender)){
                commands.add("give");commands.add("delete");commands.add("create");
                commands.add("reload");commands.add("reset");commands.add("edit");
                commands.add("verify");commands.add("migrate");commands.add("open");
            }
            for(String c : commands) {
                if(args[0].isEmpty() || c.startsWith(args[0].toLowerCase())) {
                    completions.add(c);
                }
            }
            return completions;
        }else {
            if(args.length == 2) {
                if(!claimKitShortCommand){
                    commands.add("claim");
                }
                if(kitPreviewEnabled){
                    commands.add("preview");
                }
                if(PlayerUtils.isPlayerKitsAdmin(sender)){
                    commands.add("give");commands.add("delete");
                    commands.add("reset");commands.add("edit");
                    commands.add("open");
                }
                for(String c : commands) {
                    if(args[0].equalsIgnoreCase(c)){
                        if(c.equals("open")){
                            return getInventoryCompletions(args,1);
                        }else{
                            return getKitCompletions(sender,args,1);
                        }

                    }
                }
            }else if(args.length == 3 && PlayerUtils.isPlayerKitsAdmin(sender)){
                if(args[0].equalsIgnoreCase("create")){
                    commands.add("original");commands.add("configurable");
                    for(String c : commands) {
                        if(args[2].isEmpty() || c.startsWith(args[2].toLowerCase())) {
                            completions.add(c);
                        }
                    }
                    return completions;
                }else if(args[0].equalsIgnoreCase("reset")){
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        if(args[2].isEmpty() || p.getName().toLowerCase().startsWith(args[2].toLowerCase())){
                            completions.add(p.getName());
                        }
                    }
                    if(args[2].isEmpty() || "*".startsWith(args[2].toLowerCase())) {
                        completions.add("*");
                    }
                    return completions;
                }
            }
        }

        return null;
    }

    public List<String> getKitCompletions(CommandSender sender,String[] args,int argKitPos){
        List<String> completions = new ArrayList<>();
        String argKit = args[argKitPos];

        Collection<Kit> kits = plugin.getKitsManager().getKits();
        for(Kit kit : kits) {
            if(argKit.isEmpty() || kit.getName().toLowerCase().startsWith(argKit.toLowerCase())) {
                if(kit.playerHasPermission(sender)){
                    completions.add(kit.getName());
                }
            }
        }

        if(completions.isEmpty()){
            return null;
        }
        return completions;
    }

    public List<String> getInventoryCompletions(String[] args,int argInvPos){
        List<String> completions = new ArrayList<>();
        String argInv = args[argInvPos];

        ArrayList<KitInventory> inventories = plugin.getInventoryManager().getInventories();
        for(KitInventory inv : inventories) {
            Bukkit.getConsoleSender().sendMessage(inv.getName());
            if((argInv.isEmpty() || inv.getName().toLowerCase().startsWith(argInv.toLowerCase()))
                && !inv.getName().equals("preview_inventory") && !inv.getName().equals("buy_requirements_inventory")) {
                completions.add(inv.getName());
            }
        }

        if(completions.isEmpty()){
            return null;
        }
        return completions;
    }
}
