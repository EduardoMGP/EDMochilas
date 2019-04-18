/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edmochilas;

import static java.lang.Short.parseShort;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_7_R4.MojangsonParser;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author EduardoMGP
 */
public class Comandos implements CommandExecutor {

    private EDMochilas plugin;

    public Comandos() {
        plugin = EDMochilas.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {

        if (cmd.getName().equalsIgnoreCase("mochila")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length == 0) {
                    abrirMochila(p);
                } else {
                    Bukkit.getConsoleSender().sendMessage("[EDMochila] Este comando pode ser executado apenas por jogadores");
                }

            }

        }
        return true;
    }

    public void abrirMochila(Player p) {
        try {
            Conexao c = new Conexao();
            Connection conexao = c.abrirConexao();
            Statement st = conexao.createStatement();
            PreparedStatement pre = conexao.prepareStatement("SELECT * FROM edmochilas WHERE mochilaOwner = ?");
            pre.setString(1, p.getName());
            ResultSet resultado = pre.executeQuery();

            String nome = plugin.getConfig().getString("mochilaNome").replaceAll("&", "§");
            Inventory inv = Bukkit.createInventory(p, 9 * 6, nome);
            ItemStack itemStack;
            String[] item = new String[2];
            for (int i = 0; i < 54; i++) {

                int mvenda = plugin.getConfig().getInt("mochilaVenda");
                int mvendaLoja = plugin.getConfig().getInt("mochilaVendaLoja");
                int mvip = plugin.getConfig().getInt("mochilasVip");

                if (i < mvenda) {

                    item = plugin.getConfig().getString("idMochilaSemComprar").split(":");
                    itemStack = new ItemStack(Integer.parseInt(item[0]), 1, Short.parseShort(item[1]));

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(plugin.getConfig().getString("nomeMochilaSemComprar").replaceAll("&", "§"));
                    List<String> lore = new ArrayList<>();
                    for (String l : plugin.getConfig().getStringList("loreMochilaVenda")) {
                        lore.add(l.replaceAll("&", "§").replaceAll("%preco%", plugin.getConfig().getDouble("mochilaPreco") + ""));
                    }
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);
                    inv.setItem(i, new ItemStack(itemStack));
                }

                if (i >= mvenda && i < mvip + mvenda) {

                    item = plugin.getConfig().getString("idMochilaSemComprarVip").split(":");
                    itemStack = new ItemStack(Integer.parseInt(item[0]), 1, Short.parseShort(item[1]));

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(plugin.getConfig().getString("nomeMochilaSemComprar").replaceAll("&", "§"));
                    List<String> lore = new ArrayList<>();
                    for (String l : plugin.getConfig().getStringList("loreMochilasVip")) {
                        lore.add(l.replaceAll("&", "§").replaceAll("%preco%", plugin.getConfig().getDouble("mochilaVipPreco") + ""));
                    }
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);
                    inv.setItem(i, new ItemStack(itemStack));

                }

                if (i >= mvip + mvenda && i < mvip + mvenda + mvendaLoja) {

                    item = plugin.getConfig().getString("idMochilaSemComprarLoja").split(":");
                    itemStack = new ItemStack(Integer.parseInt(item[0]), 1, Short.parseShort(item[1]));

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(plugin.getConfig().getString("nomeMochilaSemComprar").replaceAll("&", "§"));
                    List<String> lore = new ArrayList<>();
                    for (String l : plugin.getConfig().getStringList("loreMochilaVendaLoja")) {
                        lore.add(l.replaceAll("&", "§"));
                    }
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);
                    inv.setItem(i, new ItemStack(itemStack));
                }

            }

            item = plugin.getConfig().getString("idMochilaComprado").split(":");
            while (resultado.next()) {
                itemStack = new ItemStack(Integer.parseInt(item[0]), 1, Short.parseShort(item[1]));
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (resultado.getString("mochilaNome") == null) {
                    itemMeta.setDisplayName("§bMochila");
                } else {
                    itemMeta.setDisplayName(resultado.getString("mochilaNome"));
                }
                itemStack.setItemMeta(itemMeta);
                inv.setItem(resultado.getInt("mochilaNumero"), itemStack);
                itemStack.setItemMeta(itemMeta);
            }

            p.openInventory(inv);
        } catch (Exception e) {
        }
    }

    public void abrirMochila(Player p, int numeroMochila) {
        Conexao c = new Conexao();
        try {
            Connection conexao = c.abrirConexao();
            Statement st = conexao.createStatement();
            PreparedStatement pre = conexao.prepareStatement("CALL listarItensMochila(?, ?)");
            pre.setInt(1, numeroMochila);
            pre.setString(2, p.getName());
            ResultSet resultado = pre.executeQuery();
            ArrayList<String> items = new ArrayList<String>();
            Inventory inv = Bukkit.createInventory(p, 9 * 6, plugin.getConfig().getString("mochilaNome").replaceAll("&", "§") + numeroMochila);
            while (resultado.next()) {
                if (resultado.getInt("mochilaItemSlot") == -1) {
                    p.sendMessage(plugin.getConfig().getString("naoPossuiEstaMochila").replaceAll("&", "§"));
                    return;
                }
                ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(resultado.getString("mochilaItemId"))), Integer.parseInt(resultado.getString("mochilaItemQuantidade")), parseShort(resultado.getString("mochilaItemData")));
                net.minecraft.server.v1_7_R4.ItemStack itemCopy = CraftItemStack.asNMSCopy(item);
                if (!resultado.getString("mochilaItemJson").equals("0") && resultado.getString("mochilaItemJson") != null) {
                    NBTTagCompound tag = (NBTTagCompound) MojangsonParser.parse(resultado.getString("mochilaItemJson"));
                    itemCopy.setTag(tag);
                }
                inv.setItem(Integer.parseInt(resultado.getString("mochilaItemSlot")), CraftItemStack.asBukkitCopy(itemCopy));
            }

            for (int i = 36; i < 54; i++) {
                inv.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE));
            }
            inv.setItem(46, new ItemStack(Material.BEACON));
            inv.setItem(52, new ItemStack(Material.BEACON));
            p.openInventory(inv);

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§c" + e);
        }
    }
}
