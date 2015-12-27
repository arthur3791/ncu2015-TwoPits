package com.twopits.balls.cdc;

import java.util.Random;
import java.util.Vector;

/**
 * Created by Lenovo on 2015/12/18.
 */
public class CentralizedDataCenter implements CentralizedDataCenterInterface {

    private Vector<Player> playerMap;
    private Vector<Ball> ballMap;
    public CentralizedDataCenter(){
        playerMap = new Vector<Player>();
        ballMap = new Vector<Ball>();
    }
    public Vector getPlayerMap(){
        return playerMap;
    }
    public int getPlayerMapCount(){
        return playerMap.size();
    }

    public Player addPlayer() {
        if(getPlayerMapCount()==4){
            return null;
        }
        double x=0,y=0;
        int dir=0;
        Random random=new Random();
        boolean b=false;
        while(!b) {
            x = random.nextInt(10)*100 + 25 + random.nextInt(2) * 50;
            y =  random.nextInt(10)*100 + 25 + random.nextInt(2) * 50;
            b=true;
            for(int i=0;i<playerMap.size();i++){
               if(x==playerMap.elementAt(i).getX()&&y==playerMap.elementAt(i).getY()){
                   b=false;
               }
            }
        }
        Player player = new Player(playerMap.size(),x,y,dir);
        playerMap.add(player);
        return player;
    }
    public void updateCharacterStatus(int clientno,double x,double y,int dir){
        Player temp = findPlayer(clientno);
        temp.updateInfo(x,y,dir);
    }
    public Player findPlayer(int clientno){
        for(int i = 0; i< playerMap.size(); i++){
            if(playerMap.elementAt(i).getID()==clientno) {
                return playerMap.elementAt(i);
            }
        }
        return null;
    }

    boolean addBall(int color,int index,int x,int y) {
        Ball ball = new Ball(color,index,x,y);
        ballMap.add(ball);
        return true;
    }
    Ball findBall(int index){
        for(int i=0;i<ballMap.size();i++){
            if(ballMap.elementAt(i).getIndex()==index) {
                return ballMap.elementAt(i);
            }
        }
        return null;
    }

    void startUpdatingThread() throws InterruptedException {

    }
}
