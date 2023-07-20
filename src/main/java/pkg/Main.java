package pkg;

import com.intuit.karate.job.JobUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("args: " + Arrays.asList(args));
        File src = new File(args[0]);
        File dest = new File(args[1]);
        // zip(src, dest);
        JobUtils.zip(src, dest);
        System.out.println("done: " + dest.getAbsolutePath());
    }
    
    public static void zip(File src, File dest) {
        try {
            FileOutputStream fos = new FileOutputStream(dest);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            zip(src.getAbsoluteFile(), "", zipOut, 0);
            zipOut.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void zip(File fileToZip, String fileName, ZipOutputStream zipOut, int level) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            String entryName = fileName;
            zipOut.putNextEntry(new ZipEntry(entryName + "/"));
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                String childFileName = childFile.getName();
                // TODO improve ?
                if (childFileName.equals("target") || childFileName.equals("build")) {
                    continue;
                }
                if (level != 0) {
                    childFileName = entryName + "/" + childFileName;
                }
                zip(childFile, childFileName, zipOut, level + 1);
            }
            return;
        }
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        RandomAccessFile reader = new RandomAccessFile(fileToZip.getAbsolutePath(), "r");
        FileChannel fc = reader.getChannel();
        int bufferSize = 1024;
        if (bufferSize > fc.size()) {
            bufferSize = (int) fc.size();
        }
        ByteBuffer bb = ByteBuffer.allocate(bufferSize);
        while (fc.read(bb) > 0) {
            zipOut.write(bb.array(), 0, bb.position());
            bb.clear();
        }
        reader.close();
    }    
    
}
