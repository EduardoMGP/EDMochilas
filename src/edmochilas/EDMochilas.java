/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edmochilas;

import com.earth2me.essentials.api.Economy;
import static java.lang.Short.parseShort;
import net.minecraft.server.v1_7_R4.MojangsonParser;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author EduardoMGP
 */
public class EDMochilas extends JavaPlugin {
    
    private static EDMochilas plugin;
    
    @Override
    public void onEnable() {
        plugin = this;
        System.out.println("[EDBau] Plugin habilitado com sucesso");
        getCommand("mochila").setExecutor(new Comandos());
        getCommand("teste").setExecutor(this);
        saveDefaultConfig();
        Conexao c = new Conexao();
        c.criarTabela();
        this.getServer().getPluginManager().registerEvents(new Eventos(), this);
    }

    @Override
    public void onDisable() {
        System.out.println("[EDBau] Plugin desabilitado com sucesso");
        saveDefaultConfig();
    }
    
    public static EDMochilas getInstance(){
        return plugin;
    }
    

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("teste")) {
            Player p = (Player) sender;
            ItemStack item = p.getItemInHand();
            
            net.minecraft.server.v1_7_R4.ItemStack item2 = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = item2.getTag();
            item2.setTag(tag);
        }

        
        
        if (command.getName().equalsIgnoreCase("teste2")) {
            Player p = (Player) sender;
            String[] id = getConfig().getString("id").split(":");
            String nbtTag = getConfig().getString("item");
            ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(id[0])), 1, parseShort(id[1]));
            net.minecraft.server.v1_7_R4.ItemStack item2 = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = (NBTTagCompound) MojangsonParser.parse(nbtTag);
            item2.setTag(tag);
            p.getInventory().addItem(CraftItemStack.asBukkitCopy(item2));
            
                      
        }
        
        return true;
    }

}
