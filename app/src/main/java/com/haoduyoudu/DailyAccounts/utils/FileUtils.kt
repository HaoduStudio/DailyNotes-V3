package com.haoduyoudu.DailyAccounts.utils

import android.graphics.Bitmap
import android.util.Log
import com.haoduyoudu.DailyAccounts.BaseApplication
import java.io.*

object FileUtils {

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    fun delete(fileName: String): Boolean {
        val file = File(fileName)
        return if (!file.exists()) {
            println("删除文件失败:" + fileName + "不存在！")
            false
        } else {
            if (file.isFile) deleteFile(fileName) else deleteDirectory(fileName)
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    fun deleteFile(fileName: String): Boolean {
        val file = File(fileName)
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        return if (file.exists() && file.isFile) {
            if (file.delete()) {
                println("删除单个文件" + fileName + "成功！")
                true
            } else {
                println("删除单个文件" + fileName + "失败！")
                false
            }
        } else {
            println("删除单个文件失败：" + fileName + "不存在！")
            false
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    fun deleteDirectory(dir: String): Boolean {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        var dir = dir
        if (!dir.endsWith(File.separator)) dir += File.separator
        val dirFile = File(dir)
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory) {
            println("删除目录失败：" + dir + "不存在！")
            return false
        }
        var flag = true
        // 删除文件夹中的所有文件包括子目录
        val files = dirFile.listFiles()
        for (i in files.indices) {
            // 删除子文件
            if (files[i].isFile) {
                flag = deleteFile(files[i].absolutePath)
                if (!flag) break
            } else if (files[i].isDirectory) {
                flag = deleteDirectory(
                    files[i].absolutePath
                )
                if (!flag) break
            }
        }
        if (!flag) {
            println("删除目录失败！")
            return false
        }
        // 删除当前目录
        return if (dirFile.delete()) {
            println("删除目录" + dir + "成功！")
            true
        } else {
            false
        }
    }

    fun readTxtFile(filePath: String): String {
        Log.w("FileUtils", "Try to read $filePath")
        try {
            val Txts: MutableList<String> = ArrayList()
            val encoding = "UTF-8"
            val file = File(filePath)
            val SB = StringBuilder()
            if (file.isFile && file.exists()) { //判断文件是否存在
                val read = InputStreamReader(
                    FileInputStream(file), encoding
                ) //考虑到编码格式
                val bufferedReader = BufferedReader(read)
                var lineTxt: String? = null
                while (bufferedReader.readLine().also { lineTxt = it } != null) {
                    Txts.add(lineTxt!!)
                }
                read.close()
                for (i in 0 until Txts.size) {
                    SB.append(Txts[i])
                    if (i != Txts.size - 1) {
                        SB.append("\n")
                    }
                }
                return SB.toString()
            } else {
                println("找不到指定的文件")
            }
        } catch (e: Exception) {
            println("读取文件内容出错")
            e.printStackTrace()
        }
        return ""
    }

    fun writeTxtToFile(strcontent: String, filePath: String, fileName: String): Boolean {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName)
        val strFilePath = filePath + fileName
        // 每次写入时，都换行写
        return try {
            val file = File(strFilePath)
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:$strFilePath")
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            val raf = RandomAccessFile(file, "rwd")
            raf.seek(file.length())
            raf.write(strcontent.toByteArray())
            raf.close()
            true
        } catch (e: java.lang.Exception) {
            Log.e("TestFile", "Error on write File:$e")
            false
        }
    }

    private fun makeFilePath(filePath: String, fileName: String): File? {
        var file: File? = null
        makeRootDirectory(filePath)
        try {
            file = File(filePath, fileName)
            if (file.exists()) {
                file.delete()
            }
            if (!file.exists()) {
                file.createNewFile()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return file
    }

    fun makeRootDirectory(filePath: String) {
        var file: File? = null
        try {
            file = File(filePath)
            if (!file.exists()) {
                file.mkdir()
            }
        } catch (e: Exception) {
            Log.i("error:", e.toString() + "")
        }
    }

    fun copyFile(`oldPath$Name`: String, `newPath$Name`: String): Boolean {
        return try {
            val oldFile = File(`oldPath$Name`)
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.")
                return false
            } else if (!oldFile.isFile) {
                Log.e("--Method--", "copyFile:  oldFile not file.")
                return false
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.")
                return false
            }

            val fileInputStream = FileInputStream(`oldPath$Name`)
            val fileOutputStream = FileOutputStream(`newPath$Name`)
            val buffer = ByteArray(1024)
            var byteRead: Int
            while (-1 != fileInputStream.read(buffer).also { byteRead = it }) {
                fileOutputStream.write(buffer, 0, byteRead)
            }
            fileInputStream.close()
            fileOutputStream.flush()
            fileOutputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun copyFolder(oldPath: String, newPath: String): Boolean {
        return try {
            val newFile = File(newPath)
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.")
                    return false
                }
            }
            val oldFile = File(oldPath)
            val files = oldFile.list()
            var temp: File
            for (file in files!!) {
                temp = if (oldPath.endsWith(File.separator)) {
                    File(oldPath + file)
                } else {
                    File(oldPath + File.separator + file)
                }
                if (temp.isDirectory) {   //如果是子文件夹
                    copyFolder("$oldPath/$file", "$newPath/$file")
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.")
                    return false
                } else if (!temp.isFile) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.")
                    return false
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.")
                    return false
                } else {
                    val fileInputStream = FileInputStream(temp)
                    val fileOutputStream = FileOutputStream(newPath + "/" + temp.name)
                    val buffer = ByteArray(1024)
                    var byteRead: Int
                    while (fileInputStream.read(buffer).also { byteRead = it } != -1) {
                        fileOutputStream.write(buffer, 0, byteRead)
                    }
                    fileInputStream.close()
                    fileOutputStream.flush()
                    fileOutputStream.close()
                }
            }
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exists(str: String) = File(str).exists()
    fun exists(str: File) = str.exists()

    fun getFilesList(rootPath: String): MutableList<String> {
        val fileNames: MutableList<String> = mutableListOf()
        try {
            val fileTree: FileTreeWalk = File(rootPath).walk()
            val rootName = File(rootPath).name
            fileTree.maxDepth(1) //需遍历的目录层次为1，即无须检查子目录
                .filter { it.name != rootName}
                //.filter { it.extension in listOf("m4a","mp3") }
                .forEach { fileNames.add(it.name) }//循环 处理符合条件的文件
        }catch (e: Exception) {
            e.printStackTrace()
        }
        return fileNames
    }

    fun getFileSuffix(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".")) //例如：abc.png  截取后：.png
    }

    fun getAllFileNameInAssets(path: String): ArrayList<String> {
        val result = ArrayList<String>()
        BaseApplication.context.assets.list(path)?.let {
            result.addAll(it)
        }
        return result
    }

    fun getFileNameWithoutSuffix(fileName: String) = fileName.substring(0, fileName.lastIndexOf("."))

    fun saveBitmap(targetPath: String, bm: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG) {
        val saveFile = File(targetPath)
        try {
            if (saveFile.parentFile?.exists() == false) {
                makeRootDirectory(saveFile.parent!!)
            }
            val saveImgOut = FileOutputStream(saveFile)
            bm.compress(format, 100, saveImgOut)
            saveImgOut.flush()
            saveImgOut.close()
            Log.d("FileUtils", "The picture is save to your phone!")
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun removePathSlashAtLast(str: String): String {
        return if (str.last() == '/') {
            str.dropLast(1)
        }else {
            str
        }
    }
}