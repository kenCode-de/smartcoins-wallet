package com.luminiasoft.bitshares;



/**
 * <p>
 * Generic class used to represent a graphene object as defined in
 * <a href="http://docs.bitshares.org/development/blockchain/objects.html"></a>
 * </p>
 * Created by nelson on 11/8/16.
 */
public class GrapheneObject {
    protected int space;
    protected int type;
    protected long instance;
    protected String id;

    public GrapheneObject(String id){
        String[] parts = id.split("\\.");
        if(parts.length == 3){
            this.space = Integer.parseInt(parts[0]);
            this.type = Integer.parseInt(parts[1]);
            this.instance = Long.parseLong(parts[2]);
        }
        this.id = id;
    }
}
