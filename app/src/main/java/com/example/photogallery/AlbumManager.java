package com.example.photogallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AlbumManager {
    private static final String PREFS_NAME = "photo_gallery_albums";
    private static final String ALBUMS_KEY = "albums";
    private static AlbumManager instance;
    private List<Album> albums;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    private AlbumManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadAlbums();
    }

    public static synchronized AlbumManager getInstance(Context context) {
        if (instance == null) {
            instance = new AlbumManager(context.getApplicationContext());
        }
        return instance;
    }

    private void loadAlbums() {
        try {
            String albumsJson = sharedPreferences.getString(ALBUMS_KEY, null);
            if (albumsJson != null) {
                Type type = new TypeToken<List<Album>>(){}.getType();
                albums = gson.fromJson(albumsJson, type);
            } else {
                albums = new ArrayList<>();
            }
        } catch (Exception e) {
            albums = new ArrayList<>();
        }
    }

    private void saveAlbums() {
        try {
            String albumsJson = gson.toJson(albums);
            sharedPreferences.edit().putString(ALBUMS_KEY, albumsJson).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Album> getAlbums() {
        return new ArrayList<>(albums);
    }

    public Album createAlbum(String name) {
        long id = System.currentTimeMillis();
        Album album = new Album(id, name);
        albums.add(album);
        saveAlbums();
        return album;
    }

    public void deleteAlbum(Album album) {
        for (int i = albums.size() - 1; i >= 0; i--) {
            if (albums.get(i).getId() == album.getId()) {
                albums.remove(i);
                break;
            }
        }
        saveAlbums();
    }

    public void renameAlbum(Album album, String newName) {
        for (Album a : albums) {
            if (a.getId() == album.getId()) {
                a.setName(newName);
                saveAlbums();
                break;
            }
        }
    }

    public void clearAlbum(Album album) {
        for (Album a : albums) {
            if (a.getId() == album.getId()) {
                a.clearImages();
                saveAlbums();
                break;
            }
        }
    }

    public void addImageToAlbum(Album album, Uri imageUri) {
        for (Album a : albums) {
            if (a.getId() == album.getId()) {
                a.addImage(imageUri);
                saveAlbums();
                break;
            }
        }
    }

    public void removeImageFromAlbum(Album album, Uri imageUri) {
        for (Album a : albums) {
            if (a.getId() == album.getId()) {
                a.removeImage(imageUri);
                saveAlbums();
                break;
            }
        }
    }

    public Album getAlbumById(long id) {
        for (Album album : albums) {
            if (album.getId() == id) {
                return album;
            }
        }
        return null;
    }
} 