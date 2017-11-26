package com.pudimcraft.verificar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.connorlinfoot.titleapi.TitleAPI;
import com.earth2me.essentials.Essentials;

public class Verificar extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Pudimcraft");

	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		this.logger.info("Verificar v" + pdfFile.getVersion() + ", ATIVADO");
		saveDefaultConfig();
	}
	String prfx = this.getConfig().getString("Prefix");
	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		this.logger.info("Verificar v" + pdfFile.getVersion() + ", DESATIVADO");
	}

	public HashMap<String, Long> cooldowns = new HashMap<String, Long>();
	public ArrayList<String> suspeitos = new ArrayList<String>();

	public boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}
	public void logToFile(String message)
	 
    {
 
        try
        {
            File dataFolder = getDataFolder();
            if(!dataFolder.exists())
            {
                dataFolder.mkdir();
            }
 
            File saveTo = new File(getDataFolder(), this.getConfig().getString("ArquivoLog"));
            if (!saveTo.exists())
            {
                saveTo.createNewFile();
            }
 
 
            FileWriter fw = new FileWriter(saveTo, true);
 
            PrintWriter pw = new PrintWriter(fw);
 
            pw.println(message);
 
            pw.flush();
 
            pw.close();
 
        } catch (IOException e)
        {
 
            e.printStackTrace();
 
        }
  
    }
	public void verificar(Player p, Player alvo, int tempo) {
		Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		Location inicio = p.getLocation();
		Location alvop = alvo.getLocation();
		GameMode gmInicial = p.getGameMode();
		p.setGameMode(GameMode.SPECTATOR);
		ess.getUser(p).setVanished(true);
		p.teleport(alvop);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				p.teleport(inicio);
				p.setGameMode(gmInicial);
				ess.getUser(p).setVanished(false);
				cooldowns.remove(p.getName());
				suspeitos.remove(alvo.getName());
			}
		}, tempo);
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		if (!(sender instanceof Player)) {
			sender.sendMessage(this.getConfig().getString("Console"));
			return false;
		}
		if (command.getName().equalsIgnoreCase("verificar")) {
			if (!p.hasPermission(this.getConfig().getString("PermMod"))) {
				p.sendMessage(this.getConfig().getString("NoPerm"));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
				return false;
			}
			if (args.length == 0 || args.length == 1) {
				p.sendMessage(this.getConfig().getString("Usage"));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
				return false;
			}
			if (Bukkit.getPlayer(args[0]) == null) {
				p.sendMessage(this.getConfig().getString("JogadorNaoEncontrado"));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
				return false;
			}
			if (args[1] == null) {
				p.sendMessage(this.getConfig().getString("Usage"));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
			}
			Player alvo = Bukkit.getPlayer(args[0]);
			if (!isInt(args[1])) {
				p.sendMessage(this.getConfig().getString("Usage"));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
				return false;
			}
			int tempo = Integer.parseInt(args[1]) * 20;
			if (Integer.parseInt(args[1]) > this.getConfig().getInt("TempoMax")) {
				p.sendMessage(this.getConfig().getString("TempoMaximo"));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
				return false;
			}
			if (args[0].equalsIgnoreCase(p.getName())) {
				p.sendMessage(this.getConfig().getString("VerificarVc"));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
				return false;
			}
			if (this.cooldowns.containsKey(sender.getName())) {
				int cooldownTime = Integer.parseInt(args[1]);
				long secondsLeft = ((Long) this.cooldowns.get(sender.getName())).longValue() / 1000L + cooldownTime
						- System.currentTimeMillis() / 1000L;
				if (secondsLeft > 0L) {
					sender.sendMessage(this.getConfig().getString("JaVerificando").replaceAll("%tempo%", String.valueOf(secondsLeft)));
					return false;
				}
				return false;
			}
			if(this.suspeitos.contains(alvo.getName())) {
				p.sendMessage(this.getConfig().getString("ModJaVerificando").replaceAll("%mod%", p.getName()).replaceAll("%player%", alvo.getName()));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
				return false;
			}
			if (alvo.hasPermission(this.getConfig().getString("PermMod")) && !p.hasPermission(this.getConfig().getString("PermAdmin"))) {
				p.sendMessage(this.getConfig().getString("modVmod"));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
				return false;
			}
			if (Integer.parseInt(args[1]) < this.getConfig().getInt("TempoMin")) {
				p.sendMessage(this.getConfig().getString("TempoMinimo"));
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomErro")), 1.0F, 1.0F);
				return false;
			} else {
				verificar(p, alvo, tempo);
				this.cooldowns.put(sender.getName(), Long.valueOf(System.currentTimeMillis()));
				this.suspeitos.add(alvo.getName());
				Date now = new Date();
				SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("SomSucesso")), 1.0F, 1.0F);
				if(this.getConfig().getBoolean("BroadCastToMods")) {
					int tempox = tempo / 20;
					Bukkit.broadcast(this.getConfig().getString("BroadCast").replaceAll("%mod", p.getName()).replaceAll("%player%", alvo.getName()).replaceAll("%tempo%", String.valueOf(tempox)), this.getConfig().getString("PermMod"));
				}
				if(this.getConfig().getBoolean("LogToFile")) {
					logToFile("[" + format.format(now) + "] " + p.getName() + " verificou " + alvo.getName());
				}
				if(this.getConfig().getBoolean("Titulos")) {
					TitleAPI.sendTitle(p, 20, tempo - 40, 20, this.getConfig().getString("Titulo").replaceAll("%player%", alvo.getName()), this.getConfig().getString("SubTitulo"));
				}
				return true;
			}
		}
		return false;
	}
}