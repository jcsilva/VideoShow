package com.example.android.videoshow;

import java.io.Serializable;

/**
 * Created by jcsilva on 22/01/17.
 */

public class VideoInfo implements Serializable {
    int id;
    String posterPath;

    public VideoInfo(int id, String posterPath)
    {
        this.id = id;
        this.posterPath = posterPath;
    }
}
