package com.pudimcraft.verificar;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import com.earth2me.essentials.Essentials;

public class Verificar
  extends JavaPlugin
{
  public final Logger logger = Logger.getLogger("Pudimcraft");
  
  public void onEnable()
  {
    PluginDescriptionFile pdfFile = getDescription();
    this.logger.info("Verificar v" + pdfFile.getVersion() + ", ATIVADO");
    saveDefaultConfig();
  }
  
  public void onDisable()
  {
    PluginDescriptionFile pdfFile = getDescription();
    this.logger.info("Verificar v" + pdfFile.getVersion() + ", DESATIVADO");
  }
  public HashMap<String, Long> cooldowns = new HashMap<String, Long>();
  
  public boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
		}
		return false;
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
			}
		},tempo);

  }
  
  
  
  
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = (Player) sender;
		int tempo = Integer.parseInt(args[1]) * 20;
		if(!(sender instanceof Player)) {
			sender.sendMessage("Use esse comando no jogo");
			return true;
		}
		if(!p.hasPermission("mod.verificar")) {
			p.sendMessage("§cVocê nao pode fazer isso.");
			return true;
		}
		if(args.length == 1 || args.length == 0) {
			p.sendMessage("§7/verificar [JOGADOR] [SEGUNDOS]");
			return true;
		}
		if(Bukkit.getPlayer(args[0]) == null) {
			p.sendMessage("§cJogador não encontrado");
			return true;
		}
		Player alvo = Bukkit.getPlayer(args[0]);
		if(!isInt(args[1])) {
			p.sendMessage("§7/verificar [JOGADOR] [SEGUNDOS]");
			return true;
		}
		if(Integer.parseInt(args[1]) > 90) {
			p.sendMessage("§cO tempo maximo é 90 segundos.");
			return true;
		}
		if (this.cooldowns.containsKey(sender.getName())) {
			int cooldownTime = Integer.parseInt(args[0]);
			long secondsLeft = ((Long) this.cooldowns.get(sender.getName())).longValue() / 1000L + cooldownTime
					- System.currentTimeMillis() / 1000L;
			if (secondsLeft > 0L) {
				sender.sendMessage("§cVoce ja esta verificando um jogador aguarde o termino, tempo restante: §e" + secondsLeft);
				return true;
			}
			return true;
		}
		if(alvo.hasPermission("mod.verificar") && !p.hasPermission("admin.verificar")) {
			p.sendMessage("§cVoce nao pode verificar outros moderadores!");
			return true;
		}
		if(Integer.parseInt(args[1]) < 10) {
			p.sendMessage("§cO tempo deve ser maior que 10 segundos!");
			return true;
		} else {
			verificar(p, alvo, tempo);
			this.cooldowns.put(sender.getName(), Long.valueOf(System.currentTimeMillis()));
			return false;
		}
  	} 
}