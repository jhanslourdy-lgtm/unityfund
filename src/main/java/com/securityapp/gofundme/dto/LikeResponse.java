/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.dto;

public class LikeResponse {
    private boolean liked;
    private long count;

    public LikeResponse(boolean liked, long count) {
        this.liked = liked;
        this.count = count;
    }

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
