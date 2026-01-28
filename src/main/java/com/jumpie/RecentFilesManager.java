package com.jumpie;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RecentFilesManager {
    private static final String RECENT_FILES_PATH = "recent_files.dat";
    private static final int MAX_RECENT_FILES = 10;
    private final LinkedList<String> recentFiles;
    private final List<RecentFilesListener> listeners;

    public interface RecentFilesListener {
        void onRecentFilesChanged();
    }

    public RecentFilesManager() {
        this.recentFiles = new LinkedList<>();
        this.listeners = new ArrayList<>();
        loadRecentFiles();
    }

    public void addRecentFile(File file) {
        if (file == null || !file.exists()) return;

        String absolutePath = file.getAbsolutePath();

        // Удаляем файл из списка, если он уже есть
        recentFiles.remove(absolutePath);

        // Добавляем файл в начало списка
        recentFiles.addFirst(absolutePath);

        // Ограничиваем размер списка
        while (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.removeLast();
        }

        saveRecentFiles();
        notifyListeners();
    }

    public List<File> getRecentFiles() {
        List<File> files = new ArrayList<>();

        // Создаем копию списка для итерации
        List<String> pathsCopy = new ArrayList<>(recentFiles);

        for (String path : pathsCopy) {
            File file = new File(path);
            if (file.exists()) {
                files.add(file);
            } else {
                // Удаляем несуществующие файлы
                recentFiles.remove(path);
            }
        }

        // Сохраняем обновленный список, если были удалены файлы
        if (pathsCopy.size() != recentFiles.size()) {
            saveRecentFiles();
        }

        return files;
    }

    public void clearRecentFiles() {
        recentFiles.clear();
        saveRecentFiles();
        notifyListeners();
    }

    public void removeRecentFile(File file) {
        if (file == null) return;
        recentFiles.remove(file.getAbsolutePath());
        saveRecentFiles();
        notifyListeners();
    }

    public void addListener(RecentFilesListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RecentFilesListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (RecentFilesListener listener : listeners) {
            listener.onRecentFilesChanged();
        }
    }

    private void saveRecentFiles() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RECENT_FILES_PATH))) {
            oos.writeObject(new ArrayList<>(recentFiles));
        } catch (IOException e) {
            System.err.println("Не удалось сохранить список недавних файлов: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadRecentFiles() {
        File file = new File(RECENT_FILES_PATH);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RECENT_FILES_PATH))) {
            List<String> loaded = (List<String>) ois.readObject();
            recentFiles.addAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Не удалось загрузить список недавних файлов: " + e.getMessage());
        }
    }

    public int getMaxRecentFiles() {
        return MAX_RECENT_FILES;
    }

    public boolean hasRecentFiles() {
        return !getRecentFiles().isEmpty();
    }
}
