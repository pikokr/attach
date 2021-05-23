package com.github.patrick.attach

import com.github.patrick.attach.plugin.AttachPlugin
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Tools {
    const val TOOLS_DIR = "tools/"

    const val ATTACH_DIR = "attach/"

    private const val NATIVE_DIR = "natives/"

    @JvmStatic
    fun loadAttachLibrary() {
        val path = installBinary()
        val separator = System.getProperty("path.separator")
        val property = "java.library.path"
        val pathProperty = System.getProperty(property)
        val libraryPath = if (pathProperty != null) path + separator + pathProperty else path

        System.setProperty(property, libraryPath)

        val fieldSysPath = ClassLoader::class.java.getDeclaredField("sys_paths")
        fieldSysPath.isAccessible = true
        fieldSysPath[null] = null

//        System.loadLibrary("attach")
    }

    @JvmStatic
    private fun installBinary(): String {
        val platform = Platform.platform
        val path =
            TOOLS_DIR + ATTACH_DIR + NATIVE_DIR + (if (Platform.is64Bit) "64/" else "32/") + platform.dir + platform.binary
        val file = File(AttachPlugin.plugin.dataFolder, path)

        if (!file.exists()) {
            if (!file.parentFile.mkdirs()) {
                throw RuntimeException("Failed to create directory")
            }

            runCatching {
                this::class.java.classLoader.getResourceAsStream(path)?.use { stream ->
                    Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }.onFailure { throwable ->
                throwable.printStackTrace()
            }
        }

        return file.parentFile.canonicalPath
    }
}