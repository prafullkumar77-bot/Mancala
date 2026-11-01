package com.offline.mancala;

import java.util.*;

public final class Game {
    public int[] pits = new int[14];
    public int turn;
    public boolean over;
    public int captured;
    public boolean extra;
    public Game() { Arrays.fill(pits,4); pits[6]=pits[13]=0; }
    public Game copy() { Game g=new Game(); g.pits=pits.clone(); g.turn=turn; g.over=over; return g; }
    public List<Integer> moves() {
        List<Integer> result=new ArrayList<>();
        if (!over) for(int i=turn*7;i<turn*7+6;i++) if(pits[i]>0) result.add(i);
        return result;
    }
    public boolean move(int pit) {
        if(!moves().contains(pit)) return false;
        captured=0; extra=false;
        int player=turn, store=player*7+6, skip=player==0?13:6;
        int seeds=pits[pit], at=pit; pits[pit]=0;
        while(seeds>0) { at=(at+1)%14; if(at==skip) continue; pits[at]++; seeds--; }
        if(at>=player*7 && at<player*7+6 && pits[at]==1 && pits[12-at]>0) {
            captured=pits[12-at]+1; pits[store]+=captured; pits[at]=pits[12-at]=0;
        }
        extra=at==store;
        boolean empty0=true,empty1=true;
        for(int i=0;i<6;i++) { empty0 &= pits[i]==0; empty1 &= pits[i+7]==0; }
        if(empty0||empty1) {
            for(int i=0;i<6;i++) { pits[6]+=pits[i]; pits[13]+=pits[i+7]; pits[i]=pits[i+7]=0; }
            over=true; extra=false;
        } else if(!extra) turn=1-turn;
        return true;
    }
    private static final class Timeout extends RuntimeException {}
    private static int evaluate(Game g,int player) {
        int diff=g.pits[player*7+6]-g.pits[(1-player)*7+6];
        if(g.over) return diff==0?0:(diff>0?100000:-100000)+diff;
        int seeds=0; for(int i=0;i<6;i++) seeds+=g.pits[player*7+i]-g.pits[(1-player)*7+i];
        return diff*30+seeds*2+(g.turn==player?3:-3);
    }
    private static int search(Game g,int depth,int alpha,int beta,int player,long end) {
        if(Thread.currentThread().isInterrupted() || System.nanoTime()>end) throw new Timeout();
        if(depth==0||g.over) return evaluate(g,player);
        boolean max=g.turn==player; int best=max?-1000000:1000000;
        for(int move:g.moves()) {
            Game next=g.copy(); next.move(move);
            int score=search(next,depth-1,alpha,beta,player,end);
            best=max?Math.max(best,score):Math.min(best,score);
            if(max) alpha=Math.max(alpha,best); else beta=Math.min(beta,best);
            if(beta<=alpha) break;
        }
        return best;
    }
    public static int bot(Game g,boolean expert,long millis) {
        List<Integer> legal=g.moves(); if(legal.isEmpty()) return -1;
        if(!expert) return legal.get(new Random().nextInt(legal.size()));
        long end=System.nanoTime()+millis*1000000; int best=legal.get(0);
        for(int depth=1;depth<=16;depth++) {
            int candidate=best,score=-1000000;
            List<Integer> ordered=new ArrayList<>(legal); ordered.remove(Integer.valueOf(best)); ordered.add(0,best);
            try {
                for(int move:ordered) {
                    Game next=g.copy(); next.move(move);
                    int value=search(next,depth-1,-1000000,1000000,g.turn,end);
                    if(value>score) { score=value; candidate=move; }
                }
                best=candidate;
            } catch(Timeout t) { break; }
        }
        return best;
    }
}
