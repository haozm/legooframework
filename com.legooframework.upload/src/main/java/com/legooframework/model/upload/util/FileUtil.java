package com.legooframework.model.upload.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import com.google.common.base.Preconditions;

public class FileUtil {


    private FileUtil() {
        throw new AssertionError();
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @return
     */
    public static boolean isFileExist(String fileName) {
        return new File(fileName).isFile();
    }

    /**
     * 读取文件转换成字节数组
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] readFileToBytes(File file) {
        Objects.requireNonNull(file);
        if (!file.exists())
            throw new IllegalStateException("文件不存在");
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(new FileInputStream(file));
            short bufSize = 1024;
            byte[] buffer = new byte[bufSize];
            int len;
            while (-1 != (len = in.read(buffer, 0, bufSize))) {
                bos.write(buffer, 0, len);
            }

            byte[] bs = bos.toByteArray();
            return bs;
        } catch (Exception e) {
            throw new IllegalStateException("文件转换字节流失败");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException io) {
                throw new IllegalStateException("输入流关闭失败");
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException io) {
                throw new IllegalStateException("输入流管理失败");
            }
        }
    }

    /**
     * 将文件转换为数组
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static byte[] readFileToBytes(String filePath) throws IOException {
        File file = new File(filePath);
        return readFileToBytes(file);
    }

    /**
     * 根据文件名称获取文件后缀
     *
     * @param fileName
     * @return
     */
    public static String getSuffix(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return suffix;
    }

    /**
     * 根据文件获取文件后缀
     *
     * @param file
     * @return
     */
    public static String getSuffix(File file) {
        return getSuffix(file.getName());
    }

    public static void checkExistFile(String path) {
        File file = new File(path);
        Preconditions.checkState(file.exists(), "path : %s ,is not exist", path);
    }


    /**
     * 释放资源
     *
     * @param out
     * @param bufOut
     * @param in
     * @param bufIn
     */
    public static void release(OutputStream out, BufferedOutputStream bufOut
            , InputStream in, BufferedInputStream bufIn) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bufIn != null) {
            try {
                bufIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bufOut != null) {
            try {
                bufOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void writeFile(byte[] bs, File destFile) {
        OutputStream out = null;
        BufferedOutputStream bufOut = null;
        try {
            destFile.createNewFile();
            out = new FileOutputStream(destFile);
            bufOut = new BufferedOutputStream(out);
            bufOut.write(bs, 0, bs.length);
            bufOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            release(out, bufOut, null, null);
        }

    }

    /**
     * 保存文件
     *
     * @param sourceFile 原文件
     * @param destFile   目标文件
     */
    public static void writeFile(InputStream in, File destFile) {
        OutputStream out = null;
        BufferedInputStream bufIn = null;
        BufferedOutputStream bufOut = null;
        try {
            bufIn = new BufferedInputStream(in);
            out = new FileOutputStream(destFile);
            bufOut = new BufferedOutputStream(out);
            byte[] bs = new byte[1024];
            int len = 0;
            while ((len = bufIn.read(bs)) != -1) {
                bufOut.write(bs, 0, len);
            }
            bufOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            release(out, bufOut, in, bufIn);
        }
    }

    /**
     * 保存文件
     *
     * @param sourceFile 原文件
     * @param destFile   目标文件
     * @throws FileNotFoundException
     */
    public static void writeFile(File sourceFile, File destFile) throws FileNotFoundException {
        FileInputStream in = new FileInputStream(sourceFile);
        writeFile(in, destFile);
    }

    /**
     * 判断存放文件的文件夹是否存在，如果不存在则创建文件夹
     *
     * @param path
     * @return
     */
    public static boolean createDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdirs())
                return true;
            return false;
        }
        if (file.isDirectory())
            return true;
        return false;
    }

}
