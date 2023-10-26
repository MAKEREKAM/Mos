package kr.vanilage.main

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList


class Main : JavaPlugin() {
    override fun onEnable() {
        this.saveDefaultConfig()
    }

    override fun onDisable() {
        for (onlinePlayer in Bukkit.getOnlinePlayers()) { onlinePlayer.kick() }
        val worldName = this.config.getString("worldname")!!
        val backupFolderName = this.config.getString("backupfolderdir")!!
        if (backupFolderName == "") return
        val world = Bukkit.getWorld(worldName)!!
        Bukkit.unloadWorld(world, false)
        deleteDirectory(Path.of(world.worldFolder.toURI()))
        val sourceFolder = File(backupFolderName)
        copyWorld(sourceFolder, Bukkit.getWorld(worldName)!!.worldFolder)
    }

    private fun deleteDirectory(directory: Path?) {
        Files.walk(directory)
            .sorted(Comparator.reverseOrder())
            .map { it.toFile() }
            .forEach { it.delete() }
    }

    private fun copyWorld(source: File, target: File) {
        try {
            val ignore: ArrayList<String> = ArrayList(Arrays.asList("uid.dat", "session.dat"))
            if (!ignore.contains(source.getName())) {
                if (source.isDirectory()) {
                    if (!target.exists()) target.mkdirs()
                    val files = source.list()
                    for (file in files!!) {
                        val srcFile = File(source, file)
                        val destFile = File(target, file)
                        copyWorld(srcFile, destFile)
                    }
                } else {
                    val `in`: InputStream = FileInputStream(source)
                    val out: OutputStream = FileOutputStream(target)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (`in`.read(buffer).also { length = it } > 0) out.write(buffer, 0, length)
                    `in`.close()
                    out.close()
                }
            }
        } catch (_: IOException) {
        }
    }
}