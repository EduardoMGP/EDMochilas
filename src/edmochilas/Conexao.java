/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edmochilas;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.bukkit.Bukkit;

/**
 *
 * @author EduardoMGP
 */
public class Conexao {

    private EDMochilas plugin;

    public Conexao() {
        plugin = EDMochilas.getInstance();
    }

    public Connection abrirConexao() {
        return abrirConexaoMySQL();
    }

    private Connection abrirConexaoMySQL() {

        String URL = "jdbc:mysql://" + this.plugin.getConfig().getString("Conexao.Host") + ":" + this.plugin.getConfig().getString("Conexao.Porta") + "/" + this.plugin.getConfig().getString("Conexao.DataBase");
        try {
            Connection conexao = DriverManager.getConnection(URL, this.plugin.getConfig().getString("Conexao.Usuario"), this.plugin.getConfig().getString("Conexao.Senha"));
            return conexao;
        } catch (Exception e) {
            System.out.println("[EDHome] Houve um erro ao se conectar ao MySQL, desabilitando o plugin!");
            this.plugin.getPluginLoader().disablePlugin(this.plugin);
        }

        return null;
    }

    public void criarTabela() {
        Connection conexao = abrirConexao();
        if (conexao != null) {
            try {
                PreparedStatement prepare = conexao.prepareStatement(""
                        + "CREATE TABLE IF NOT EXISTS edmochilas ("
                        + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                        + "mochilaNumero INT NOT NULL,"
                        + "mochilaNome VARCHAR(50) DEFAULT NULL,"
                        + "mochilaOwner VARCHAR(50) NOT NULL"
                        + ")");

                prepare.execute();

                prepare = conexao.prepareStatement(""
                        + "CREATE TABLE IF NOT EXISTS edmochilasItens ("
                        + "mochilaNumero INT NOT NULL,"
                        + "mochilaOwner VARCHAR(50),"
                        + "mochilaItemJson TEXT DEFAULT NULL,"
                        + "mochilaItemId INT NOT NULL,"
                        + "mochilaItemData INT NOT NULL,"
                        + "mochilaItemQuantidade INT NOT NULL,"
                        + "mochilaItemSlot INT NOT NULL"
                        + ")");

                prepare.execute();
                prepare.execute("DROP PROCEDURE IF EXISTS listarItensMochila");
                prepare = conexao.prepareStatement(""
                        + "CREATE PROCEDURE IF NOT EXISTS listarItensMochila(mochilaNumero INT, mochilaOwner VARCHAR(50))\n "
                        + "	BEGIN "
                        + "        SELECT COUNT(edmochilas.mochilaNumero) "
                        + "        INTO @quantidade "
                        + "        FROM edmochilas "
                        + "        WHERE edmochilas.mochilaOwner = mochilaOwner AND edmochilas.mochilaNumero = mochilaNumero; "
                        + "        IF(@quantidade = 0) THEN "
                        + "        	SELECT -1 AS mochilaItemId, -1 AS mochilaItemJson, -1 AS mochilaItemId, -1 AS mochilaItemData, -1 AS mochilaItemSlot, -1 AS mochilaItemQuantidade; "
                        + "        ELSE "
                        + "        	SELECT COUNT(edmochilasItens.mochilaNumero) "
                        + "	        INTO @quantidade "
                        + "	        FROM edmochilasItens "
                        + "	        WHERE edmochilasItens.mochilaOwner = mochilaOwner; "
                        + "        	IF(@quantidade = 0) THEN "
                        + "        		SELECT 0 AS mochilaItemId, 0 AS mochilaItemJson, 0 AS mochilaItemId, 0 AS mochilaItemData, 0 AS mochilaItemSlot, 0 AS mochilaItemQuantidade; "
                        + "        	ELSE "
                        + "	        	SELECT * FROM edmochilasItens "
                        + "	        	WHERE edmochilasItens.mochilaNumero = mochilaNumero "
                        + "	        	AND edmochilasItens.mochilaOwner = mochilaOwner; "
                        + "	        END IF; "
                        + "        END IF;\n "
                        + "	END;");

                prepare.execute();
                conexao.close();
                return;

            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§c" + e + "");
                Bukkit.getConsoleSender().sendMessage("[EDMochilas]  §cHouve um erro ao criar a base de dados, desligando servidor!");
                Bukkit.shutdown();
                return;
            }
        }
    }

}
