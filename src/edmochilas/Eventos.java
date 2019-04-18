/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edmochilas;

import com.earth2me.essentials.api.Economy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author EduardoMGP
 */
public class Eventos implements Listener {

    private EDMochilas plugin;
    private Economy econ;

    public Eventos() {
        plugin = EDMochilas.getInstance();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
   
        if (e.getClickedInventory().getTitle().equals(plugin.getConfig().getString("mochilaNome").replaceAll("&", "§"))) {
            e.setCancelled(true);
            if (e.getClick() == ClickType.SHIFT_LEFT) {
                String id = e.getCurrentItem().getTypeId() + ":" + e.getCurrentItem().getData().getData();
                if (id.equals(plugin.getConfig().getString("idMochilaSemComprar"))) {
                    try {
                        double preco = plugin.getConfig().getDouble("mochilaPreco");
                        if (econ.getMoney(p.getName()) >= preco) {
                            econ.subtract(p.getName(), preco);
                            Conexao c = new Conexao();
                            Connection conexao = c.abrirConexao();
                            PreparedStatement prepare = conexao.prepareStatement("INSERT INTO edmochilas (mochilaNumero, mochilaOwner) VALUES (?, ?)");
                            prepare.setString(2, p.getName());
                            prepare.setInt(1, e.getSlot());
                            prepare.execute();
                            p.sendMessage(plugin.getConfig().getString("mochilaComprada").replaceAll("&", "§").replaceAll("%preco%", preco + ""));
                            p.closeInventory();
                            new Comandos().abrirMochila(p);
                        } else {
                            p.sendMessage(plugin.getConfig().getString("semDinheiro").replaceAll("&", "§").replaceAll("%preco%", preco + ""));
                        }
                    } catch (Exception err) {
                    }
                }
                if (id.equals(plugin.getConfig().getString("idMochilaSemComprarVip"))) {
                    if (p.hasPermission("edmochilas.mochilasVip")) {
                        try {
                            double preco = plugin.getConfig().getDouble("mochilaVipPreco");
                            if (econ.getMoney(p.getName()) >= preco) {
                                econ.subtract(p.getName(), preco);
                                Conexao c = new Conexao();
                                Connection conexao = c.abrirConexao();
                                PreparedStatement prepare = conexao.prepareStatement("INSERT INTO edmochilas (mochilaNumero, mochilaOwner) VALUES (?, ?)");
                                prepare.setString(2, p.getName());
                                prepare.setInt(1, e.getSlot());
                                prepare.execute();
                                p.sendMessage(plugin.getConfig().getString("mochilaComprada").replaceAll("&", "§").replaceAll("%preco%", preco + ""));
                                p.closeInventory();
                                new Comandos().abrirMochila(p);
                            } else {
                                p.sendMessage(plugin.getConfig().getString("semDinheiro").replaceAll("&", "§").replaceAll("%preco%", preco + ""));
                            }
                        } catch (Exception err) {
                        }
                    } else {
                        p.sendMessage(plugin.getConfig().getString("apenasVips").replaceAll("&", "§"));
                    }
                }
                return;
            }
            
            Comandos c = new Comandos();
            c.abrirMochila(p, e.getSlot());
            return;
        }
        if (e.getClickedInventory().getTitle().contains(plugin.getConfig().getString("mochilaNome").replaceAll("&", "§"))) {
            if (e.getSlot() > 35) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Inventory inv = e.getInventory();
        if (inv.getTitle().equals(plugin.getConfig().getString("mochilaNome").replaceAll("&", "§"))) {
            return;
        }
        if (inv.getTitle().contains(plugin.getConfig().getString("mochilaNome").replaceAll("&", "§"))) {
            String[] titulo = inv.getTitle().split(plugin.getConfig().getString("mochilaNome").replaceAll("&", "§"));
            int mochilaNumero = Integer.parseInt(titulo[1]);
            int i = 0;
            try {
                Conexao c = new Conexao();
                Connection conexao = c.abrirConexao();

                PreparedStatement prepare = conexao.prepareStatement("DELETE FROM edmochilasItens WHERE mochilaOwner = ? AND mochilaNumero = ?");
                prepare.setInt(2, mochilaNumero);
                prepare.setString(1, player.getName());
                prepare.execute();

                for (ItemStack item : inv.getContents()) {
                    if (item != null) {
                        net.minecraft.server.v1_7_R4.ItemStack item2 = CraftItemStack.asNMSCopy(item);
                        NBTTagCompound tag = item2.getTag();
                        item2.setTag(tag);
                        prepare = conexao.prepareStatement(""
                                + "INSERT INTO edmochilasItens (mochilaNumero, mochilaOwner, "
                                + "mochilaItemJson, mochilaItemId, "
                                + "mochilaItemData, mochilaItemQuantidade, "
                                + "mochilaItemSlot ) VALUES (?, ?, ?, ?, ?, ?, ?)");
                        prepare.setInt(1, mochilaNumero);
                        prepare.setString(2, player.getName());
                        try {
                            prepare.setString(3, item2.getTag().toString());
                        } catch (Exception err) {
                            prepare.setString(3, "0");
                        }
                        prepare.setInt(4, item.getTypeId());
                        prepare.setInt(5, item.getData().getData());
                        prepare.setInt(6, item.getAmount());
                        prepare.setInt(7, i);
                        prepare.execute();
                    }

                    i++;
                    if (i == 36) {
                        break;
                    }
                }
                conexao.close();

            } catch (Exception ee) {
                Bukkit.getConsoleSender().sendMessage("§c" + ee);
            }

        }
    }

}
