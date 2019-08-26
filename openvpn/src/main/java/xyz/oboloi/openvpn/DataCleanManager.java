package xyz.oboloi.openvpn;

import android.content.Context;
import android.os.Environment;

import java.io.File;


public class DataCleanManager {

    public static void cleanCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
        cleanExternalCache(context);
    }


    private static void deleteDatabases(Context context) {
        for (String database : context.databaseList()) {
            context.deleteDatabase(database);
        }
    }


    private static void cleanSharedPreference(Context context) {
        deleteFilesByDirectory(new File(/*"/data/data/" + context.getPackageName()*/ context.getFilesDir().getParent() + "/shared_prefs"));
    }




    private static void cleanFiles(Context context) {
        deleteFilesByDirectory(context.getFilesDir());
    }


    private static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }


//    public static void cleanCustomCache(String filePath) {
//        deleteFilesByDirectory(new File(filePath));
//    }


    public static void cleanApplicationData(Context context/*, String... filepath*/) {
        cleanCache(context);
//        cleanExternalCache(context);
//        cleanDatabases(context);
        deleteDatabases(context);
        cleanSharedPreference(context);
        cleanFiles(context);
//        for (String filePath : filepath) {
//            cleanCustomCache(filePath);
//        }
    }


    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
//                item.deleteOnExit();
            }
        }
    }



    public static float getFolderSize(File file) {
        float size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {

                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return size;
    }
}
