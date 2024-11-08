package com.mygdx.game.states.game.standard.market;


import com.mygdx.game.states.game.standard.item.Item;

import java.io.Serializable;

/**
 * Stores information of items
 * being sold in items market
 *
 * @author Pedro Sampaio
 * @since 1.8
 */
public class MarketItem implements Serializable {

    private int mid; // the market item ID for this item
    private int uid; // the unique ID of item
    private String seller; // the name of the item seller
    private long price; // the price of the item
    private int level;  // the level of the item
    private Item.Quality quality; // the quality of the item
    private boolean sold; // item was sold?
    private String name; // the name of the item
    private String description; // the description of the item
    private int page; // the page of this item in player inventory
    private int idxI; // the index i of this item in player inventory
    private int idxJ; // the index j of this item in player inventory

    /**
     * Getters and Setters
     */

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Item.Quality getQuality() {
        return quality;
    }

    public void setQuality(Item.Quality quality) {
        this.quality = quality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPage() {return page;}

    public void setPage(int page) {this.page = page;}

    public int getIdxI() {return idxI;}

    public void setIdxI(int idxI) {this.idxI = idxI;}

    public int getIdxJ() {return idxJ;}

    public void setIdxJ(int idxJ) {this.idxJ = idxJ;}

    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }
}
